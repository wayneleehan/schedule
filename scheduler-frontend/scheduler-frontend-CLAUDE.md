# Frontend 規範 — Vue 3

> 放置於 `scheduler/scheduler-frontend/CLAUDE.md`

## ⚠️ 第一步：修正資料夾名稱
```bash
# 在 scheduler-frontend/ 目錄下執行
mv srcf src
```
然後更新 `vite.config.js` 和 `index.html` 中所有 `srcf` 路徑。

## 套件安裝（尚未安裝，需要先執行）
```bash
npm install pinia
npm install -D tailwindcss @tailwindcss/vite
```

## 目標目錄結構
```
src/
├── main.js
├── App.vue
├── api/                    ← API 統一管理（待建立）
│   ├── index.js            ← axios 實例 + interceptor
│   ├── teacher.js
│   ├── course.js
│   └── schedule.js
├── stores/                 ← Pinia（待建立）
│   ├── useTeacherStore.js
│   ├── useCourseStore.js
│   └── useScheduleStore.js
├── router/
│   └── index.js            ✅ 已存在（需更新路由）
├── views/                  ← 原為 view（建議改名複數）
│   ├── Login.vue           ✅ 已存在
│   ├── Schedule.vue        ✅ 已存在
│   ├── Result.vue          ✅ 已存在
│   ├── admin/
│   │   └── TeacherList.vue ✅ 已存在
│   └── setup/
│       ├── Availability.vue ✅ 已存在
│       ├── Courses.vue     ✅ 已存在
│       ├── Grade.vue       ✅ 已存在
│       └── Type.vue        ✅ 已存在
└── components/
    ├── ui/                 ← 可複用基礎組件（待建立）
    │   ├── BaseButton.vue
    │   ├── BaseInput.vue
    │   ├── BaseTable.vue
    │   └── BaseModal.vue
    └── layout/
        ├── AppHeader.vue
        └── AppSidebar.vue
```

## 刪除預設模板組件
以下檔案是 Vue 預設模板，請直接刪除：
```
src/components/HelloWorld.vue
src/components/TheWelcome.vue
src/components/WelcomeItem.vue
src/components/icons/IconCommunity.vue
src/components/icons/IconDocumentation.vue
src/components/icons/IconEcosystem.vue
src/components/icons/IconSupport.vue
src/components/icons/IconTooling.vue
src/assets/logo.svg
```

## 設計風格：Anthropic 極簡風

### 色彩系統（CSS 變數，定義於 main.css）
```css
:root {
  --color-bg:            #F5F4EF;  /* 米白底色，Anthropic 標誌性暖底 */
  --color-surface:       #FFFFFF;
  --color-surface-alt:   #FAFAF7;
  --color-text-primary:  #1A1A1A;
  --color-text-secondary:#6B6B6B;
  --color-text-muted:    #9B9B9B;
  --color-accent:        #D97757;  /* Anthropic 橘紅 */
  --color-accent-hover:  #C4684A;
  --color-border:        #E5E3DC;
  --color-border-strong: #C8C6BF;

  --radius-sm:  4px;
  --radius-md:  8px;
  --radius-lg:  12px;

  --shadow-sm:  0 1px 3px rgba(0,0,0,0.06);
  --shadow-md:  0 4px 12px rgba(0,0,0,0.08);
}
```

### 字體
```css
/* 在 index.html 引入 */
<link href="https://fonts.googleapis.com/css2?family=DM+Serif+Display&family=DM+Sans:wght@300;400;500&display=swap" rel="stylesheet">

/* 使用 */
font-family: 'DM Serif Display', Georgia, serif;   /* 標題 */
font-family: 'DM Sans', system-ui, sans-serif;     /* 內文 */
```

### 設計原則
1. **大量留白**：section padding 最少 48px，元素間距最少 16px
2. **無多餘裝飾**：不用漸層背景、不堆疊陰影、圓角保守（4-8px）
3. **文字優先**：排版即設計，靠字型大小和粗細建立層級
4. **邊框代替陰影**：用 `1px solid var(--color-border)` 區隔區塊
5. **互動克制**：hover 只改 opacity 或 color，transition 0.15s ease

### Tailwind 對應（安裝後使用）
```js
// tailwind.config.js
export default {
  theme: {
    extend: {
      colors: {
        bg: '#F5F4EF',
        surface: '#FFFFFF',
        accent: '#D97757',
        border: '#E5E3DC',
        muted: '#6B6B6B',
      },
      fontFamily: {
        serif: ['DM Serif Display', 'Georgia', 'serif'],
        sans: ['DM Sans', 'system-ui', 'sans-serif'],
      }
    }
  }
}
```

## API 層範本
```js
// src/api/index.js
import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  timeout: 10000,
})

// Request interceptor：加 token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Response interceptor：統一錯誤處理
api.interceptors.response.use(
  res => res.data,   // 直接回傳 ApiResponse 的 data 欄位
  err => {
    const message = err.response?.data?.message || '系統錯誤'
    console.error('[API Error]', message)
    return Promise.reject(new Error(message))
  }
)

export default api
```

```js
// src/api/teacher.js
import api from './index'

export const teacherApi = {
  getAll: ()           => api.get('/teachers'),
  create: (data)       => api.post('/teachers', data),
  update: (id, data)   => api.put(`/teachers/${id}`, data),
  delete: (id)         => api.delete(`/teachers/${id}`),
  getAvailability: (id)=> api.get(`/teachers/${id}/availability`),
  setAvailability:(id, data) => api.post(`/teachers/${id}/availability`, data),
}
```

## Pinia Store 範本
```js
// src/stores/useTeacherStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { teacherApi } from '@/api/teacher'

export const useTeacherStore = defineStore('teacher', () => {
  const teachers = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function fetchAll() {
    loading.value = true
    try {
      const res = await teacherApi.getAll()
      teachers.value = res.data
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  return { teachers, loading, error, fetchAll }
})
```

## 組件規範
- 使用 `<script setup>` 語法，禁止 Options API
- Props 必須定義 type 和 default
- 組件用 PascalCase 命名：`TeacherList.vue`
- views/ 目錄只放頁面級組件（對應路由）
- components/ui/ 放可複用的無狀態基礎組件
- 禁止在 view 組件中直接呼叫 axios，必須透過 store 或 api 層

## 路由結構
```js
// src/router/index.js
const routes = [
  { path: '/',        component: () => import('@/views/Login.vue') },
  { path: '/schedule', component: () => import('@/views/Schedule.vue'),
    children: [
      { path: 'availability', component: () => import('@/views/setup/Availability.vue') },
      { path: 'courses',      component: () => import('@/views/setup/Courses.vue') },
      { path: 'grade',        component: () => import('@/views/setup/Grade.vue') },
      { path: 'type',         component: () => import('@/views/setup/Type.vue') },
    ]
  },
  { path: '/result',  component: () => import('@/views/Result.vue') },
  { path: '/admin/teachers', component: () => import('@/views/admin/TeacherList.vue') },
]
```

## 環境變數
```env
# .env.development
VITE_API_BASE_URL=http://localhost:8080/api/v1

# .env.production
VITE_API_BASE_URL=/api/v1
```
