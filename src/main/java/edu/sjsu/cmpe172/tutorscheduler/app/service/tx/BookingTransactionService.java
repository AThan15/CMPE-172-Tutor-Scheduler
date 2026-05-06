package edu.sjsu.cmpe172.tutorscheduler.app.service.tx;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import edu.sjsu.cmpe172.tutorscheduler.app.model.Appointment;
import edu.sjsu.cmpe172.tutorscheduler.app.model.AppointmentRequest;
import edu.sjsu.cmpe172.tutorscheduler.app.model.AvailabilitySlot;
import edu.sjsu.cmpe172.tutorscheduler.app.repository.AppointmentRepository;
import edu.sjsu.cmpe172.tutorscheduler.app.repository.AvailabilityRepository;

@Service
public class BookingTransactionService {

    private final AvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;

    public BookingTransactionService(AvailabilityRepository availabilityRepository, AppointmentRepository appointmentRepository) {
        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Appointment bookAppointmentOnce(AppointmentRequest request) {
        LocalDate date = parseDate(request.getDate());
        LocalTime startTime = parseTime(request.getStartTime());

        AvailabilitySlot slot = availabilityRepository.lockSlotForUpdate(request.getTutorId(), date, startTime);
        if (slot == null) {
            throw new IllegalStateException("Slot not found.");
        }

        if (appointmentRepository.existsByTutorAndTime(request.getTutorId(), date, startTime)) {
            throw new DuplicateKeyException("Slot already booked.");
        }

        Long studentId = appointmentRepository.findOrCreateStudent(request.getStudentName(), request.getStudentEmail());
        Long subjectId = appointmentRepository.findOrCreateSubject(request.getSubjectName());
        Long locationId = appointmentRepository.findOrCreateLocation("TBD", null);

        Appointment appointment = appointmentRepository.insertLesson(request.getTutorId(), date, startTime, slot.getEndTime(), studentId, locationId,subjectId
        );
        if (appointment == null) {
            throw new IllegalStateException("Failed to create appointment.");
        }
        return appointment;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            return LocalDate.now();
        }
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return LocalTime.of(9, 0);
        }
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException ex) {
            return LocalTime.of(9, 0);
        }
    }
}
