package com.example.scheduler.service;

import com.example.scheduler.model.*;
import com.example.scheduler.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SchedulerService {

    @Autowired private TeacherRepository teacherRepo;
    @Autowired private CourseRequirementRepository courseRepo;
    @Autowired private TeacherAvailabilityRepository availabilityRepo;
    @Autowired private ScheduleItemRepository scheduleRepo;

    // --- 1. [設定條件區] 科目屬性設定 ---
    private static final Set<String> MORNING_SUBJECTS = Set.of("國文", "數學", "英文", "社會", "自然");
    private static final Set<String> AFTERNOON_SUBJECTS = Set.of("體育", "美術", "音樂", "綜合", "電腦");

    /**
     * [超級功能] 年級總排課指揮官 (先排科任 -> 後排班導)
     */
    @Transactional
    public Map<String, Object> autoScheduleGrade(Integer grade) {
        Map<String, Object> finalResult = new HashMap<>();
        List<String> allConflicts = new ArrayList<>();

        // 1. 清空該年級舊課表
        scheduleRepo.deleteByTargetGrade(grade);

        // 2. 撈出該年級所有老師
        List<Teacher> allTeachers = teacherRepo.findAll().stream()
                .filter(t -> grade.equals(t.getGrade()))
                .collect(Collectors.toList());

        // 3. 分組：科任 (SUBJECT) vs 班導 (HOMEROOM)
        List<Teacher> subjectTeachers = new ArrayList<>();
        List<Teacher> homeroomTeachers = new ArrayList<>();

        for (Teacher t : allTeachers) {
            if ("SUBJECT".equalsIgnoreCase(t.getType())) {
                subjectTeachers.add(t);
            } else {
                homeroomTeachers.add(t);
            }
        }

        // 4. 第一階段：科任老師 (大石頭)
        Collections.shuffle(subjectTeachers);
        for (Teacher t : subjectTeachers) {
            Map<String, Object> res = autoSchedule(t.getId(), grade);
            List<String> conflicts = (List<String>) res.get("conflicts");
            if (conflicts != null && !conflicts.isEmpty()) {
                allConflicts.add("❌ 科任 [" + t.getName() + "] 衝突: " + conflicts);
            }
        }

        // 5. 第二階段：班導師 (填沙子)
        for (Teacher t : homeroomTeachers) {
            Map<String, Object> res = autoSchedule(t.getId(), grade);
            List<String> conflicts = (List<String>) res.get("conflicts");
            if (conflicts != null && !conflicts.isEmpty()) {
                allConflicts.add("⚠️ 班導 [" + t.getName() + "] 缺課: " + conflicts);
            }
        }

        // 6. 回傳報告
        if (allConflicts.isEmpty()) {
            finalResult.put("status", "success");
            finalResult.put("message", "完美！" + grade + " 年級所有課程已排定。");
        } else {
            finalResult.put("status", "partial");
            finalResult.put("message", "排課完成，但有部分衝突，請手動調整。");
            finalResult.put("conflicts", allConflicts);
        }
        return finalResult;
    }

    /**
     * 單一老師排課 (核心演算法)
     * 支援 targetGrade 寫入
     */
    @Transactional
    public Map<String, Object> autoSchedule(Long teacherId, Integer targetGrade) {
        Teacher teacher = teacherRepo.findById(teacherId).orElseThrow();

        // 🔥 [修正重點] 先清除該老師在該年級的舊課表，避免重複疊加！
        scheduleRepo.deleteByTeacherAndTargetGrade(teacher, targetGrade);

        // 準備資料
        List<CourseRequirement> courses = courseRepo.findByTeacher(teacher);
        List<TeacherAvailability> myBusy = availabilityRepo.findByTeacher(teacher);
        
        // 找出該年級已經被「其他人」佔用的時間
        List<ScheduleItem> gradeSchedule = scheduleRepo.findByTargetGrade(targetGrade);

        // 初始化佔用表
        boolean[][] occupied = new boolean[6][9]; 
        
        // 1. 標記老師自己的忙碌時間
        for (TeacherAvailability busy : myBusy) {
            occupied[busy.getDayOfWeek()][busy.getPeriod()] = true;
        }

        // 2. 標記該年級已經被佔用的時間
        for (ScheduleItem item : gradeSchedule) {
            // 只要不是我自己的課 (雖然前面刪過了，但以防萬一)，都視為佔用
            if (!item.getTeacher().getId().equals(teacherId)) {
                occupied[item.getDayOfWeek()][item.getPeriod()] = true;
            }
        }

        // 找出所有空位
        List<int[]> allSlots = new ArrayList<>();
        for (int d = 1; d <= 5; d++) {
            for (int p = 1; p <= 8; p++) {
                if (!occupied[d][p]) allSlots.add(new int[]{d, p});
            }
        }

        // 排序課程需求 (優先排主科、節數多的)
        courses.sort((c1, c2) -> {
            boolean p1 = MORNING_SUBJECTS.contains(c1.getSubject());
            boolean p2 = MORNING_SUBJECTS.contains(c2.getSubject());
            if (p1 && !p2) return -1;
            if (!p1 && p2) return 1;
            return c2.getSessions().compareTo(c1.getSessions());
        });

        List<ScheduleItem> newSchedule = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();
        
        // 虛擬佔用表
        boolean[][] tempOccupied = new boolean[6][9];
        for(int i=0; i<6; i++) System.arraycopy(occupied[i], 0, tempOccupied[i], 0, 9);
        
        Random random = new Random();

        // --- 開始填空 ---
        for (CourseRequirement req : courses) {
            String subject = req.getSubject();
            int sessionsNeeded = req.getSessions();

            for (int i = 0; i < sessionsNeeded; i++) {
                int[] bestSlot = null;
                int maxScore = Integer.MIN_VALUE;

                Collections.shuffle(allSlots, random);

                for (int[] slot : allSlots) {
                    int d = slot[0];
                    int p = slot[1];

                    if (tempOccupied[d][p]) continue;

                    int score = calculateScore(d, p, subject, newSchedule);

                    if (score > maxScore) {
                        maxScore = score;
                        bestSlot = slot;
                    }
                }

                if (bestSlot != null) {
                    int d = bestSlot[0];
                    int p = bestSlot[1];
                    
                    ScheduleItem item = new ScheduleItem(d, p, subject, teacher, targetGrade);
                    newSchedule.add(item);
                    tempOccupied[d][p] = true;
                } else {
                    conflicts.add(subject);
                }
            }
        }

        List<ScheduleItem> saved = scheduleRepo.saveAll(newSchedule);

        Map<String, Object> result = new HashMap<>();
        result.put("schedule", saved);
        result.put("conflicts", conflicts);
        return result;
    }

    private int calculateScore(int day, int period, String subject, List<ScheduleItem> currentSchedule) {
        int score = 0;

        if (MORNING_SUBJECTS.contains(subject)) {
            if (period <= 4) score += 20;
            else score -= 10;
        }

        if (AFTERNOON_SUBJECTS.contains(subject)) {
            if (period >= 5 || period == 4) score += 10;
        }

        long countToday = currentSchedule.stream()
                .filter(item -> item.getDayOfWeek() == day && item.getSubject().equals(subject))
                .count();
        if (countToday > 0) {
            score -= 100;
        }

        score += new Random().nextInt(5);

        return score;
    }
}