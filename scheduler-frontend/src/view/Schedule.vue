<template>
  <div class="schedule-layout">
    <div class="left-panel">
      <div class="toolbar">
        <h2>📅 排課工作台 <span class="teacher-name">{{ teacherName }}</span></h2>
        <div class="eraser" :class="{ active: isEraser }" @click="selectEraser">
          <span>🧹</span> 橡皮擦
        </div>
      </div>

      <div class="grid-container" @mouseup="stopDrag" @mouseleave="stopDrag">
        <div class="grid-header">節</div>
        <div v-for="day in days" :key="day" class="grid-header">{{ day }}</div>

        <template v-for="period in 8" :key="period">
          <div class="period-label">{{ period }}</div>
          
          <div v-for="day in 5" :key="`${day}-${period}`"
               class="grid-cell"
               :class="{ busy: isBusy(day, period) }"
               :style="{ backgroundColor: getCellColor(day, period) }"
               @mousedown="startDrag(day, period)"
               @mouseover="onDragOver(day, period)"
               @click="fillCell(day, period)">
            
            {{ getCellSubject(day, period) }}
            
            <div v-if="getCellInfo(day, period)" class="info-text">
                {{ getCellInfo(day, period) }}
            </div>
          </div>
        </template>
      </div>
    </div>

    <div class="right-panel">
      <h3>🎨 課程畫筆</h3>
      <div class="course-pool">
        <div v-for="(count, subject) in courseNeeds" :key="subject"
             class="pool-item"
             :class="{ active: selectedSubject === subject && !isEraser, empty: getRemaining(subject) <= 0 }"
             @click="selectSubject(subject)">
          <div style="display:flex; align-items:center;">
             <span class="color-dot" :style="{ background: getColor(subject) }"></span>
             {{ subject }}
          </div>
          <span class="badge" :class="{ done: getRemaining(subject) === 0 }">
            {{ getRemaining(subject) > 0 ? `剩 ${getRemaining(subject)}` : 'OK' }}
          </span>
        </div>
      </div>

      <button class="btn-save" @click="saveSchedule">💾 儲存課表</button>
      <button class="btn-auto" @click="autoSchedule">🪄 自動排課</button>
      <button class="btn-back" @click="router.push('/login')">回登入頁</button>
    </div>

    <div v-if="showConflictModal" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
            💬 來自科任老師的訊息 
            <span class="close" @click="showConflictModal = false">&times;</span>
        </div>
        <div class="chat-content">
            <div v-for="(msg, idx) in conflictMessages" :key="idx" :class="['msg-row', msg.type]">
                <strong>{{ msg.sender }}:</strong> {{ msg.text }}
            </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { teacherApi, courseApi, availabilityApi } from '@/api/teacher'

const router = useRouter()
const days = ['週一', '週二', '週三', '週四', '週五']
const PALETTE = {
    '國文': '#FFB7B2', '英文': '#AEC6CF', '數學': '#B9D7EA',
    '自然': '#C8E6C9', '社會': '#E6EE9C', '體育': '#FFECB3',
    '音樂': '#D1C4E9', '美術': '#F0F4C3', '電腦': '#B2DFDB'
}

const teacherName = ref(localStorage.getItem('teacherName'))
const tid = localStorage.getItem('teacherId')
const busySlots = ref([])
const courseNeeds = ref({})
const scheduleData = reactive({})
const selectedSubject = ref(null)
const isEraser = ref(false)
const isMouseDown = ref(false)

const showConflictModal = ref(false)
const conflictMessages = ref([])

onMounted(async () => {
  if (!tid) { router.push('/login'); return }
  await loadAvailability()
  await loadRequirements()
  await loadExistingSchedule()
})

const loadAvailability = async () => {
  try {
    busySlots.value = await availabilityApi.getAvailability(tid)
  } catch (e) {
    alert(e.message || '載入忙碌時段失敗')
  }
}

const loadRequirements = async () => {
  try {
    const data = await courseApi.getCourses(tid)
    const map = {}
    data.forEach(c => { map[c.subject] = c.sessions })
    courseNeeds.value = map
    const first = Object.keys(map)[0]
    if (first) selectSubject(first)
  } catch (e) {
    alert(e.message || '載入課程需求失敗')
  }
}

const loadExistingSchedule = async () => {
  try {
    const data = await teacherApi.getMySchedule(tid)
    for (const key in scheduleData) delete scheduleData[key]
    data.forEach(item => {
      const key = `${item.dayOfWeek}-${item.period}`
      scheduleData[key] = {
        subject: item.subject,
        info: item.teacher?.grade ? `去 ${item.teacher.grade} 年級` : ''
      }
    })
  } catch (e) {
    alert(e.message || '載入課表失敗')
  }
}

// --- 網格邏輯 ---
const isBusy = (day, period) => {
  return busySlots.value.some(b => b.dayOfWeek === day && b.period === period);
};

const getCellSubject = (day, period) => {
  return scheduleData[`${day}-${period}`]?.subject || '';
};

const getCellInfo = (day, period) => {
  return scheduleData[`${day}-${period}`]?.info || '';
};

const getCellColor = (day, period) => {
  const subject = getCellSubject(day, period);
  return subject ? getColor(subject) : 'white';
};

const getColor = (subject) => {
    if (PALETTE[subject]) return PALETTE[subject];
    return '#ddd'; // 預設顏色
};

const getRemaining = (subject) => {
    const total = courseNeeds.value[subject] || 0;
    // 計算目前網格中該科目出現幾次
    let count = 0;
    Object.values(scheduleData).forEach(v => {
        if(v.subject === subject) count++;
    });
    return total - count;
};

// --- 互動邏輯 ---
const selectSubject = (sub) => {
    selectedSubject.value = sub;
    isEraser.value = false;
};

const selectEraser = () => {
    isEraser.value = true;
};

const startDrag = (day, period) => {
    isMouseDown.value = true;
    fillCell(day, period);
};

const onDragOver = (day, period) => {
    if (isMouseDown.value) fillCell(day, period);
};

const stopDrag = () => {
    isMouseDown.value = false;
};

const fillCell = (day, period) => {
    if (isBusy(day, period)) return;
    
    const key = `${day}-${period}`;
    
    if (isEraser.value) {
        delete scheduleData[key];
    } else {
        if (selectedSubject.value && getRemaining(selectedSubject.value) > 0) {
            scheduleData[key] = { subject: selectedSubject.value };
        }
    }
};

const saveSchedule = async () => {
  const items = []
  for (const [key, val] of Object.entries(scheduleData)) {
    const [d, p] = key.split('-')
    items.push({
      dayOfWeek: parseInt(d),
      period: parseInt(p),
      subject: val.subject
    })
  }
  try {
    await teacherApi.saveSchedule(tid, items)
    alert('儲存成功！')
  } catch (e) {
    alert(e.message || '儲存失敗')
  }
}

const autoSchedule = async () => {
  if (!confirm('這將會清除當前排課，確定嗎？')) return
  try {
    const result = await teacherApi.autoSchedule(tid)
    await loadExistingSchedule()
    if (result.conflicts && result.conflicts.length > 0) {
      conflictMessages.value = result.conflicts.map(c => ({ sender: '某老師', text: `衝突科目：${c}` }))
      showConflictModal.value = true
    } else {
      alert('自動排課成功！')
    }
  } catch (e) {
    alert(e.message || '自動排課失敗')
  }
}
</script>

<style scoped>
/* 這裡把原本 schedule.html 和 css 貼過來 */
.schedule-layout { display: flex; height: 100vh; background: #f8f9fa; }
.left-panel { flex: 3; padding: 20px; display: flex; flex-direction: column; }
.right-panel { flex: 1; padding: 20px; background: white; border-left: 1px solid #ddd; }
.grid-container { display: grid; grid-template-columns: 60px repeat(5, 1fr); gap: 1px; background: #ced4da; border: 2px solid #ced4da; margin-top: 10px; user-select: none; }
.grid-header { background: #343a40; color: white; text-align: center; padding: 10px; }
.period-label { background: #e9ecef; display: flex; align-items: center; justify-content: center; font-weight: bold; }
.grid-cell { background: white; min-height: 50px; display: flex; align-items: center; justify-content: center; cursor: pointer; flex-direction: column; }
.grid-cell:hover { filter: brightness(0.95); }
.grid-cell.busy { background-color: #e2e3e5 !important; cursor: not-allowed; pointer-events: none; }
.pool-item { padding: 10px; margin-bottom: 5px; cursor: pointer; background: #f1f3f5; display: flex; justify-content: space-between; border-radius: 6px;}
.pool-item.active { background: white; border: 2px solid #333; }
.pool-item.empty { opacity: 0.5; }
.badge { background: #6c757d; color: white; padding: 2px 8px; border-radius: 10px; font-size: 12px; }
.badge.done { background: #28a745; }
.color-dot { width: 15px; height: 15px; border-radius: 50%; margin-right: 8px; border: 1px solid #ddd; }
.info-text { font-size: 10px; color: #666; }
.btn-save { width: 100%; padding: 12px; background: #28a745; color: white; border: none; border-radius: 5px; margin-top: 10px; cursor: pointer; }
.btn-auto { width: 100%; padding: 12px; background: #6f42c1; color: white; border: none; border-radius: 5px; margin-top: 5px; cursor: pointer; }
.btn-back { width: 100%; padding: 10px; background: #6c757d; color: white; border: none; border-radius: 5px; margin-top: 5px; cursor: pointer; }

/* 視窗樣式簡化 */
.modal-overlay { position: fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; }
.modal-content { background: white; width: 400px; padding: 0; border-radius: 10px; overflow: hidden; }
.modal-header { background: #0d6efd; color: white; padding: 15px; font-weight: bold; display: flex; justify-content: space-between; }
.chat-content { padding: 20px; max-height: 300px; overflow-y: auto; }
.close { cursor: pointer; }
</style>