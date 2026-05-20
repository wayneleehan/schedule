package com.example.scheduler.service;

import com.example.scheduler.dto.AvailabilityRequest;
import com.example.scheduler.dto.AvailabilityResponse;
import com.example.scheduler.model.Teacher;
import com.example.scheduler.model.TeacherAvailability;
import com.example.scheduler.repository.TeacherAvailabilityRepository;
import com.example.scheduler.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    @Autowired private TeacherRepository teacherRepo;
    @Autowired private TeacherAvailabilityRepository availabilityRepo;

    /** 取得該教師的忙碌時段 */
    public List<AvailabilityResponse> getAvailability(Long teacherId) {
        Teacher teacher = findTeacherOrThrow(teacherId);
        return availabilityRepo.findByTeacher(teacher).stream()
                .map(AvailabilityResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 覆蓋式更新忙碌時段（先清空，再寫入）
     */
    @Transactional
    public List<AvailabilityResponse> updateAvailability(Long teacherId,
                                                          List<AvailabilityRequest> requests) {
        Teacher teacher = findTeacherOrThrow(teacherId);
        availabilityRepo.deleteByTeacher(teacher);

        List<TeacherAvailability> slots = requests.stream()
                .filter(r -> r.getDayOfWeek() != null && r.getPeriod() != null)
                .map(r -> new TeacherAvailability(r.getDayOfWeek(), r.getPeriod(), teacher))
                .collect(Collectors.toList());

        return availabilityRepo.saveAll(slots).stream()
                .map(AvailabilityResponse::new)
                .collect(Collectors.toList());
    }

    /** 新增單筆忙碌時段 */
    public AvailabilityResponse addSlot(Long teacherId, AvailabilityRequest req) {
        Teacher teacher = findTeacherOrThrow(teacherId);
        TeacherAvailability slot = new TeacherAvailability(req.getDayOfWeek(), req.getPeriod(), teacher);
        return new AvailabilityResponse(availabilityRepo.save(slot));
    }

    /** 刪除單筆忙碌時段 */
    @Transactional
    public void deleteSlot(Long teacherId, Long slotId) {
        TeacherAvailability slot = availabilityRepo.findById(slotId)
                .orElseThrow(() -> new RuntimeException("找不到 id=" + slotId + " 的時段設定"));
        if (!slot.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("此時段設定不屬於該教師");
        }
        availabilityRepo.deleteById(slotId);
    }

    /** 清空該教師所有忙碌時段 */
    @Transactional
    public void clearAvailability(Long teacherId) {
        Teacher teacher = findTeacherOrThrow(teacherId);
        availabilityRepo.deleteByTeacher(teacher);
    }

    private Teacher findTeacherOrThrow(Long id) {
        return teacherRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到 id=" + id + " 的教師"));
    }
}
