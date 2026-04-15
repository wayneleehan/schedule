# Backend 規範 — Spring Boot

> 放置於 `scheduler/scheduler-backend/CLAUDE.md`

## 套件結構（目標架構）
```
com.example.scheduler/
├── SchedulerApplication.java
├── controller/
│   ├── TeacherController.java        ✅ 已存在
│   ├── CourseController.java         ❌ 待建立
│   ├── ScheduleController.java       ❌ 待建立
│   └── AvailabilityController.java   ❌ 待建立
├── service/
│   ├── TeacherService.java           ❌ 待建立（目前只有 SchedulerService）
│   ├── CourseService.java            ❌ 待建立
│   ├── SchedulerService.java         ✅ 已存在（核心排課邏輯）
│   └── AvailabilityService.java      ❌ 待建立
├── repository/
│   ├── TeacherRepository.java        ✅ 已存在
│   ├── CourseRequirementRepository.java ✅ 已存在
│   ├── ScheduleItemRepository.java   ✅ 已存在
│   └── TeacherAvailabilityRepository.java ✅ 已存在
├── model/  (Entity)
│   ├── Teacher.java                  ✅ 已存在
│   ├── CourseRequirement.java        ✅ 已存在
│   ├── ScheduleItem.java             ✅ 已存在
│   └── TeacherAvailability.java      ✅ 已存在
├── dto/                              ❌ 整個目錄待建立
│   ├── request/
│   │   ├── TeacherCreateRequest.java
│   │   ├── CourseRequirementRequest.java
│   │   └── AvailabilityRequest.java
│   └── response/
│       ├── TeacherResponse.java
│       ├── ScheduleItemResponse.java
│       └── ApiResponse.java          ← 統一回應包裝
├── exception/                        ❌ 待建立
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── ScheduleConflictException.java
└── config/                           ❌ 待建立
    └── CorsConfig.java
```

## 分層規範（嚴格遵守）
```
Controller → Service → Repository
```
- **Controller**：只做請求驗證、呼叫 Service、回傳 ApiResponse
- **Service**：業務邏輯、呼叫 Repository、Entity ↔ DTO 轉換
- **Repository**：只做資料存取，可加自訂 JPQL
- **禁止**：Controller 直接呼叫 Repository、Entity 直接回傳給前端

## 統一回應格式

```java
// ApiResponse.java
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "success", null);
    }

    public static ApiResponse<Void> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

## Controller 範本
```java
@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public ApiResponse<List<TeacherResponse>> getAllTeachers() {
        return ApiResponse.success(teacherService.findAll());
    }

    @PostMapping
    public ApiResponse<TeacherResponse> createTeacher(@RequestBody @Valid TeacherCreateRequest req) {
        return ApiResponse.success(teacherService.create(req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.delete(id);
        return ApiResponse.success();
    }
}
```

## 例外處理範本
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResponse<Void> handleNotFound(ResourceNotFoundException ex) {
        return ApiResponse.error(404, ex.getMessage());
    }

    @ExceptionHandler(ScheduleConflictException.class)
    public ApiResponse<Void> handleConflict(ScheduleConflictException ex) {
        return ApiResponse.error(409, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGeneral(Exception ex) {
        return ApiResponse.error(500, "系統錯誤：" + ex.getMessage());
    }
}
```

## API 端點規劃
| Method | URL | 功能 |
|--------|-----|------|
| GET | /api/v1/teachers | 取得所有教師 |
| POST | /api/v1/teachers | 新增教師 |
| PUT | /api/v1/teachers/{id} | 更新教師 |
| DELETE | /api/v1/teachers/{id} | 刪除教師 |
| GET | /api/v1/teachers/{id}/availability | 取得教師可用時段 |
| POST | /api/v1/teachers/{id}/availability | 設定可用時段 |
| GET | /api/v1/courses | 取得課程需求列表 |
| POST | /api/v1/courses | 新增課程需求 |
| POST | /api/v1/schedule/generate | 觸發自動排課 |
| GET | /api/v1/schedule/result | 取得排課結果 |

## 命名規範
- Entity：`Teacher`、`CourseRequirement`、`ScheduleItem`
- Request DTO：`TeacherCreateRequest`、`CourseRequirementRequest`
- Response DTO：`TeacherResponse`、`ScheduleItemResponse`
- Service：`TeacherService`（interface）+ `TeacherServiceImpl`（實作）
- URL：複數小寫 `/api/v1/teachers`

## CORS 設定
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true);
    }
}
```
