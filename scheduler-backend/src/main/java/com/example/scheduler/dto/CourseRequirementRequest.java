package com.example.scheduler.dto;

public class CourseRequirementRequest {
    private String subject;
    private Integer sessions;

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Integer getSessions() { return sessions; }
    public void setSessions(Integer sessions) { this.sessions = sessions; }
}
