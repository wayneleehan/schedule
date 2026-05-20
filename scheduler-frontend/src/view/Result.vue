<template>
    <div class="result-page">
      <div class="card">
        <h2 id="pageTitle">{{ title }}</h2>
        <div class="subtitle">{{ subtitle }}</div>
  
        <div class="grid-container">
          <div class="grid-header">節</div>
          <div v-for="d in days" :key="d" class="grid-header">{{ d }}</div>
  
          <template v-for="period in 8" :key="period">
            <div class="period-label">{{ period }}</div>
            <div v-for="day in 5" :key="`${day}-${period}`" class="grid-cell">
              
              <template v-if="getCell(day, period)">
                <div class="subject-tag" :style="{ backgroundColor: getColor(getCell(day, period).subject) }">
                  {{ getCell(day, period).subject }}
                </div>
                <div class="info-tag">{{ getInfo(getCell(day, period)) }}</div>
              </template>
              
            </div>
          </template>
        </div>
  
        <div class="btn-group">
          <button class="btn btn-back" @click="router.push('/schedule')">✏️ 返回修改</button>
          <button class="btn btn-print" @click="printPage">🖨️ 列印課表</button>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { reactive, onMounted, computed } from 'vue'
  import { useRouter } from 'vue-router'
  import { teacherApi } from '@/api/teacher'

  const router = useRouter()
  const days = ['週一', '週二', '週三', '週四', '週五']
  const PALETTE = {
      '國文': '#FFCDD2', '英文': '#BBDEFB', '數學': '#E1BEE7',
      '自然': '#C8E6C9', '社會': '#FFF9C4', '體育': '#FFECB3',
      '音樂': '#D1C4E9', '美術': '#F0F4C3', '電腦': '#B2DFDB'
  }

  const tid = localStorage.getItem('teacherId')
  const tName = localStorage.getItem('teacherName')
  const tGrade = localStorage.getItem('teacherGrade')
  const tType = localStorage.getItem('teacherType')

  const scheduleMap = reactive({})

  const title = computed(() => {
    return tType === 'HOMEROOM' ? `${tGrade} 年級班級課表` : `${tName} 老師行程表`
  })

  const subtitle = computed(() => {
    return tType === 'HOMEROOM' ? `導師：${tName}` : `科任教師`
  })

  onMounted(async () => {
    try {
      const data = tType === 'HOMEROOM'
        ? await teacherApi.getGradeSchedule(tGrade)
        : await teacherApi.getMySchedule(tid)
      data.forEach(item => {
        scheduleMap[`${item.dayOfWeek}-${item.period}`] = item
      })
    } catch (e) {
      alert(e.message || '載入課表失敗')
    }
  })
  
  const getCell = (d, p) => scheduleMap[`${d}-${p}`];
  
  const getInfo = (item) => {
    // 如果我是班導師 (HOMEROOM)
    if (tType === 'HOMEROOM') {
        // 如果這堂課的老師，不是我 (代表是科任老師)
        if (item.teacher && item.teacher.name !== tName) {
            return item.teacher.name + " 老師"; // 顯示：王小明 老師
        }
    } 
    // 如果我是科任老師 (SUBJECT)
    else {
        // 顯示我去教哪個年級
        if (item.targetGrade) {
            return "去 " + item.targetGrade + " 年級";
        }
    }
    return "";
};
  
  const getColor = (sub) => PALETTE[sub] || '#EEE';
  const printPage = () => window.print();
  
  </script>
  
  <style scoped>
  .result-page { display: flex; flex-direction: column; align-items: center; padding: 20px; background-color: #f0f2f5; min-height: 100vh; }
  .card { background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); width: 80%; max-width: 900px; }
  h2 { text-align: center; color: #333; margin-bottom: 5px; }
  .subtitle { text-align: center; color: #666; font-size: 14px; margin-bottom: 20px; }
  .grid-container { display: grid; grid-template-columns: 60px repeat(5, 1fr); gap: 1px; background: #dee2e6; border: 2px solid #dee2e6; border-radius: 4px; margin-bottom: 20px; }
  .grid-header { background: #343a40; color: white; text-align: center; padding: 12px; font-weight: bold; }
  .period-label { background: #e9ecef; display: flex; align-items: center; justify-content: center; font-weight: bold; }
  .grid-cell { background: white; min-height: 60px; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 5px; text-align: center; font-size: 16px; font-weight: bold; color: #333; }
  .subject-tag { padding: 4px 8px; border-radius: 6px; margin-bottom: 4px; font-size: 0.9em; }
  .info-tag { font-size: 12px; color: #666; font-weight: normal; }
  .btn-group { display: flex; justify-content: center; gap: 15px; }
  .btn { padding: 10px 20px; border: none; border-radius: 8px; cursor: pointer; font-size: 16px; font-weight: bold; }
  .btn-print { background-color: #0d6efd; color: white; }
  .btn-back { background-color: #6c757d; color: white; }
  
  @media print {
      .result-page { background: white; padding: 0; }
      .card { box-shadow: none; width: 100%; max-width: 100%; padding: 0; }
      .btn-group { display: none; }
      .grid-container { border: 1px solid #000; }
      .grid-header { background: #ddd !important; color: black; border: 1px solid #000; }
      .grid-cell { border: 1px solid #000; }
  }
  </style>