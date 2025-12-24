package com.example.scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "schedule_items")
public class ScheduleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer dayOfWeek; // 1-5
    private Integer period;    // 1-8
    private String subject;    // 科目名稱

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    @JsonIgnore
    private Teacher teacher;

    // --- 建構子 ---
    public ScheduleItem() {}

    public ScheduleItem(Integer dayOfWeek, Integer period, String subject, Teacher teacher) {
        this.dayOfWeek = dayOfWeek;
        this.period = period;
        this.subject = subject;
        this.teacher = teacher;
    }

    // --- Getters & Setters ---
    public Long getId() {
         return id; 
    }

    public void setId(Long id) {
         this.id = id; 
    }

    public Integer getDayOfWeek() {
         return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) { 
        this.dayOfWeek = dayOfWeek; 
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) { 
        this.period = period; 
    }

    public String getSubject() { 
        return subject; 
    }

    public void setSubject(String subject) { 
        this.subject = subject; 
    }

    public Teacher getTeacher() { 
        return teacher; 
    }

    public void setTeacher(Teacher teacher) { 
        this.teacher = teacher; 
    }
}