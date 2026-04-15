package com.example.scheduler.service;

import com.example.scheduler.dto.CourseRequirementRequest;
import com.example.scheduler.dto.CourseRequirementResponse;
import com.example.scheduler.model.CourseRequirement;
import com.example.scheduler.model.Teacher;
import com.example.scheduler.repository.CourseRequirementRepository;
import com.example.scheduler.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired private TeacherRepository teacherRepo;
    @Autowired private CourseRequirementRepository courseRepo;

    /** 取得該教師所有課程需求 */
    public List<CourseRequirementResponse> getCourses(Long teacherId) {
        Teacher teacher = findTeacherOrThrow(teacherId);
        return courseRepo.findByTeacher(teacher).stream()
                .map(CourseRequirementResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 覆蓋式更新課程需求（先清空舊的，再寫入新的）
     * 忽略 sessions <= 0 的項目
     */
    @Transactional
    public List<CourseRequirementResponse> updateCourses(Long teacherId,
                                                         List<CourseRequirementRequest> requests) {
        Teacher teacher = findTeacherOrThrow(teacherId);
        courseRepo.deleteByTeacher(teacher);

        List<CourseRequirement> newCourses = requests.stream()
                .filter(r -> r.getSubject() != null && !r.getSubject().isBlank()
                             && r.getSessions() != null && r.getSessions() > 0)
                .map(r -> new CourseRequirement(r.getSubject(), r.getSessions(), teacher))
                .collect(Collectors.toList());

        return courseRepo.saveAll(newCourses).stream()
                .map(CourseRequirementResponse::new)
                .collect(Collectors.toList());
    }

    /** 新增單筆課程需求 */
    public CourseRequirementResponse addCourse(Long teacherId, CourseRequirementRequest req) {
        Teacher teacher = findTeacherOrThrow(teacherId);
        CourseRequirement course = new CourseRequirement(req.getSubject(), req.getSessions(), teacher);
        return new CourseRequirementResponse(courseRepo.save(course));
    }

    /** 更新單筆課程需求 */
    public CourseRequirementResponse updateCourse(Long teacherId, Long courseId,
                                                   CourseRequirementRequest req) {
        CourseRequirement course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("找不到 id=" + courseId + " 的課程需求"));
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("此課程需求不屬於該教師");
        }
        course.setSubject(req.getSubject());
        course.setSessions(req.getSessions());
        return new CourseRequirementResponse(courseRepo.save(course));
    }

    /** 刪除單筆課程需求 */
    @Transactional
    public void deleteCourse(Long teacherId, Long courseId) {
        CourseRequirement course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("找不到 id=" + courseId + " 的課程需求"));
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("此課程需求不屬於該教師");
        }
        courseRepo.deleteById(courseId);
    }

    private Teacher findTeacherOrThrow(Long id) {
        return teacherRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到 id=" + id + " 的教師"));
    }
}
