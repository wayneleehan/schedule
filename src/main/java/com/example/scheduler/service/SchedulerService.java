package com.example.scheduler.service;

import com.example.scheduler.model.*;
import com.example.scheduler.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SchedulerService {

    @Autowired private TeacherRepository teacherRepo;
    @Autowired private CourseRequirementRepository courseRepo;
    @Autowired private TeacherAvailabilityRepository availabilityRepo;
    @Autowired private ScheduleItemRepository scheduleRepo;

    private static final List<String> HIGH_PRIORITY_SUBJECTS = List.of(
        "音樂", "體育", "美術", "自然", "社會", "數學", "電腦"
    );

    /**
     * 自動排課 (盡力版)
     * 回傳 Map:
     * - "schedule": List<ScheduleItem> (成功的課)
     * - "conflicts": List<String> (失敗的科目名稱)
     */
    @Transactional
    public Map<String, Object> autoSchedule(Long teacherId) {
        Teacher teacher = teacherRepo.findById(teacherId).orElseThrow();
        Integer grade = teacher.getGrade();

        // 1. 準備資料
        List<CourseRequirement> courses = courseRepo.findByTeacher(teacher);
        List<TeacherAvailability> myBusy = availabilityRepo.findByTeacher(teacher);
        List<ScheduleItem> gradeSchedule = scheduleRepo.findByTeacher_Grade(grade);

        // 2. 初始化佔用表
        boolean[][] occupied = new boolean[6][9]; 
        for (TeacherAvailability busy : myBusy) {
            occupied[busy.getDayOfWeek()][busy.getPeriod()] = true;
        }

        for (ScheduleItem item : gradeSchedule) {
            if (!item.getTeacher().getId().equals(teacherId)) {
                occupied[item.getDayOfWeek()][item.getPeriod()] = true;
            }
        }

        // 3. 準備隨機時段
        List<int[]> allSlots = new ArrayList<>();
        for (int d = 1; d <= 5; d++) {
            for (int p = 1; p <= 8; p++) {
                if (!occupied[d][p]) allSlots.add(new int[]{d, p});
            }
        }
        Collections.shuffle(allSlots); // 洗牌

        // 4. 排序 (大石頭優先)
        courses.sort((c1, c2) -> {
            boolean p1 = HIGH_PRIORITY_SUBJECTS.contains(c1.getSubject());
            boolean p2 = HIGH_PRIORITY_SUBJECTS.contains(c2.getSubject());
            if (p1 && !p2) return -1;
            if (!p1 && p2) return 1;
            return c2.getSessions().compareTo(c1.getSessions());
        });

        List<ScheduleItem> newSchedule = new ArrayList<>();
        List<String> conflicts = new ArrayList<>(); // 記錄失敗的科目
        boolean[][] tempOccupied = new boolean[6][9];
        for(int i=0; i<6; i++) System.arraycopy(occupied[i], 0, tempOccupied[i], 0, 9);

        // 5. 開始填空
        for (CourseRequirement req : courses) {
            String subject = req.getSubject();
            int sessionsNeeded = req.getSessions();

            for (int i = 0; i < sessionsNeeded; i++) {
                boolean placed = false;
                
                for (int[] slot : allSlots) {
                    int d = slot[0];
                    int p = slot[1];
                    if (!tempOccupied[d][p]) {
                        ScheduleItem item = new ScheduleItem();
                        item.setDayOfWeek(d);
                        item.setPeriod(p);
                        item.setSubject(subject);
                        item.setTeacher(teacher);
                        
                        newSchedule.add(item);
                        tempOccupied[d][p] = true;
                        placed = true;
                        break;
                    }
                }
                
                if (!placed) {
                    // 🔥 如果排不進去，不要拋出例外，而是加入衝突清單
                    conflicts.add(subject);
                }
            }
        }

        // 6. 存檔 (只存成功的)
        scheduleRepo.deleteByTeacher(teacher);
        List<ScheduleItem> saved = scheduleRepo.saveAll(newSchedule);

        // 7. 回傳結果
        Map<String, Object> result = new HashMap<>();
        result.put("schedule", saved);
        result.put("conflicts", conflicts);
        return result;
    }
}