# Backend 規範 — Spring Boot

排課系統的後端。獨立運作；只需要知道 PostgreSQL 連線資訊，不需要懂前端。

## 技術棧

- Spring Boot 3.4.1
- Java 21（Gradle Toolchain）
- Spring Data JPA + Hibernate
- PostgreSQL 15
- Lombok（已引入但目前 entity / DTO 多為手寫 getter/setter）
- Gradle 多模組（根目錄 `settings.gradle` 宣告，本模組是 `:scheduler-backend`）

## 目錄結構

```text
scheduler-backend/
├── CLAUDE.md
├── build.gradle              ← 模組獨立的 build 設定
├── Dockerfile                ← build context 為本資料夾
└── src/
    ├── main/
    │   ├── java/com/example/scheduler/
    │   │   ├── SchedulerApplication.java
    │   │   ├── common/ApiResponse.java          ← 統一回應包裝
    │   │   ├── config/CorsConfig.java           ← 允許 :5173
    │   │   ├── controller/                      ← REST 入口
    │   │   │   ├── TeacherController.java
    │   │   │   ├── CourseController.java
    │   │   │   └── AvailabilityController.java
    │   │   ├── service/                         ← 業務邏輯
    │   │   │   ├── TeacherService.java
    │   │   │   ├── CourseService.java
    │   │   │   ├── AvailabilityService.java
    │   │   │   └── SchedulerService.java        ← 自動排課演算法
    │   │   ├── repository/                      ← Spring Data JPA
    │   │   ├── model/                           ← JPA Entity
    │   │   ├── dto/                             ← Request / Response
    │   │   └── exception/GlobalExceptionHandler.java
    │   └── resources/application.properties
    └── test/java/com/example/scheduler/SchedulerApplicationTests.java
```

## 分層規則（嚴格遵守）

```
Controller → Service → Repository → DB
```

- **Controller**：只負責解析請求、呼叫 Service、用 `ApiResponse` 包裝回傳。**禁止直接呼叫 Repository。**
- **Service**：業務邏輯、Entity ↔ DTO 轉換、`@Transactional` 管交易
- **Repository**：純資料存取，可自訂 JPQL 或 derived query
- **Entity 不可直接回傳給前端** — 全部走 DTO（避免循環序列化、避免外洩 password 等敏感欄位）

## 統一回應格式

所有 Controller 必須回傳 `ApiResponse<T>`：

```java
return ApiResponse.success(data);                   // 200
return ApiResponse.success("註冊成功", data);        // 200 + 自訂訊息
return ApiResponse.error(400, "帳號已存在");         // 錯誤（一般丟例外讓 GlobalExceptionHandler 處理）
```

實作位置：[common/ApiResponse.java](src/main/java/com/example/scheduler/common/ApiResponse.java)

## 例外處理

所有未捕獲的例外都會被 [GlobalExceptionHandler](src/main/java/com/example/scheduler/exception/GlobalExceptionHandler.java) 接住並包成 `ApiResponse`：

| Exception | HTTP | code |
| --- | --- | --- |
| `IllegalArgumentException` | 400 | 400 |
| `RuntimeException` | 400 | 400 |
| `Exception`（其他） | 500 | 500 |

Service 層直接 `throw new RuntimeException("帳號已存在")` 即可。

## 資料模型

| Entity | 用途 | 主要欄位 |
| --- | --- | --- |
| `Teacher` | 教師 | `name`, `password`（明文 ⚠️）, `grade`, `type`（HOMEROOM / SUBJECT） |
| `CourseRequirement` | 每週課程需求 | `subject`, `sessions`, `teacher`（FK） |
| `TeacherAvailability` | 不可排課時段 | `dayOfWeek` (1–5), `period` (1–8), `teacher` |
| `ScheduleItem` | 排定後的課表 | `dayOfWeek`, `period`, `subject`, `targetGrade`, `teacher` |

時間網格：5 天 × 8 節。

> ⚠️ 所有業務欄位目前都是 `nullable`、`teachers.name` 沒有 UNIQUE — 之後要補 `@Column(nullable=false)` 與 unique constraint。

## API 端點

統一前綴 `/api/v1/`。詳細端點清單見根目錄 [README.md](../README.md#五api-規範)。

### URL 命名

- 資源用複數小寫：`/teachers`
- 子資源走 path：`/teachers/{id}/availability`、`/teachers/{id}/courses`

## 排課演算法

核心邏輯在 [SchedulerService.java](src/main/java/com/example/scheduler/service/SchedulerService.java)：

1. **年級總指揮 `autoScheduleGrade(grade)`**：先排科任老師（資源稀缺）→ 再排班導師（填空格）
2. **單一老師排課 `autoSchedule(teacherId, grade)`**：建 `boolean[6][9]` 佔用表、依規則打分數、選最佳格子
3. **評分規則**（`calculateScore`）：
   - 主科（國/數/英/社/自）排上午 +20、排下午 -10
   - 副科（體育/美術/音樂/綜合/電腦）排下午 +10
   - 同一天已有同科 -100
   - 加少量隨機擾動，避免每次結果相同

## 資料庫連線

設定檔：[application.properties](src/main/resources/application.properties)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/schedule_db
spring.datasource.username=LeeWayne
spring.datasource.password=Lumberjacker012     # ⚠️ 明文，僅限本機
spring.jpa.hibernate.ddl-auto=update           # 自動建/改表
```

啟動 DB（從專案根目錄）：

```bash
docker-compose up -d
```

直接連 DB 看資料：

```bash
docker exec -it scheduler-db psql -U LeeWayne -d schedule_db
```

常用 psql 指令：`\dt` 列表、`\d+ teachers` 看 schema、`SELECT * FROM teachers;`

## 開發指令

```bash
# 從專案根目錄
./gradlew :scheduler-backend:bootRun        # 啟動（:8080）
./gradlew :scheduler-backend:test           # 測試
./gradlew :scheduler-backend:bootJar        # 打 fat jar

# Docker
docker build -t scheduler-backend ./scheduler-backend
docker run -p 8080:8080 scheduler-backend
```

## 命名規範

- Entity：`Teacher`、`CourseRequirement`、`ScheduleItem`、`TeacherAvailability`
- Request DTO：`{Resource}{Action}Request`，例如 `TeacherCreateRequest`
- Response DTO：`{Resource}Response`，例如 `TeacherResponse`
- Service：直接用 class（目前未拆 interface / impl，模組小不需要）
- 套件：全小寫，單字之間不加底線

## 已知待辦

- [ ] 密碼雜湊（BCrypt）— 目前 `Teacher.password` 明文存放
- [ ] DB 帳密改用環境變數（`${DB_PASSWORD}`）
- [ ] Entity 補 `@Column(nullable=false)`、`teachers.name` 加 UNIQUE
- [ ] `teacher_availability` 加 `(teacher_id, day_of_week, period)` 複合 unique
- [ ] `schedule_items` 加 `(target_grade, day_of_week, period)` 複合 unique 防衝突
- [ ] 課表查詢加索引（`target_grade`、`teacher_id`）
- [ ] 補實際單元測試（目前只有 `SchedulerApplicationTests` 空殼）
- [ ] Bean validation（`@NotBlank`、`@Min` 等）+ `@Valid`
