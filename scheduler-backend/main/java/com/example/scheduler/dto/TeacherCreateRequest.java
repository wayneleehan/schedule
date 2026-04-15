package com.example.scheduler.dto;

public class TeacherCreateRequest {

    private String name;
    private String password;
    private Integer grade;
    private String type;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
