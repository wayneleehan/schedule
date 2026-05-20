<template>
  <div class="login-container">
    <div class="card">
      <h2>{{ isLoginMode ? "教師登入" : "建立新帳號" }}</h2>
      
      <input v-model="form.name" type="text" placeholder="輸入名字 (帳號)">
      <input v-model="form.password" type="password" placeholder="輸入密碼">
      
      <button @click="handleAction">{{ isLoginMode ? "登入" : "註冊" }}</button>
      
      <div class="toggle" @click="toggleMode">
        {{ isLoginMode ? "還沒有帳號？點此註冊" : "已有帳號？返回登入" }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { teacherApi } from '@/api/teacher'

const router = useRouter()
const isLoginMode = ref(true)
const form = reactive({ name: '', password: '' })

const toggleMode = () => {
  isLoginMode.value = !isLoginMode.value
}

const handleAction = async () => {
  if (!form.name || !form.password) {
    alert('請輸入帳號和密碼')
    return
  }

  try {
    if (isLoginMode.value) {
      const teacher = await teacherApi.login(form)
      localStorage.setItem('teacherId', teacher.id)
      localStorage.setItem('teacherName', teacher.name)
      localStorage.setItem('teacherGrade', teacher.grade ?? '')
      localStorage.setItem('teacherType', teacher.type ?? '')
      alert('登入成功！')
      router.push('/setup/grade')
    } else {
      await teacherApi.register(form)
      alert('註冊成功！請直接登入。')
      toggleMode()
    }
  } catch (e) {
    alert(e.message || '系統錯誤')
  }
}
</script>

<style scoped>
/* 這裡放原本 login.html 裡的 CSS */
.login-container { display: flex; justify-content: center; padding-top: 100px; background-color: #f0f2f5; height: 100vh; }
.card { background: white; padding: 40px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); width: 320px; text-align: center; height: fit-content; }
input { width: 100%; padding: 12px; margin: 10px 0; border: 1px solid #ddd; border-radius: 8px; box-sizing: border-box; }
button { width: 100%; padding: 12px; background-color: #0d6efd; color: white; border: none; border-radius: 8px; cursor: pointer; margin-top: 10px; }
button:hover { background-color: #0b5ed7; }
.toggle { margin-top: 20px; color: #0d6efd; cursor: pointer; text-decoration: underline; }
</style>