<template>
    <div class="setup-container">
      <div class="card wide-card">
        <div class="progress">設定流程：4 / 4</div>
        <h2>設定不排課時間</h2>
        <p style="color:#666; font-size:14px;">請點擊或拖曳選取 <span style="color:#dc3545; font-weight:bold;">無法排課</span> 的紅色時段</p>
  
        <div class="grid-container" @mouseleave="isMouseDown = false" @mouseup="isMouseDown = false">
          <div class="grid-header">節</div>
          <div v-for="d in days" :key="d" class="grid-header">{{ d }}</div>
  
          <template v-for="period in 8" :key="period">
            <div class="period-label">{{ period }}</div>
            <div v-for="day in 5" :key="`${day}-${period}`"
                 class="grid-cell"
                 :class="{ busy: isBusy(day, period) }"
                 @mousedown="startToggle(day, period)"
                 @mouseover="onHover(day, period)"
                 @click="toggle(day, period)">
              {{ isBusy(day, period) ? "不排" : "可排" }}
            </div>
          </template>
        </div>
  
        <div class="btn-group">
          <button class="btn btn-prev" @click="router.push('/setup/courses')">上一步</button>
          <button class="btn btn-finish" @click="finish">完成設定，開始排課 🚀</button>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  
  const router = useRouter();
  const tid = localStorage.getItem('teacherId');
  const days = ['週一', '週二', '週三', '週四', '週五'];
  const busySet = ref(new Set()); // 使用 Set 儲存 "day-period" 字串，比 Array 更好操作
  const isMouseDown = ref(false);
  
  onMounted(async () => {
    if (!tid) router.push('/login');
    
    const res = await fetch(`/api/teachers/${tid}/availability`);
    const data = await res.json();
    data.forEach(slot => {
      busySet.value.add(`${slot.dayOfWeek}-${slot.period}`);
    });
  });
  
  const isBusy = (d, p) => busySet.value.has(`${d}-${p}`);
  
  const toggle = (d, p) => {
    const key = `${d}-${p}`;
    if (busySet.value.has(key)) busySet.value.delete(key);
    else busySet.value.add(key);
  };
  
  const startToggle = (d, p) => {
    isMouseDown.value = true;
    toggle(d, p);
  };
  
  const onHover = (d, p) => {
    if (isMouseDown.value) toggle(d, p);
  };
  
  const finish = async () => {
    const busyList = [];
    busySet.value.forEach(key => {
      const [d, p] = key.split('-');
      busyList.push({ dayOfWeek: parseInt(d), period: parseInt(p) });
    });
  
    await fetch(`/api/teachers/${tid}/availability`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(busyList)
    });
    
    router.push('/schedule');
  };
  </script>
  
  <style scoped>
  /* css */
  .setup-container { display: flex; flex-direction: column; align-items: center; padding-top: 30px; background-color: #f8f9fa; min-height: 100vh; }
  .card { background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); text-align: center; }
  .wide-card { width: 600px; }
  .grid-container { display: grid; grid-template-columns: 50px repeat(5, 1fr); gap: 2px; margin: 20px 0; user-select: none; }
  .grid-header { background: #333; color: white; padding: 8px; font-weight: bold; border-radius: 4px; }
  .period-label { background: #eee; display: flex; align-items: center; justify-content: center; font-weight: bold; }
  .grid-cell { background: #d4edda; height: 45px; border: 1px solid #ccc; border-radius: 4px; display: flex; align-items: center; justify-content: center; cursor: pointer; font-size: 14px; color: #155724; }
  .grid-cell.busy { background: #f8d7da; color: #721c24; font-weight: bold; border-color: #f5c6cb; }
  .btn-group { display: flex; gap: 10px; margin-top: 20px; }
  .btn { flex: 1; padding: 12px; border: none; border-radius: 50px; cursor: pointer; font-size: 16px; font-weight: bold; }
  .btn-prev { background-color: #e9ecef; }
  .btn-finish { background-color: #0d6efd; color: white; }
  </style>