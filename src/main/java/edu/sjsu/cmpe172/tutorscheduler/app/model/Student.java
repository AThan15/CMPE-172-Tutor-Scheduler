package edu.sjsu.cmpe172.tutorscheduler.app.model;
public class Student {

    private Long studentID;

    private String studentName;
    private String sEmail;
    private String sPhoneNumber;

    public Student() {}

    public Student(String studentName, String sEmail, String sPhoneNumber) {
        this.studentName = studentName;
        this.sEmail = sEmail;
        this.sPhoneNumber = sPhoneNumber;
    }

    public Long getStudentID() { return studentID; }
    public void setStudentID(Long studentID) { this.studentID = studentID; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getSEmail() { return sEmail; }
    public void setSEmail(String sEmail) { this.sEmail = sEmail; }

    public String getSPhoneNumber() { return sPhoneNumber; }
    public void setSPhoneNumber(String sPhoneNumber) { this.sPhoneNumber = sPhoneNumber; }
}
