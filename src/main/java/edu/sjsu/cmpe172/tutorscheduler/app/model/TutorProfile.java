package edu.sjsu.cmpe172.tutorscheduler.app.model;

public class TutorProfile {

    private Long tutorId;
    private String tutorName;

    public TutorProfile() {
    }

    public TutorProfile(Long tutorId, String tutorName) {
        this.tutorId = tutorId;
        this.tutorName = tutorName;
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
}
