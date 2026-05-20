package com.example.scheduler.controller;

import com.example.scheduler.common.ApiResponse;
import com.example.scheduler.dto.AvailabilityRequest;
import com.example.scheduler.dto.AvailabilityResponse;
import com.example.scheduler.service.AvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teachers/{teacherId}/availability")
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    /** 取得該教師的忙碌時段 */
    @GetMapping
    public ApiResponse<List<AvailabilityResponse>> getAvailability(@PathVariable Long teacherId) {
        return ApiResponse.success(availabilityService.getAvailability(teacherId));
    }

    /**
     * 覆蓋式更新忙碌時段
     * 前端傳入完整清單，後端清空後重寫
     */
    @PutMapping
    public ApiResponse<List<AvailabilityResponse>> updateAvailability(
            @PathVariable Long teacherId,
            @RequestBody List<AvailabilityRequest> requests) {
        return ApiResponse.success(availabilityService.updateAvailability(teacherId, requests));
    }

    /** 新增單筆忙碌時段 */
    @PostMapping
    public ApiResponse<AvailabilityResponse> addSlot(
            @PathVariable Long teacherId,
            @RequestBody AvailabilityRequest req) {
        return ApiResponse.success("時段新增成功", availabilityService.addSlot(teacherId, req));
    }

    /** 刪除單筆忙碌時段 */
    @DeleteMapping("/{slotId}")
    public ApiResponse<Void> deleteSlot(@PathVariable Long teacherId,
                                         @PathVariable Long slotId) {
        availabilityService.deleteSlot(teacherId, slotId);
        return ApiResponse.success("刪除成功", null);
    }

    /** 清空所有忙碌時段 */
    @DeleteMapping
    public ApiResponse<Void> clearAvailability(@PathVariable Long teacherId) {
        availabilityService.clearAvailability(teacherId);
        return ApiResponse.success("已清空所有忙碌時段", null);
    }
}
