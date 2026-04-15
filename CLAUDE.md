# 排課系統 (Scheduler) — Claude Code 主指南

## 專案概覽
學校排課系統，管理教師、課程類型、年級需求、教師可用時段，並自動生成課表。

## 技術棧
| 層級 | 技術 |
|------|------|
| 前端 | Vue 3 (Composition API + `<script setup>`) + Vite + Vue Router 4 + Pinia + Axios |
| 後端 | Spring Boot 3.4.1 + Java 21 + Gradle + JPA + PostgreSQL + Lombok |
| 容器 | Docker + docker-compose |

## Monorepo 結構
```
scheduler/
├── CLAUDE.md                  ← 你在這裡
├── scheduler-backend/         ← Spring Boot
│   ├── main/java/com/example/scheduler/
│   └── main/resources/
├── scheduler-frontend/        ← Vue 3
│   └── src/                   ← 注意：原本打錯為 srcf，請重新命名
├── build.gradle
├── docker-compose.yml
└── Dockerfile
```

## 開發規範

### Git 提交格式
```
feat(teacher): 新增教師可用時段 API
fix(schedule): 修正衝突偵測邏輯
style(ui): 更新課表頁面樣式
```

### API 基礎路徑
所有 API 統一前綴：`/api/v1/`

### 統一回應格式（前後端都必須遵守）
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

## 當前開發進度
- [x] 基本專案結構建立
- [x] Teacher entity + repository
- [x] CourseRequirement entity + repository
- [x] ScheduleItem entity + repository
- [x] TeacherAvailability entity + repository
- [x] 前端路由基本頁面（Login、Schedule、Result）
- [ ] DTO 層建立（高優先）
- [ ] GlobalExceptionHandler（高優先）
- [ ] CourseController / ScheduleController / AvailabilityController
- [ ] 前端 API 層統一管理
- [ ] Pinia store 建立
- [ ] 前端 UI 重構（Anthropic 風格）
- [ ] 自動排課演算法完善
- [ ] 衝突偵測邏輯

## 已知問題（待修復）
1. 前端資料夾名稱 `srcf` → 應改為 `src`
2. 預設 Vue 模板組件未清除（HelloWorld.vue 等）
3. 後端 Entity 直接回傳給前端，需要 DTO 隔離
4. 缺乏統一錯誤處理
