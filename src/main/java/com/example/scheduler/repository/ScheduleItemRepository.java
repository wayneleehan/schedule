package com.example.scheduler.repository;

import com.example.scheduler.model.ScheduleItem;
import com.example.scheduler.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {
    List<ScheduleItem> findByTeacher(Teacher teacher);
    
    @Transactional
    void deleteByTeacher(Teacher teacher);
}