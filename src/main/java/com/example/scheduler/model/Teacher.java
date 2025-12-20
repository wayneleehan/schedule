package com.example.scheduler.model;

import jakarta.persistence.*;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String password;
    private Integer grade;
    private String type; // 新增：用來存 "HOMEROOM" (班導) 或 "SUBJECT" (科任)

    // --- 建構子 ---
    public Teacher() {
    }

    public Teacher(String name, String password, Integer grade, String type) {
        this.name = name;
        this.password = password;
        this.grade = grade;
        this.type = type;
    }

    // --- Getters 和 Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    // 新增 type 的 Getter/Setter
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}