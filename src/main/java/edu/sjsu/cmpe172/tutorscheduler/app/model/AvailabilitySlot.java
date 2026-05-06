package edu.sjsu.cmpe172.tutorscheduler.app.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilitySlot {

    private Long tutorId;
    private String tutorName;
    private String subjectName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String locationName;

    public AvailabilitySlot() {
    }

    public AvailabilitySlot(Long tutorId, String tutorName, String subjectName, LocalDate date, LocalTime startTime, LocalTime endTime, String locationName) {
        this.tutorId = tutorId;
        this.tutorName = tutorName;
        this.subjectName = subjectName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.locationName = locationName;
    }

    public Long getTutorId() {
        return tutorId;
    }

    public void setTutorId(Long tutorId) {
        this.tutorId = tutorId;
    }

    public String getTutorName() {
        return tutorName;
    }

    public void setTutorName(String tutorName) {
        this.tutorName = tutorName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
