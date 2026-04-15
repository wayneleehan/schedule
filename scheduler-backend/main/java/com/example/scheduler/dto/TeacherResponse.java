package com.example.scheduler.dto;

import com.example.scheduler.model.Teacher;

public class TeacherResponse {

    private Long id;
    private String name;
    private Integer grade;
    private String type;

    public TeacherResponse(Teacher teacher) {
        this.id = teacher.getId();
        this.name = teacher.getName();
        this.grade = teacher.getGrade();
        this.type = teacher.getType();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getGrade() { return grade; }
    public String getType() { return type; }
}
