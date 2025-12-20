package com.example.scheduler.repository;

import com.example.scheduler.model.CourseRequirement;
import com.example.scheduler.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CourseRequirementRepository extends JpaRepository<CourseRequirement, Long> {
    // 根據老師尋找他的所有課程需求
    List<CourseRequirement> findByTeacher(Teacher teacher);

    // 刪除該老師的所有需求 (更新前先清空舊的)
    @Transactional
    void deleteByTeacher(Teacher teacher);
}