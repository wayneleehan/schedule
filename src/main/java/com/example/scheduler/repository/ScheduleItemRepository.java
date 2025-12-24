package com.example.scheduler.repository;

import com.example.scheduler.model.ScheduleItem;
import com.example.scheduler.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {
    
    // 透過 Teacher 物件查詢
    List<ScheduleItem> findByTeacher(Teacher teacher);
    
    // 🔥 [新增] 透過 Teacher 的 ID 查詢 (這就是你缺少的)
    List<ScheduleItem> findByTeacher_Id(Long teacherId);
    
    // 查詢某個年級的所有課表
    List<ScheduleItem> findByTeacher_Grade(Integer grade);
    
    @Transactional
    void deleteByTeacher(Teacher teacher);
}