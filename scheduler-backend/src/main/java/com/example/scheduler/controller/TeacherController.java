package com.example.scheduler.controller;

import com.example.scheduler.common.ApiResponse;
import com.example.scheduler.dto.TeacherCreateRequest;
import com.example.scheduler.dto.TeacherResponse;
import com.example.scheduler.model.ScheduleItem;
import com.example.scheduler.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    // -------------------------------------------------------
    // 教師基礎 CRUD
    // -------------------------------------------------------

    /** 取得所有教師列表 */
    @GetMapping
    public ApiResponse<List<TeacherResponse>> getAllTeachers() {
        return ApiResponse.success(teacherService.getAllTeachers());
    }

    /** 教師註冊 */
    @PostMapping("/register")
    public ApiResponse<TeacherResponse> register(@RequestBody TeacherCreateRequest req) {
        return ApiResponse.success("註冊成功", teacherService.register(req));
    }

    /** 教師登入 */
    @PostMapping("/login")
    public ApiResponse<TeacherResponse> login(@RequestBody TeacherCreateRequest req) {
        return ApiResponse.success("登入成功", teacherService.login(req));
    }

    /** 更新年級（班導師用） */
    @PostMapping("/{id}/grade")
    public ApiResponse<TeacherResponse> updateGrade(@PathVariable Long id,
                                                    @RequestBody Integer grade) {
        return ApiResponse.success(teacherService.updateGrade(id, grade));
    }

    /** 更新教師類型（HOMEROOM / SUBJECT） */
    @PostMapping("/{id}/type")
    public ApiResponse<TeacherResponse> updateType(@PathVariable Long id,
                                                   @RequestBody String type) {
        return ApiResponse.success(teacherService.updateType(id, type));
    }

    // -------------------------------------------------------
    // 排課功能
    // -------------------------------------------------------

    /** 觸發單一教師自動排課 */
    @PostMapping("/{id}/auto-schedule")
    public ApiResponse<Map<String, Object>> autoSchedule(@PathVariable Long id) {
        return ApiResponse.success(teacherService.autoSchedule(id));
    }

    /** 取得個人課表 */
    @GetMapping("/{id}/schedule")
    public ApiResponse<List<ScheduleItem>> getMySchedule(@PathVariable Long id) {
        return ApiResponse.success(teacherService.getMySchedule(id));
    }

    /** 取得年級課表 */
    @GetMapping("/grade/{grade}/schedule")
    public ApiResponse<List<ScheduleItem>> getGradeSchedule(@PathVariable Integer grade) {
        return ApiResponse.success(teacherService.getGradeSchedule(grade));
    }

    /** 手動儲存課表 */
    @PostMapping("/{id}/schedule")
    public ApiResponse<List<ScheduleItem>> saveSchedule(@PathVariable Long id,
                                                        @RequestBody List<ScheduleItem> items) {
        return ApiResponse.success(teacherService.saveSchedule(id, items));
    }
}
