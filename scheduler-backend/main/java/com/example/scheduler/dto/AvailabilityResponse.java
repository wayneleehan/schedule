package com.example.scheduler.dto;

import com.example.scheduler.model.TeacherAvailability;

public class AvailabilityResponse {
    private Long id;
    private Integer dayOfWeek;
    private Integer period;
    private Long teacherId;

    public AvailabilityResponse(TeacherAvailability slot) {
        this.id = slot.getId();
        this.dayOfWeek = slot.getDayOfWeek();
        this.period = slot.getPeriod();
        this.teacherId = slot.getTeacher().getId();
    }

    public Long getId() { return id; }
    public Integer getDayOfWeek() { return dayOfWeek; }
    public Integer getPeriod() { return period; }
    public Long getTeacherId() { return teacherId; }
}
