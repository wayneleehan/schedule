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
    
    // 透過 Teacher 的 ID 查詢
    List<ScheduleItem> findByTeacher_Id(Long teacherId);
    
    // 查詢某個年級的所有課表
    List<ScheduleItem> findByTeacher_Grade(Integer grade);

    // 查詢目標年級的所有課表 (用於排課時避開衝突)
    List<ScheduleItem> findByTargetGrade(Integer targetGrade);
    
    // 刪除該年級的所有課表 (用於總指揮官重排)
    @Transactional
    void deleteByTargetGrade(Integer targetGrade);
    
    // 刪除該老師的所有課表
    @Transactional
    void deleteByTeacher(Teacher teacher);

    // 🔥 [新增] 刪除特定老師在特定年級的課表 (用於防止單人排課時產生重複數據)
    @Transactional
    void deleteByTeacherAndTargetGrade(Teacher teacher, Integer targetGrade);
}