package edu.sjsu.cmpe172.tutorscheduler.app.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import edu.sjsu.cmpe172.tutorscheduler.app.model.Appointment;
import edu.sjsu.cmpe172.tutorscheduler.app.model.AppointmentRequest;
import edu.sjsu.cmpe172.tutorscheduler.app.model.AvailabilitySlot;
import edu.sjsu.cmpe172.tutorscheduler.app.repository.AppointmentRepository;
import edu.sjsu.cmpe172.tutorscheduler.app.repository.AvailabilityRepository;
import edu.sjsu.cmpe172.tutorscheduler.app.service.tx.BookingTransactionService;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final BookingTransactionService bookingTransactionService;
    private final BookingMetricsService bookingMetricsService;

    public AppointmentService(
            AvailabilityRepository availabilityRepository,
            AppointmentRepository appointmentRepository,
            BookingTransactionService bookingTransactionService,
            BookingMetricsService bookingMetricsService
    ) {
        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
        this.bookingTransactionService = bookingTransactionService;
        this.bookingMetricsService = bookingMetricsService;
    }

    public List<AvailabilitySlot> getAvailableSlots() {
        return availabilityRepository.findAll();
    }

    public List<Appointment> getAppointments() {
        return appointmentRepository.findAll();
    }

    public Appointment bookAppointment(AppointmentRequest request) {
        String requestId = UUID.randomUUID().toString();
        long startNs = System.nanoTime();
        log.info("Booking started requestId={} tutorId={} date={} startTime={}",
                requestId, request.getTutorId(), request.getDate(), request.getStartTime());

        int attempts = 0;
        while (true) {
            try {
                Appointment appointment = bookingTransactionService.bookAppointmentOnce(request);
                long latencyMs = (System.nanoTime() - startNs) / 1_000_000;
                bookingMetricsService.recordBookingSuccess(latencyMs);
                log.info("Booking confirmed requestId={} lessonId={} tutor={} durationMs={}",
                        requestId, appointment.getLessonId(), appointment.getTutorName(), latencyMs);
                return appointment;
            } catch (CannotAcquireLockException | DeadlockLoserDataAccessException ex) {
                attempts++;
                log.warn("Retrying booking requestId={} attempt={} reason={}", requestId, attempts, ex.getClass().getSimpleName());
                if (attempts >= 3) {
                    bookingMetricsService.recordBookingFailure();
                    log.error("Booking failed after retries requestId={} tutorId={} date={} startTime={} error={}",
                            requestId, request.getTutorId(), request.getDate(), request.getStartTime(), ex.getMessage());
                    throw ex;
                }
                backoff(attempts);
            } catch (DataIntegrityViolationException ex) {
                bookingMetricsService.recordBookingFailure();
                log.warn("Booking conflict requestId={} tutorId={} date={} startTime={} reason={}",
                        requestId, request.getTutorId(), request.getDate(), request.getStartTime(), ex.getClass().getSimpleName());
                throw ex;
            } catch (RuntimeException ex) {
                bookingMetricsService.recordBookingFailure();
                log.error("Unhandled booking failure requestId={} tutorId={} date={} startTime={} message={}",
                        requestId, request.getTutorId(), request.getDate(), request.getStartTime(), ex.getMessage(), ex);
                throw ex;
            }
        }
    }

    public Appointment getAppointment(Long id) {
        return appointmentRepository.findById(id);
    }

    private void backoff(int attempts) {
        try {
            Thread.sleep(75L * attempts);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
