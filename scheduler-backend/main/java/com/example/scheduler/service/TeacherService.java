package com.example.scheduler.service;

import com.example.scheduler.dto.TeacherCreateRequest;
import com.example.scheduler.dto.TeacherResponse;
import com.example.scheduler.model.ScheduleItem;
import com.example.scheduler.model.Teacher;
import com.example.scheduler.repository.ScheduleItemRepository;
import com.example.scheduler.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    @Autowired private TeacherRepository teacherRepo;
    @Autowired private ScheduleItemRepository scheduleRepo;
    @Autowired private SchedulerService schedulerService;

    public List<TeacherResponse> getAllTeachers() {
        return teacherRepo.findAll().stream()
                .map(TeacherResponse::new)
                .collect(Collectors.toList());
    }

    public TeacherResponse register(TeacherCreateRequest req) {
        if (teacherRepo.findByName(req.getName()) != null) {
            throw new IllegalArgumentException("使用者名稱「" + req.getName() + "」已存在");
        }
        Teacher teacher = new Teacher(req.getName(), req.getPassword(), req.getGrade(), req.getType());
        return new TeacherResponse(teacherRepo.save(teacher));
    }

    public TeacherResponse login(TeacherCreateRequest req) {
        Teacher teacher = teacherRepo.findByName(req.getName());
        if (teacher == null || !teacher.getPassword().equals(req.getPassword())) {
            throw new IllegalArgumentException("帳號或密碼錯誤");
        }
        return new TeacherResponse(teacher);
    }

    public TeacherResponse updateGrade(Long id, Integer grade) {
        Teacher teacher = findOrThrow(id);
        teacher.setGrade(grade);
        return new TeacherResponse(teacherRepo.save(teacher));
    }

    public TeacherResponse updateType(Long id, String type) {
        Teacher teacher = findOrThrow(id);
        teacher.setType(type);
        return new TeacherResponse(teacherRepo.save(teacher));
    }

    public Map<String, Object> autoSchedule(Long id) {
        Teacher teacher = findOrThrow(id);
        Integer grade = teacher.getGrade();
        if (grade == null) {
            throw new IllegalArgumentException("教師尚未設定年級，無法排課");
        }
        return schedulerService.autoSchedule(id, grade);
    }

    public List<ScheduleItem> getMySchedule(Long id) {
        return scheduleRepo.findByTeacher_Id(id);
    }

    public List<ScheduleItem> getGradeSchedule(Integer grade) {
        return scheduleRepo.findByTargetGrade(grade);
    }

    @Transactional
    public List<ScheduleItem> saveSchedule(Long id, List<ScheduleItem> items) {
        Teacher teacher = findOrThrow(id);
        scheduleRepo.deleteByTeacher(teacher);
        items.forEach(item -> item.setTeacher(teacher));
        return scheduleRepo.saveAll(items);
    }

    private Teacher findOrThrow(Long id) {
        return teacherRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到 id=" + id + " 的教師"));
    }
}
