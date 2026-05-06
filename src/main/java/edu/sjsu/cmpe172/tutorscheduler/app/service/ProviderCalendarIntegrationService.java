package edu.sjsu.cmpe172.tutorscheduler.app.service;

import edu.sjsu.cmpe172.tutorscheduler.app.model.Appointment;
import edu.sjsu.cmpe172.tutorscheduler.app.model.integration.ProviderCalendarEventRequest;
import edu.sjsu.cmpe172.tutorscheduler.app.model.integration.ProviderCalendarEventResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProviderCalendarIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(ProviderCalendarIntegrationService.class);

    private final AppointmentService appointmentService;
    private final RestTemplate restTemplate;
    private final String providerCalendarBaseUrl;

    public ProviderCalendarIntegrationService(
            AppointmentService appointmentService,
            @Value("${integration.provider-calendar.base-url}") String providerCalendarBaseUrl
    ) {
        this.appointmentService = appointmentService;
        this.providerCalendarBaseUrl = providerCalendarBaseUrl;
        this.restTemplate = new RestTemplate();
    }

    public ProviderCalendarEventResponse syncAppointmentToProviderCalendar(Long lessonId) {
        log.info("Provider calendar sync started lessonId={}", lessonId);
        Appointment appointment = appointmentService.getAppointment(lessonId);
        if (appointment == null) {
            log.warn("Provider calendar sync rejected lessonId={} reason=appointment_not_found", lessonId);
            throw new IllegalArgumentException("Appointment not found: " + lessonId);
        }

        ProviderCalendarEventRequest externalRequest = new ProviderCalendarEventRequest();
        externalRequest.setAppointmentId(appointment.getLessonId());
        externalRequest.setTutorName(appointment.getTutorName());
        externalRequest.setStudentName(appointment.getStudentName());
        externalRequest.setSubjectName(appointment.getSubjectName());
        externalRequest.setDate(String.valueOf(appointment.getDate()));
        externalRequest.setStartTime(String.valueOf(appointment.getStartTime()));
        externalRequest.setLocationName(appointment.getLocationName());
        externalRequest.setAction("UPSERT");

        int attempt = 0;
        while (true) {
            try {
                ResponseEntity<ProviderCalendarEventResponse> response = restTemplate.exchange(
                        providerCalendarBaseUrl + "/events",
                        HttpMethod.POST,
                        new HttpEntity<>(externalRequest),
                        ProviderCalendarEventResponse.class
                );

                ProviderCalendarEventResponse body = response.getBody();
                if (body == null) {
                    throw new IllegalStateException("Mock provider calendar returned an empty response.");
                }
                log.info("Provider calendar sync success lessonId={} externalEventId={} externalStatus={}",
                        lessonId, body.getExternalEventId(), body.getStatus());
                return body;
            } catch (RestClientException ex) {
                attempt++;
                if (attempt >= 3) {
                    log.error("Provider calendar sync failed lessonId={} attempts={} message={}",
                            lessonId, attempt, ex.getMessage(), ex);
                    throw ex;
                }
                log.warn("Provider calendar sync retry lessonId={} attempt={} reason={}",
                        lessonId, attempt, ex.getClass().getSimpleName());
                backoff(attempt);
            }
        }
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(100L * attempt);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
