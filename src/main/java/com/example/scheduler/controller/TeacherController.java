package com.example.scheduler.controller;

import com.example.scheduler.model.CourseRequirement;
import com.example.scheduler.model.ScheduleItem;
import com.example.scheduler.model.Teacher;
import com.example.scheduler.model.TeacherAvailability;
import com.example.scheduler.repository.CourseRequirementRepository;
import com.example.scheduler.repository.ScheduleItemRepository;
import com.example.scheduler.repository.TeacherAvailabilityRepository;
import com.example.scheduler.repository.TeacherRepository;
import com.example.scheduler.service.SchedulerService; // 引入排課服務

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    // [注入] 基礎資料庫操作 Repository
    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRequirementRepository courseRepo;

    @Autowired
    private TeacherAvailabilityRepository availabilityRepo;

    @Autowired
    private ScheduleItemRepository scheduleRepo;

    // [注入] 核心排課邏輯服務
    @Autowired
    private SchedulerService schedulerService;

    // ---------------------------------------------------------
    // 1. 自動排課功能
    // ---------------------------------------------------------

    /**
     * [功能] 觸發自動排課
     * [說明] 呼叫 Service 執行複雜演算法，會清除舊課表並產生新課表
     */
    @PostMapping("/{id}/auto-schedule")
    public Map<String, Object> autoSchedule(@PathVariable Long id) {
        // 現在回傳的是 Map (包含課表 + 衝突清單)
        return schedulerService.autoSchedule(id);
    }

    // ---------------------------------------------------------
    // 2. 查詢課表功能 (修復衝突部分)
    // ---------------------------------------------------------
    
    /**
     * [功能] 取得「個人」課表
     * [說明] 給科任老師看自己的跑班行程 (修復：已移除重複的 mapping)
     */
    @GetMapping("/{id}/schedule")
    public List<ScheduleItem> getMySchedule(@PathVariable Long id) {
        return scheduleRepo.findByTeacher_Id(id); 
    }

    /**
     * [功能] 取得「年級」總課表
     * [說明] 給班導師看該年級所有課程 (包含自己 + 科任)
     */
    @GetMapping("/grade/{grade}/schedule")
    public List<ScheduleItem> getGradeSchedule(@PathVariable Integer grade) {
        return scheduleRepo.findByTeacher_Grade(grade);
    }

    // ---------------------------------------------------------
    // 3. 基礎 CRUD 功能 (登入、註冊、設定)
    // ---------------------------------------------------------

    /**
     * [功能] 取得所有老師列表
     */
    @GetMapping
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    /**
     * [功能] 老師註冊
     */
    @PostMapping("/register")
    public Teacher register(@RequestBody Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    /**
     * [功能] 老師登入驗證
     */
    @PostMapping("/login")
    public Teacher login(@RequestBody Teacher loginRequest) {
        Teacher teacher = teacherRepository.findByName(loginRequest.getName());
        if (teacher != null && 
            teacher.getPassword() != null && 
            teacher.getPassword().equals(loginRequest.getPassword())) {
            return teacher;
        }
        return null;
    }

    /**
     * [功能] 設定/更新年級 (用於班導)
     */
    @PostMapping("/{id}/grade")
    public Teacher updateGrade(@PathVariable Long id, @RequestBody Integer grade) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        teacher.setGrade(grade);
        return teacherRepository.save(teacher);
    }
    
    /**
     * [功能] 設定/更新教師類型 (班導/科任)
     */
    @PostMapping("/{id}/type")
    public Teacher updateType(@PathVariable Long id, @RequestBody String type) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        teacher.setType(type);
        return teacherRepository.save(teacher);
    }

    /**
     * [功能] 設定課程需求 (要上什麼課、幾節)
     */
    @PostMapping("/{id}/courses")
    public List<CourseRequirement> updateCourses(@PathVariable Long id, @RequestBody Map<String, Integer> courses) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        courseRepo.deleteByTeacher(teacher);

        for (Map.Entry<String, Integer> entry : courses.entrySet()) {
            String subject = entry.getKey();
            Integer count = entry.getValue();
            if (count != null && count > 0) {
                CourseRequirement req = new CourseRequirement(subject, count, teacher);
                courseRepo.save(req);
            }
        }
        return courseRepo.findByTeacher(teacher);
    }
    
    /**
     * [功能] 取得已設定的課程需求
     */
    @GetMapping("/{id}/courses")
    public List<CourseRequirement> getCourses(@PathVariable Long id) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        return courseRepo.findByTeacher(teacher);
    }

    /**
     * [功能] 設定不排課時段 (忙碌時間)
     */
    @PostMapping("/{id}/availability")
    public List<TeacherAvailability> updateAvailability(@PathVariable Long id, @RequestBody List<TeacherAvailability> busySlots) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        availabilityRepo.deleteByTeacher(teacher);
        for (TeacherAvailability slot : busySlots) {
            slot.setTeacher(teacher);
            availabilityRepo.save(slot);
        }
        return availabilityRepo.findByTeacher(teacher);
    }

    /**
     * [功能] 取得已設定的不排課時段
     */
    @GetMapping("/{id}/availability")
    public List<TeacherAvailability> getAvailability(@PathVariable Long id) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        return availabilityRepo.findByTeacher(teacher);
    }

    /**
     * [功能] 手動儲存排課結果
     * [說明] 用於前端「手動調整」後的回寫，與自動排課不衝突
     */
    @PostMapping("/{id}/schedule")
    public List<ScheduleItem> saveSchedule(@PathVariable Long id, @RequestBody List<ScheduleItem> items) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        scheduleRepo.deleteByTeacher(teacher);
        for (ScheduleItem item : items) {
            item.setTeacher(teacher);
            scheduleRepo.save(item);
        }
        return scheduleRepo.findByTeacher(teacher);
    }
}