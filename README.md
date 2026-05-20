# 排課系統 Scheduler

一套針對國中小學設計的全端排課系統。教師可登入後設定自己的科目需求與不可排課時段，系統會依照規則（主科上午、副科下午、同科不同日等）自動為整個年級產生衝突最少的課表。

---

## 一、系統能做什麼

1. **教師管理** — 註冊 / 登入，並區分「班導師（HOMEROOM）」與「科任老師（SUBJECT）」
2. **基本資料設定**
   - 設定老師所屬年級
   - 設定教師類型（班導 / 科任）
   - 設定每週各科目要上幾節（例：國文 5 節 / 數學 4 節）
   - 設定每週不可排課時段（會議、跨校支援等）
3. **自動排課演算法**
   - 兩階段排課：**先排科任老師（大石頭）→ 再排班導師（填沙子）**
   - 科目時段偏好：國/數/英/社/自排上午；體育/美術/音樂/綜合/電腦排下午
   - 同一天不重複排同科目
   - 避開老師的忙碌時段與該年級已被佔用的時段
4. **課表檢視**
   - 個人課表
   - 整個年級的合併課表
   - 衝突 / 缺課回報（哪些課排不下去）
5. **手動微調** — 自動排課後可手動覆寫儲存

---

## 二、技術棧

| 層級 | 技術 |
|------|------|
| 前端 | Vue 3（Composition API + `<script setup>`）+ Vite + Vue Router 4 + Pinia + Axios |
| 後端 | Spring Boot 3.4.1 + Java 21 + Gradle + Spring Data JPA + Lombok |
| 資料庫 | PostgreSQL 15 |
| 容器 | Docker + docker-compose |

---

## 三、專案結構

```
scheduler/
├── CLAUDE.md
├── README.md                       ← 你正在看的這份
├── Dockerfile                      ← 後端多階段建置
├── docker-compose.yml              ← 目前僅啟動 PostgreSQL
├── build.gradle / settings.gradle  ← Gradle 設定
│
├── scheduler-backend/              ← Spring Boot 後端
│   └── main/
│       ├── java/com/example/scheduler/
│       │   ├── SchedulerApplication.java
│       │   ├── common/ApiResponse.java          ← 統一回應格式
│       │   ├── config/CorsConfig.java           ← CORS（允許前端 :5173）
│       │   ├── controller/                      ← REST API 入口
│       │   │   ├── TeacherController.java
│       │   │   ├── CourseController.java
│       │   │   └── AvailabilityController.java
│       │   ├── service/                         ← 業務邏輯
│       │   │   ├── TeacherService.java
│       │   │   ├── CourseService.java
│       │   │   ├── AvailabilityService.java
│       │   │   └── SchedulerService.java        ← 核心排課演算法
│       │   ├── repository/                      ← JPA Repository
│       │   ├── model/                           ← JPA Entity
│       │   │   ├── Teacher.java
│       │   │   ├── CourseRequirement.java
│       │   │   ├── TeacherAvailability.java
│       │   │   └── ScheduleItem.java
│       │   ├── dto/                             ← 請求 / 回應 DTO
│       │   └── exception/GlobalExceptionHandler.java
│       └── resources/application.properties
│
└── scheduler-frontend/             ← Vue 3 前端
    ├── package.json
    ├── vite.config.js              ← /api 代理到後端 :8080
    └── src/
        ├── main.js
        ├── App.vue
        ├── router/index.js         ← 頁面路由
        ├── api/                    ← API 統一封裝
        │   ├── index.js            ← Axios 實例 + 攔截器
        │   └── teacher.js          ← teacherApi / courseApi / availabilityApi
        └── view/
            ├── Login.vue
            ├── Schedule.vue        ← 主要課表頁
            ├── Result.vue
            ├── setup/              ← 首次登入引導
            │   ├── Grade.vue
            │   ├── Type.vue
            │   ├── Courses.vue
            │   └── Availability.vue
            └── admin/TeacherList.vue
```

---

## 四、資料模型

| Entity | 用途 | 主要欄位 |
|--------|------|----------|
| `Teacher` | 教師 | `id`, `name`, `password`, `grade`, `type`（HOMEROOM / SUBJECT） |
| `CourseRequirement` | 教師的每週課程需求 | `subject`, `sessions`, `teacher` |
| `TeacherAvailability` | 教師的「忙碌」時段（不可排課） | `dayOfWeek` (1–5), `period` (1–8), `teacher` |
| `ScheduleItem` | 排定後的課表單元 | `dayOfWeek`, `period`, `subject`, `teacher`, `targetGrade` |

時間網格：一週 5 天 × 一天 8 節。

---

## 五、API 規範

- **統一前綴**：`/api/v1/`
- **統一回應格式**：

  ```json
  {
    "code": 200,
    "message": "success",
    "data": { ... }
  }
  ```

  前端 Axios 攔截器會自動拆出 `data` 並把非 2xx 轉成 `Promise.reject`，所以業務層只拿到資料本身。

### 主要端點

| Method | Path | 說明 |
|--------|------|------|
| `GET`    | `/teachers` | 取得所有教師 |
| `POST`   | `/teachers/register` | 註冊 |
| `POST`   | `/teachers/login` | 登入 |
| `POST`   | `/teachers/{id}/grade` | 更新年級 |
| `POST`   | `/teachers/{id}/type` | 更新教師類型 |
| `GET`    | `/teachers/{id}/courses` | 取得課程需求 |
| `PUT`    | `/teachers/{id}/courses` | 覆蓋式更新課程需求 |
| `POST`   | `/teachers/{id}/courses` | 新增單筆 |
| `PUT`    | `/teachers/{id}/courses/{courseId}` | 更新單筆 |
| `DELETE` | `/teachers/{id}/courses/{courseId}` | 刪除單筆 |
| `GET`    | `/teachers/{id}/availability` | 取得忙碌時段 |
| `PUT`    | `/teachers/{id}/availability` | 覆蓋式更新 |
| `POST`   | `/teachers/{id}/availability` | 新增單筆 |
| `DELETE` | `/teachers/{id}/availability/{slotId}` | 刪除單筆 |
| `DELETE` | `/teachers/{id}/availability` | 清空全部 |
| `POST`   | `/teachers/{id}/auto-schedule` | 觸發自動排課 |
| `GET`    | `/teachers/{id}/schedule` | 個人課表 |
| `GET`    | `/teachers/grade/{grade}/schedule` | 年級課表 |
| `POST`   | `/teachers/{id}/schedule` | 手動覆寫課表 |

---

## 六、排課演算法（SchedulerService）

核心流程位於 [SchedulerService.java](scheduler-backend/main/java/com/example/scheduler/service/SchedulerService.java)：

1. **`autoScheduleGrade(grade)`** — 年級總指揮
   1. 清空該年級舊課表
   2. 撈出年級內所有老師，分為「科任」與「班導」兩組
   3. **第一階段**：科任老師（資源稀缺，先佔位）
   4. **第二階段**：班導師（填補剩餘空格）
   5. 收集所有衝突回報

2. **`autoSchedule(teacherId, grade)`** — 單一老師排課
   1. 清掉這位老師在這個年級的舊課表
   2. 用 `boolean[6][9]` 標記已佔用格子（含老師忙碌時段 + 年級其他老師已排課）
   3. 課程需求排序：主科優先 → 節數多優先
   4. 每節課用 `calculateScore()` 找最佳格子：
      - 主科排上午（period ≤ 4）+20，排下午 −10
      - 副科排下午 +10
      - 同日已有同科 −100
      - 加上少量隨機擾動以避免每次結果相同
   5. 排不下的科目進入 `conflicts` 回傳給前端

---

## 七、本機啟動

### 1. 啟動資料庫

```bash
docker-compose up -d
```

`docker-compose.yml` 會在 `localhost:5432` 起一個 PostgreSQL 容器：

- DB：`schedule_db`
- 帳號：`LeeWayne`
- 密碼：見 `application.properties`（建議改為 env var）

### 2. 啟動後端

```bash
./gradlew bootRun
```

預設監聽 `http://localhost:8080`，JPA `ddl-auto=update` 會自動建立 / 更新 schema。

### 3. 啟動前端

```bash
cd scheduler-frontend
npm install
npm run dev
```

預設監聽 `http://localhost:5173`，Vite 已設定 `/api` 代理至後端 :8080，所以開發時不會有 CORS 問題。

### 容器化打包後端

根目錄 `Dockerfile` 採兩階段建置（Gradle build → JRE runtime）：

```bash
docker build -t scheduler-backend .
docker run -p 8080:8080 scheduler-backend
```

> 註：目前 `Dockerfile` 的 `COPY src src` 路徑與 `build.gradle` 中重新指向的 `scheduler-backend/main/java` 不一致，部署前需要對齊（見「待辦」）。

---

## 八、Git 提交格式

```
feat(teacher): 新增教師可用時段 API
fix(schedule): 修正衝突偵測邏輯
style(ui): 更新課表頁面樣式
```

---

## 九、開發進度

- [x] 基本專案結構
- [x] Teacher / CourseRequirement / ScheduleItem / TeacherAvailability entity + repository
- [x] 三大 Controller（Teacher / Course / Availability）
- [x] DTO 層（Request / Response 隔離 Entity）
- [x] 統一回應格式 `ApiResponse<T>`
- [x] `GlobalExceptionHandler`
- [x] 前端 API 統一封裝（Axios 攔截器）
- [x] 自動排課演算法 v1（兩階段 + 評分）
- [x] 前端路由與基本頁面
- [ ] Pinia store 抽出登入狀態 / 課表狀態
- [ ] 前端 UI 重構（Anthropic 風格）
- [ ] 密碼雜湊（目前以明文存放，**僅限本機開發**）
- [ ] 帳密與資料庫連線改用環境變數
- [ ] `Dockerfile` 對齊 monorepo 結構
- [ ] 單元測試 / 排課演算法的回歸測試

---

## 十、已知問題

1. `Teacher.password` 以明文儲存 — 上線前必須改為 BCrypt
2. `application.properties` 直接寫死 DB 帳密 — 應改為 env var / Spring profiles
3. `Dockerfile` 中 `COPY src src` 與實際 `scheduler-backend/main/java` 路徑不一致
4. `docker-compose.yml` 目前只啟動 DB，尚未把 backend / frontend 一起編排
5. 預設 Vue 模板殘留（如 `HelloWorld.vue` 若未刪）需清理
