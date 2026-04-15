<template>
    <div class="setup-container">
      <div class="card">
        <div class="progress">設定流程：2 / 4</div>
        <h2>您的教師身份是？</h2>
        
        <div class="checkbox-group">
          <label class="checkbox-option" :class="{ active: type === 'HOMEROOM' }">
            <input type="radio" value="HOMEROOM" v-model="type"> 班導師 (Homeroom)
          </label>
          <label class="checkbox-option" :class="{ active: type === 'SUBJECT' }">
            <input type="radio" value="SUBJECT" v-model="type"> 科任教師 (Subject)
          </label>
        </div>
  
        <div class="btn-group">
          <button class="btn btn-prev" @click="router.push('/setup/grade')">上一步</button>
          <button class="btn btn-next" @click="nextStep">下一步</button>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  
  const router = useRouter();
  const tid = localStorage.getItem('teacherId');
  const type = ref(null);
  
  onMounted(() => {
    if (!tid) router.push('/login');
    const saved = localStorage.getItem('teacherType');
    if (saved) type.value = saved;
  });
  
  const nextStep = async () => {
    if (!type.value) { alert("請選擇類型"); return; }
    
    await fetch(`/api/teachers/${tid}/type`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: type.value // 注意後端接收的格式，這裡是純字串
    });
  
    localStorage.setItem('teacherType', type.value);
    router.push('/setup/courses');
  };
  </script>
  
  <style scoped>
  /* 沿用 setup 共用樣式，加上 type.html 特有的 */
  .setup-container { display: flex; flex-direction: column; align-items: center; padding-top: 50px; background-color: #f8f9fa; min-height: 100vh; }
  .card { background: white; padding: 40px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); width: 400px; text-align: center; }
  .checkbox-group { display: flex; flex-direction: column; gap: 10px; text-align: left; margin: 20px 0; }
  .checkbox-option { padding: 15px; border: 2px solid #ddd; border-radius: 8px; cursor: pointer; display: flex; align-items: center; }
  .checkbox-option.active { border-color: #198754; background-color: #e8f5e9; color: #198754; font-weight: bold; }
  input { margin-right: 10px; }
  .btn-group { display: flex; gap: 10px; margin-top: 20px; }
  .btn { flex: 1; padding: 12px; border: none; border-radius: 50px; cursor: pointer; font-size: 16px; }
  .btn-prev { background-color: #e9ecef; color: #333; }
  .btn-next { background-color: #198754; color: white; }
  </style>