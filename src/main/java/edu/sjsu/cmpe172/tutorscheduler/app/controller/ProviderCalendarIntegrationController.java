package edu.sjsu.cmpe172.tutorscheduler.app.controller;

import edu.sjsu.cmpe172.tutorscheduler.app.model.integration.ProviderCalendarEventResponse;
import edu.sjsu.cmpe172.tutorscheduler.app.service.ProviderCalendarIntegrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/integrations/provider-calendar")
public class ProviderCalendarIntegrationController {

    private final ProviderCalendarIntegrationService providerCalendarIntegrationService;

    public ProviderCalendarIntegrationController(ProviderCalendarIntegrationService providerCalendarIntegrationService) {
        this.providerCalendarIntegrationService = providerCalendarIntegrationService;
    }

    @PostMapping("/appointments/{lessonId}/sync")
    public ResponseEntity<Map<String, Object>> syncAppointment(@PathVariable Long lessonId) {
        try {
            ProviderCalendarEventResponse externalResponse =
                    providerCalendarIntegrationService.syncAppointmentToProviderCalendar(lessonId);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("lessonId", lessonId);
            payload.put("integration", "provider-calendar");
            payload.put("status", "SYNCED");
            payload.put("externalEventId", externalResponse.getExternalEventId());
            payload.put("externalStatus", externalResponse.getStatus());
            payload.put("externalMessage", externalResponse.getMessage());
            payload.put("syncedAt", externalResponse.getSyncedAt());
            return ResponseEntity.ok(payload);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("lessonId", lessonId);
            error.put("status", "FAILED");
            error.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
