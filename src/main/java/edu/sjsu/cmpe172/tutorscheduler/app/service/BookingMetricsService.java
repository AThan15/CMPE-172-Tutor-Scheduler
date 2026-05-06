package edu.sjsu.cmpe172.tutorscheduler.app.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Service
public class BookingMetricsService {

    private final ConcurrentHashMap<String, LongAdder> bookingsPerHour = new ConcurrentHashMap<>();
    private final LongAdder failedBookings = new LongAdder();
    private final LongAdder totalLatencyMs = new LongAdder();
    private final LongAdder successfulBookings = new LongAdder();

    public void recordBookingSuccess(long latencyMs) {
        successfulBookings.increment();
        totalLatencyMs.add(latencyMs);
        String hourKey = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0).toString();
        bookingsPerHour.computeIfAbsent(hourKey, key -> new LongAdder()).increment();
    }

    public void recordBookingFailure() {
        failedBookings.increment();
    }

    public long getCurrentHourBookings() {
        String hourKey = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0).toString();
        LongAdder counter = bookingsPerHour.get(hourKey);
        return counter == null ? 0L : counter.sum();
    }

    public long getFailedBookings() {
        return failedBookings.sum();
    }

    public long getAverageBookingLatencyMs() {
        long successes = successfulBookings.sum();
        if (successes == 0) {
            return 0L;
        }
        return totalLatencyMs.sum() / successes;
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("bookingsPerHour", getCurrentHourBookings());
        metrics.put("failedBookings", getFailedBookings());
        metrics.put("avgBookingLatencyMs", getAverageBookingLatencyMs());
        return metrics;
    }
}
