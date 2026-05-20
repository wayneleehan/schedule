# Frontend 規範 — Vue 3

排課系統的前端。獨立運作；只需要知道後端 API 的 base URL 與 `ApiResponse` 格式，不需要懂 Java/Spring。

## 技術棧

- Vue 3.5（Composition API + `<script setup>`）
- Vite 7
- Vue Router 4
- Pinia 3（已安裝，store 尚未實作）
- Axios

## 目錄結構

```text
scheduler-frontend/
├── CLAUDE.md
├── package.json
├── vite.config.js               ← /api → :8080 dev proxy
├── jsconfig.json
└── src/
    ├── main.js
    ├── App.vue
    ├── api/                     ← API 統一封裝
    │   ├── index.js             ← axios 實例 + 攔截器
    │   └── teacher.js           ← teacherApi / courseApi / availabilityApi（之後可拆檔）
    ├── router/
    │   └── index.js
    └── view/                    ← 頁面級組件（建議改名為 views/ 複數，符合 Vue 慣例）
        ├── Login.vue
        ├── Schedule.vue
        ├── Result.vue
        ├── admin/
        │   └── TeacherList.vue
        └── setup/               ← 首次登入引導流程
            ├── Grade.vue
            ├── Type.vue
            ├── Courses.vue
            └── Availability.vue
```

## 與後端的契約

- API base：`/api/v1`（dev 時 Vite proxy 轉到 `:8080`，prod 由反向代理或同源處理）
- 所有後端回應都是 `{ code, message, data }` — 在 [api/index.js](src/api/index.js) 的 response 攔截器中已自動拆出 `data` 給呼叫端
- 非 2xx 或 `code` 非 2xx 會被轉成 `Promise.reject(new Error(message))`，呼叫端用 try/catch 處理

```js
try {
  const teachers = await teacherApi.getAll()  // 直接拿到 data 陣列
} catch (err) {
  // err.message 是後端的 message 或網路錯誤
}
```

## API 層規則

- 所有 axios 呼叫都集中在 `src/api/` — 組件**禁止**直接 `import axios`
- 每個 API 物件對應後端一個 Controller，方法名用動詞：`getAll / create / update / delete / ...`
- 新增端點時：
  1. 在 [api/teacher.js](src/api/teacher.js) 對應 API 物件中加 method
  2. 該方法只回傳 `http.xxx(...)`，不做業務轉換（轉換交給 store 或 view）

## Pinia Store 規則（待補實作）

- 檔名：`useXxxStore.js`，置於 `src/stores/`
- 用 setup-style：`defineStore('teacher', () => { ... })`
- store 內可以 `import { teacherApi } from '@/api/teacher'`
- view 取資料**透過 store**，不直接呼叫 api 層（除非真的是純展示且不共享狀態）

```js
// 範例
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { teacherApi } from '@/api/teacher'

export const useTeacherStore = defineStore('teacher', () => {
  const teachers = ref([])
  const loading = ref(false)

  async function fetchAll() {
    loading.value = true
    try { teachers.value = await teacherApi.getAll() }
    finally { loading.value = false }
  }
  return { teachers, loading, fetchAll }
})
```

## 路由

設定檔：[router/index.js](src/router/index.js)

| Path | Component | 用途 |
| --- | --- | --- |
| `/login` | Login.vue | 登入 / 註冊 |
| `/setup/grade` | setup/Grade.vue | 設定所屬年級 |
| `/setup/type` | setup/Type.vue | 設定班導 / 科任 |
| `/setup/courses` | setup/Courses.vue | 設定課程需求 |
| `/setup/availability` | setup/Availability.vue | 設定忙碌時段 |
| `/schedule` | Schedule.vue | 主要課表頁 |
| `/result` | Result.vue | 排課結果 |
| `/admin/teachers` | admin/TeacherList.vue | 管理者：教師列表 |

> 待加：登入守衛 (`beforeEach`)、未登入導回 `/login`、setup 流程的步驟控制。

## 組件規範

- **一律 `<script setup>`**，禁止 Options API
- Props 必須定義 type 和 default
- 元件命名 PascalCase：`TeacherList.vue`
- 頁面級組件放 `view/`（對應路由）
- 可重用無狀態 UI 元件放 `components/ui/`（待建立）
- 版面骨架放 `components/layout/`（待建立）
- **view 內禁止直接 `import axios`** — 必須透過 store 或 api 層

## 設計風格：Anthropic 極簡風（待套用）

色彩 / 字體 / 排版規則目前還沒套用。實作時依以下變數寫進 `src/assets/main.css`：

```css
:root {
  --color-bg:             #F5F4EF;  /* 米白底色 */
  --color-surface:        #FFFFFF;
  --color-text-primary:   #1A1A1A;
  --color-text-secondary: #6B6B6B;
  --color-accent:         #D97757;  /* Anthropic 橘紅 */
  --color-border:         #E5E3DC;

  --radius-sm: 4px;
  --radius-md: 8px;
}
```

字體：`DM Serif Display`（標題）+ `DM Sans`（內文），由 Google Fonts 引入。

### 設計原則

1. **大量留白**：section padding 最少 48px、元素間距最少 16px
2. **無多餘裝飾**：不用漸層、不堆疊陰影、圓角保守（4–8px）
3. **文字優先**：靠字型大小與粗細建立層級
4. **邊框代替陰影**：用 `1px solid var(--color-border)` 區隔區塊
5. **互動克制**：hover 只改 opacity 或 color，transition 0.15s ease

## 環境變數（待建立 `.env`）

```env
# .env.development
VITE_API_BASE_URL=/api/v1            # 走 vite proxy

# .env.production
VITE_API_BASE_URL=/api/v1            # 由反向代理處理
```

目前 [api/index.js](src/api/index.js) 直接寫死 `/api/v1`，未來要改成 `import.meta.env.VITE_API_BASE_URL`。

## 開發指令

```bash
cd scheduler-frontend
npm install
npm run dev         # http://localhost:5173
npm run build       # 產生 dist/
npm run preview     # 預覽 build 結果
```

## 已知待辦

### 結構 / 基礎建設

- [ ] `view/` 改名為 `views/`（Vue 官方推薦複數）
- [ ] 建立 `stores/`，把登入狀態與課表狀態抽出
- [ ] 拆 [api/teacher.js](src/api/teacher.js)：分成 `teacher.js / course.js / availability.js`
- [ ] 清掉 Vue 預設模板（`components/HelloWorld.vue`、`assets/logo.svg` 等，如果還在）
- [ ] 環境變數改用 `import.meta.env.VITE_API_BASE_URL`
- [ ] 套用 Anthropic 設計風格

### 流程 / UX（手動走過 login → setup 1~4 → schedule → result 一定會踩到）

- [ ] **登入守衛**：未登入時導回 `/login`（目前各頁靠 `localStorage.getItem('teacherId')` 自行檢查，分散且不一致）
- [ ] **登出機制統一**：只有 [setup/Grade.vue](src/view/setup/Grade.vue) 有 `logout()`，其他頁沒有；應抽到 layout 或 store
- [ ] **Setup 流程的「跳過已設定」邏輯**：老師重新登入後直接帶到 `/schedule`，不要每次都重走 4 步
- [ ] **Setup 中途離開的處理**：使用者離開後再回來時，應該回到上次的步驟而不是從頭開始
- [ ] **錯誤 toast 統一處理**：目前散落各頁的 `alert()`，應改用統一通知元件
- [ ] **登入成功後的路由判斷**：登入後一律 `router.push('/setup/grade')`，應改為「沒設定過 → setup / 設定過 → schedule」

### 與後端契約

- [ ] 確認所有 setup 頁面儲存後，後端 DB 真的寫入正確（目前已 curl 驗證 register/login，其他端點還沒端到端測過）
