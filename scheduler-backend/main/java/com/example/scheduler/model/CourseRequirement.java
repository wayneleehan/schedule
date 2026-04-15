package com.example.scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "course_requirements")
public class CourseRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject; // 科目名稱 (國文、英文...)
    private Integer sessions; // 節數 (每週幾堂)

    // 關聯到 Teacher (多對一：多個需求屬於一個老師)
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    @JsonIgnore // 避免轉 JSON 時無限迴圈 (Teacher -> Course -> Teacher...)
    private Teacher teacher;

    // --- 建構子 ---
    public CourseRequirement() {}

    public CourseRequirement(String subject, Integer sessions, Teacher teacher) {
        this.subject = subject;
        this.sessions = sessions;
        this.teacher = teacher;
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public Integer getSessions() { return sessions; }
    public void setSessions(Integer sessions) { this.sessions = sessions; }
    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
}