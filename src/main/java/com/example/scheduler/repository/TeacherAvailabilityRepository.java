package com.example.scheduler.repository;

import com.example.scheduler.model.Teacher;
import com.example.scheduler.model.TeacherAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TeacherAvailabilityRepository extends JpaRepository<TeacherAvailability, Long> {
    List<TeacherAvailability> findByTeacher(Teacher teacher);

    @Transactional
    void deleteByTeacher(Teacher teacher);
}