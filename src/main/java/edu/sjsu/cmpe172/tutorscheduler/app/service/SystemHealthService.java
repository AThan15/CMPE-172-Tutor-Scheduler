package edu.sjsu.cmpe172.tutorscheduler.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SystemHealthService {

    private final JdbcTemplate jdbcTemplate;
    private final BookingMetricsService bookingMetricsService;
    private final String providerCalendarBaseUrl;
    private final RestTemplate restTemplate;

    public SystemHealthService(
            JdbcTemplate jdbcTemplate,
            BookingMetricsService bookingMetricsService,
            @Value("${integration.provider-calendar.base-url}") String providerCalendarBaseUrl
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.bookingMetricsService = bookingMetricsService;
        this.providerCalendarBaseUrl = providerCalendarBaseUrl;
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> buildHealthPayload() {
        String dbStatus = checkDatabase();
        String providerStatus = checkProviderCalendarMock();
        String overallStatus = "UP".equals(dbStatus) && "UP".equals(providerStatus) ? "UP" : "DOWN";

        Map<String, Object> checks = new LinkedHashMap<>();
        checks.put("database", dbStatus);
        checks.put("providerCalendarMock", providerStatus);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", overallStatus);
        payload.put("service", "tutor-scheduler");
        payload.put("time", OffsetDateTime.now().toString());
        payload.put("checks", checks);
        payload.put("metrics", bookingMetricsService.snapshot());
        return payload;
    }

    private String checkDatabase() {
        try {
            Integer value = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return value != null && value == 1 ? "UP" : "DOWN";
        } catch (RuntimeException ex) {
            return "DOWN";
        }
    }

    private String checkProviderCalendarMock() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(providerCalendarBaseUrl + "/health", Map.class);
            return response.getStatusCode().is2xxSuccessful() ? "UP" : "DOWN";
        } catch (RestClientException ex) {
            return "DOWN";
        }
    }
}
