<template>
    <div class="setup-container">
      <div class="logout-link" @click="logout">登出</div>
      
      <div class="card">
        <div class="progress">設定流程：1 / 4</div>
        <h2>您負責哪個年級？</h2>
        
        <div class="checkbox-group">
          <label v-for="g in 6" :key="g" class="checkbox-option" :class="{ active: selectedGrade == g }">
            <input type="radio" :value="g" v-model="selectedGrade"> 
            {{ g }} 年級
          </label>
        </div>
  
        <button class="btn-next" @click="nextStep">下一步 (設定類型)</button>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { teacherApi } from '@/api/teacher'

  const router = useRouter()
  const tid = localStorage.getItem('teacherId')
  const selectedGrade = ref(null)

  onMounted(() => {
    if (!tid) router.push('/login')
    const saved = localStorage.getItem('teacherGrade')
    if (saved) selectedGrade.value = parseInt(saved)
  })

  const nextStep = async () => {
    if (!selectedGrade.value) { alert('請選擇年級'); return }
    try {
      await teacherApi.updateGrade(tid, selectedGrade.value)
      localStorage.setItem('teacherGrade', selectedGrade.value)
      router.push('/setup/type')
    } catch (e) {
      alert(e.message || '更新年級失敗')
    }
  }

  const logout = () => {
    localStorage.clear()
    router.push('/login')
  }
  </script>
  
  <style scoped>
  /* grade.html 的 CSS */
  .setup-container { display: flex; flex-direction: column; align-items: center; padding-top: 50px; background-color: #f8f9fa; min-height: 100vh; }
  .card { background: white; padding: 40px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); width: 400px; text-align: center; }
  .progress { color: #888; margin-bottom: 20px; font-size: 14px; }
  .checkbox-group { display: flex; flex-direction: column; gap: 10px; text-align: left; margin: 20px 0; }
  .checkbox-option { padding: 10px; border: 2px solid #ddd; border-radius: 8px; cursor: pointer; transition: 0.2s; display: flex; align-items: center; }
  .checkbox-option:hover { background-color: #f1f3f5; }
  .checkbox-option.active { border-color: #0d6efd; background-color: #e7f1ff; color: #0d6efd; font-weight: bold; }
  input { margin-right: 10px; transform: scale(1.2); }
  .btn-next { background-color: #0d6efd; color: white; padding: 12px 30px; border: none; border-radius: 50px; cursor: pointer; font-size: 16px; margin-top: 20px; width: 100%; }
  .logout-link { position: absolute; top: 20px; right: 20px; color: #dc3545; cursor: pointer; text-decoration: underline; }
  </style>