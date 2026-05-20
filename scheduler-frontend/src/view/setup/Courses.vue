<template>
    <div class="setup-container">
      <div class="card">
        <div class="progress">設定流程：3 / 4</div>
        <h2>每週需要上幾節課？</h2>
        <p style="font-size: 0.9em; color: #666;">輸入 0 代表不教該科</p>
        
        <div class="input-grid">
          <div v-for="subject in subjectsList" :key="subject" class="input-group">
            <label>{{ subject }}</label>
            <input type="number" min="0" max="20" v-model.number="sessions[subject]">
          </div>
        </div>
  
        <div class="btn-group">
          <button class="btn btn-prev" @click="router.push('/setup/type')">上一步</button>
          <button class="btn btn-next" @click="nextStep">下一步</button>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { reactive, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { courseApi } from '@/api/teacher'

  const router = useRouter()
  const tid = localStorage.getItem('teacherId')
  const subjectsList = ['國文', '英文', '數學', '自然', '社會']
  const sessions = reactive({})

  onMounted(async () => {
    if (!tid) { router.push('/login'); return }
    subjectsList.forEach(s => { sessions[s] = 0 })

    try {
      const data = await courseApi.getCourses(tid)
      data.forEach(item => {
        if (sessions[item.subject] !== undefined) {
          sessions[item.subject] = item.sessions
        }
      })
    } catch (e) {
      alert(e.message || '載入課程需求失敗')
    }
  })

  const nextStep = async () => {
    const payload = subjectsList
      .filter(s => sessions[s] > 0)
      .map(s => ({ subject: s, sessions: sessions[s] }))

    try {
      await courseApi.updateCourses(tid, payload)
      router.push('/setup/availability')
    } catch (e) {
      alert(e.message || '儲存課程需求失敗')
    }
  }
  </script>
  
  <style scoped>
  /* 沿用 setup 樣式，加上 courses.html 特有的 */
  .setup-container { display: flex; flex-direction: column; align-items: center; padding-top: 50px; background-color: #f8f9fa; min-height: 100vh; }
  .card { background: white; padding: 40px; border-radius: 12px; width: 400px; text-align: center; }
  .input-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin: 20px 0; text-align: left; }
  .input-group label { display: block; font-weight: bold; margin-bottom: 5px; color: #555; }
  .input-group input { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 6px; box-sizing: border-box; text-align: center; font-size: 16px; }
  .btn-group { display: flex; gap: 10px; margin-top: 20px; }
  .btn { flex: 1; padding: 12px; border: none; border-radius: 50px; cursor: pointer; }
  .btn-prev { background-color: #e9ecef; }
  .btn-next { background-color: #fd7e14; color: white; }
  </style>