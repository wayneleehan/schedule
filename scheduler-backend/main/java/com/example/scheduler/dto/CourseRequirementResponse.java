package com.example.scheduler.dto;

import com.example.scheduler.model.CourseRequirement;

public class CourseRequirementResponse {
    private Long id;
    private String subject;
    private Integer sessions;
    private Long teacherId;

    public CourseRequirementResponse(CourseRequirement req) {
        this.id = req.getId();
        this.subject = req.getSubject();
        this.sessions = req.getSessions();
        this.teacherId = req.getTeacher().getId();
    }

    public Long getId() { return id; }
    public String getSubject() { return subject; }
    public Integer getSessions() { return sessions; }
    public Long getTeacherId() { return teacherId; }
}
