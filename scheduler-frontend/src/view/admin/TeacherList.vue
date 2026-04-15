<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { teacherApi } from '@/api/teacher'

const router = useRouter()
const teachers = ref([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    teachers.value = await teacherApi.getAll()
  } catch (e) {
    error.value = e.message || '載入教師列表失敗'
  } finally {
    loading.value = false
  }
})

const getTypeLabel = (type) => {
  if (type === 'HOMEROOM') return '班導師'
  if (type === 'SUBJECT')  return '科任教師'
  return '未設定'
}

const getTypeBadge = (type) => {
  if (type === 'HOMEROOM') return 'badge badge-homeroom'
  if (type === 'SUBJECT')  return 'badge badge-subject'
  return 'badge badge-unset'
}
</script>

<template>
  <div class="page">
    <div class="container">

      <!-- 頁首 -->
      <div class="page-header flex items-center justify-between">
        <div>
          <h2>教師列表</h2>
          <p>所有已註冊的教師帳號一覽</p>
        </div>
        <button class="btn btn-secondary btn--sm" @click="router.push('/login')">
          ← 回登入頁
        </button>
      </div>

      <!-- 錯誤提示 -->
      <div v-if="error" class="alert alert-error">
        {{ error }}
      </div>

      <!-- 載入中 -->
      <div v-if="loading" class="empty-state">
        <span class="loading-spinner"></span>
        <p>載入中…</p>
      </div>

      <!-- 空狀態 -->
      <div v-else-if="!loading && teachers.length === 0 && !error" class="empty-state">
        <p>目前尚無已註冊的教師。</p>
      </div>

      <!-- 表格 -->
      <div v-else-if="!loading && teachers.length > 0" class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>姓名（帳號）</th>
              <th>身分</th>
              <th>年級／負責班級</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in teachers" :key="t.id">
              <td class="text-muted text-sm font-mono">{{ t.id }}</td>
              <td style="font-weight: 600;">{{ t.name }}</td>
              <td>
                <span :class="getTypeBadge(t.type)">{{ getTypeLabel(t.type) }}</span>
              </td>
              <td>
                <span v-if="t.grade">{{ t.grade }} 年級</span>
                <span v-else class="text-muted">—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 頁尾計數 -->
      <p v-if="!loading && teachers.length > 0" class="text-xs text-muted mt-2">
        共 {{ teachers.length }} 位教師
      </p>

    </div>
  </div>
</template>
