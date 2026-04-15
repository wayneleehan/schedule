package com.example.scheduler.controller;

import com.example.scheduler.common.ApiResponse;
import com.example.scheduler.dto.CourseRequirementRequest;
import com.example.scheduler.dto.CourseRequirementResponse;
import com.example.scheduler.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teachers/{teacherId}/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /** 取得該教師所有課程需求 */
    @GetMapping
    public ApiResponse<List<CourseRequirementResponse>> getCourses(@PathVariable Long teacherId) {
        return ApiResponse.success(courseService.getCourses(teacherId));
    }

    /**
     * 覆蓋式更新課程需求
     * 前端傳入完整清單，後端清空舊資料後重新寫入
     */
    @PutMapping
    public ApiResponse<List<CourseRequirementResponse>> updateCourses(
            @PathVariable Long teacherId,
            @RequestBody List<CourseRequirementRequest> requests) {
        return ApiResponse.success(courseService.updateCourses(teacherId, requests));
    }

    /** 新增單筆課程需求 */
    @PostMapping
    public ApiResponse<CourseRequirementResponse> addCourse(
            @PathVariable Long teacherId,
            @RequestBody CourseRequirementRequest req) {
        return ApiResponse.success("課程需求新增成功", courseService.addCourse(teacherId, req));
    }

    /** 更新單筆課程需求 */
    @PutMapping("/{courseId}")
    public ApiResponse<CourseRequirementResponse> updateCourse(
            @PathVariable Long teacherId,
            @PathVariable Long courseId,
            @RequestBody CourseRequirementRequest req) {
        return ApiResponse.success(courseService.updateCourse(teacherId, courseId, req));
    }

    /** 刪除單筆課程需求 */
    @DeleteMapping("/{courseId}")
    public ApiResponse<Void> deleteCourse(@PathVariable Long teacherId,
                                          @PathVariable Long courseId) {
        courseService.deleteCourse(teacherId, courseId);
        return ApiResponse.success("刪除成功", null);
    }
}
