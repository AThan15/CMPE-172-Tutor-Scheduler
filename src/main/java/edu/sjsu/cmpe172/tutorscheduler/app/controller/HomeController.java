package edu.sjsu.cmpe172.tutorscheduler.app.controller;

import edu.sjsu.cmpe172.tutorscheduler.app.service.SystemHealthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    private final SystemHealthService systemHealthService;

    public HomeController(SystemHealthService systemHealthService) {
        this.systemHealthService = systemHealthService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> payload = systemHealthService.buildHealthPayload();
        String status = String.valueOf(payload.get("status"));
        HttpStatus httpStatus = "UP".equals(status) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(httpStatus).body(payload);
    }
}
