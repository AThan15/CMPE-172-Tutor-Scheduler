package edu.sjsu.cmpe172.tutorscheduler.app.controller;

import edu.sjsu.cmpe172.tutorscheduler.app.model.integration.ProviderCalendarEventRequest;
import edu.sjsu.cmpe172.tutorscheduler.app.model.integration.ProviderCalendarEventResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/mock/provider-calendar/v1")
public class MockProviderCalendarController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "UP");
        payload.put("service", "mock-provider-calendar");
        payload.put("time", OffsetDateTime.now().toString());
        return payload;
    }

    @PostMapping("/events")
    public ProviderCalendarEventResponse upsertCalendarEvent(@RequestBody ProviderCalendarEventRequest request) {
        ProviderCalendarEventResponse response = new ProviderCalendarEventResponse();
        response.setExternalEventId("calender-" + request.getAppointmentId());
        response.setStatus("CONFIRMED");
        response.setMessage("Mock provider calendar accepted event upsert for tutor " + request.getTutorName() + ".");
        response.setSyncedAt(OffsetDateTime.now().toString());
        return response;
    }
}
