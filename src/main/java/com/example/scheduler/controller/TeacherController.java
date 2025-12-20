package com.example.scheduler.controller;

import com.example.scheduler.model.CourseRequirement;
import com.example.scheduler.model.Teacher;
import com.example.scheduler.repository.CourseRequirementRepository;
import com.example.scheduler.repository.TeacherRepository;
import com.example.scheduler.model.TeacherAvailability; // 新增
import com.example.scheduler.repository.TeacherAvailabilityRepository;
import com.example.scheduler.model.ScheduleItem; // 新增
import com.example.scheduler.repository.ScheduleItemRepository; // 新增

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherRepository teacherRepository;

    // 👇 你的錯誤是因為少了這兩行！請補上 👇
    @Autowired
    private CourseRequirementRepository courseRepo; 
    // 👆 必須宣告這個變數，下面的程式碼才能使用 courseRepo

    @Autowired
    private TeacherAvailabilityRepository availabilityRepo;

    @Autowired
    private ScheduleItemRepository scheduleRepo;

    // 1. 取得所有老師
    @GetMapping
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    // 2. 註冊/新增老師
    @PostMapping("/register")
    public Teacher register(@RequestBody Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    // 3. 登入驗證
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

    // 4. 設定年級
    @PostMapping("/{id}/grade")
    public Teacher updateGrade(@PathVariable Long id, @RequestBody Integer grade) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        teacher.setGrade(grade);
        return teacherRepository.save(teacher);
    }

    // 5. 設定教師類型
    @PostMapping("/{id}/type")
    public Teacher updateType(@PathVariable Long id, @RequestBody String type) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        teacher.setType(type);
        return teacherRepository.save(teacher);
    }

    // 6. 設定課程需求
    @PostMapping("/{id}/courses")
    public List<CourseRequirement> updateCourses(@PathVariable Long id, @RequestBody Map<String, Integer> courses) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();

        // 這裡使用了 courseRepo，如果上面沒宣告就會報錯
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
    
    // 7. 取得課程設定
    @GetMapping("/{id}/courses")
    public List<CourseRequirement> getCourses(@PathVariable Long id) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        return courseRepo.findByTeacher(teacher);
    }

    // [新增] 8. 步驟四：設定不排課時段
    // 接收格式: [{"dayOfWeek": 1, "period": 1}, {"dayOfWeek": 5, "period": 8}, ...]
    @PostMapping("/{id}/availability")
    public List<TeacherAvailability> updateAvailability(@PathVariable Long id, @RequestBody List<TeacherAvailability> busySlots) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();

        // 1. 清空舊設定 (全重設)
        availabilityRepo.deleteByTeacher(teacher);

        // 2. 儲存新的 "忙碌" 時段
        for (TeacherAvailability slot : busySlots) {
            slot.setTeacher(teacher);
            availabilityRepo.save(slot);
        }

        return availabilityRepo.findByTeacher(teacher);
    }

    // [新增] 9. 取得不排課時段
    @GetMapping("/{id}/availability")
    public List<TeacherAvailability> getAvailability(@PathVariable Long id) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        return availabilityRepo.findByTeacher(teacher);
    }

    // [新增] 10. 儲存最終排課結果
    @PostMapping("/{id}/schedule")
    public List<ScheduleItem> saveSchedule(@PathVariable Long id, @RequestBody List<ScheduleItem> items) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        
        // 先清除舊的排課結果
        scheduleRepo.deleteByTeacher(teacher);

        for (ScheduleItem item : items) {
            item.setTeacher(teacher);
            scheduleRepo.save(item);
        }
        return scheduleRepo.findByTeacher(teacher);
    }

    // [新增] 11. 取得排課結果
    @GetMapping("/{id}/schedule")
    public List<ScheduleItem> getSchedule(@PathVariable Long id) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        return scheduleRepo.findByTeacher(teacher);
    }
}