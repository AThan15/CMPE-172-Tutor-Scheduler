package edu.sjsu.cmpe172.tutorscheduler.app.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {

    private Long lessonId;
    private String status;
    private LocalDate date;
    private LocalTime startTime;
    private String studentName;
    private String studentEmail;
    private String tutorName;
    private String subjectName;
    private String locationName;

    public Appointment() {
    }

    public Appointment(Long lessonId, String status, LocalDate date, LocalTime startTime, String studentName, String studentEmail, String tutorName, String subjectName, String locationName) {
        this.lessonId = lessonId;
        this.status = status;
        this.date = date;
        this.startTime = startTime;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.tutorName = tutorName;
        this.subjectName = subjectName;
        this.locationName = locationName;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
