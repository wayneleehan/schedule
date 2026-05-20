# 排課系統 (Scheduler) — Monorepo 總覽

學校排課系統：教師登入後設定課程需求與不可排課時段，系統依規則自動產生年級課表。

> 這份檔案只放「整體層」的資訊。模組細節分別放在：
>
> - 後端規範：[scheduler-backend/CLAUDE.md](scheduler-backend/CLAUDE.md)
> - 前端規範：[scheduler-frontend/CLAUDE.md](scheduler-frontend/CLAUDE.md)

## 結構

```
scheduler/
├── CLAUDE.md                  ← 你在這裡
├── README.md
├── settings.gradle            ← Gradle 多模組設定（include scheduler-backend）
├── gradlew / gradle/          ← Gradle Wrapper
├── docker-compose.yml         ← 啟動 PostgreSQL
│
├── scheduler-backend/         ← Spring Boot 3.4.1 + Java 21
│   ├── CLAUDE.md
│   ├── build.gradle
│   ├── Dockerfile
│   └── src/main/java/com/example/scheduler/
│
└── scheduler-frontend/        ← Vue 3 + Vite
    ├── CLAUDE.md
    ├── package.json
    └── src/
```

## 跨模組共識（前後端都要遵守）

### API 基礎路徑
所有 API 統一前綴：`/api/v1/`

### 統一回應格式

```json
{ "code": 200, "message": "success", "data": { ... } }
```

後端用 `ApiResponse<T>` 包裝、前端 Axios 攔截器拆出 `data`。

### Git 提交格式
```
feat(teacher): 新增教師可用時段 API
fix(schedule): 修正衝突偵測邏輯
style(ui): 更新課表頁面樣式
```

## 一鍵啟動

```bash
# 1. 啟動資料庫
docker-compose up -d

# 2. 啟動後端（從專案根目錄）
./gradlew :scheduler-backend:bootRun

# 3. 啟動前端
cd scheduler-frontend && npm install && npm run dev
```

預設網址：

- 前端：`http://localhost:5173`
- 後端：`http://localhost:8080`
- DB：`localhost:5432/schedule_db`（user: `LeeWayne`）

## 開發進度

- [x] 後端：4 個 Entity + Repository + Service + Controller + DTO + GlobalExceptionHandler + CorsConfig
- [x] 後端：自動排課演算法 v1（兩階段：先科任後班導）
- [x] 前端：基本路由與 setup 流程頁面
- [x] 前端：API 統一封裝（axios 攔截器）
- [ ] 前端：Pinia store
- [ ] 前端：Anthropic 風格 UI 套版
- [ ] 後端：密碼雜湊（目前明文，**僅限本機**）
- [ ] 後端：DB 帳密改為環境變數
- [ ] 後端：Entity 加 `@Column(nullable=false)`、`teachers.name` 加 UNIQUE
- [ ] 整合：docker-compose 加入 backend / frontend 服務

## 跨模組未解問題

1. 後端 [application.properties](scheduler-backend/src/main/resources/application.properties) 寫死 DB 帳密 — 上 production 必須改 env var
2. `Teacher.password` 明文存放
3. [CorsConfig.java](scheduler-backend/src/main/java/com/example/scheduler/config/CorsConfig.java) 只允許 `localhost:5173`，部署時要改
