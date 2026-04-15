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
  import { ref, reactive } from 'vue';
  import { useRouter } from 'vue-router';
  
  const router = useRouter();
  const isLoginMode = ref(true);
  const form = reactive({ name: '', password: '' });
  
  const toggleMode = () => {
    isLoginMode.value = !isLoginMode.value;
  };
  
  const handleAction = async () => {
    if (!form.name || !form.password) {
      alert("請輸入帳號和密碼");
      return;
    }
  
    const url = isLoginMode.value ? '/api/teachers/login' : '/api/teachers/register';
  
    try {
      const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      });
  
      if (res.ok) {
        const data = await res.text();
        // 如果 data 有內容，代表成功拿到 Teacher 物件
        if (data) {
          const teacher = JSON.parse(data);
          
          if (isLoginMode.value) {
            // --- 登入成功邏輯 ---
            localStorage.setItem('teacherId', teacher.id);
            localStorage.setItem('teacherName', teacher.name);
            localStorage.setItem('teacherGrade', teacher.grade || "");
            localStorage.setItem('teacherType', teacher.type || "");
            
            alert("登入成功！");
            router.push('/setup/grade');
          } else {
            // --- 註冊成功邏輯 ---
            alert("註冊成功！請直接登入。");
            toggleMode(); // 切換回登入模式
          }
        } else {
          // 🔥 這裡是重點：後端回傳 null (空字串) 代表失敗
          if (isLoginMode.value) {
              alert("登入失敗：帳號或密碼錯誤");
          } else {
              alert("註冊失敗：該帳號名稱已被使用，請換一個名字！");
          }
        }
      } else {
        alert("系統錯誤");
      }
    } catch (e) {
      console.error(e);
      alert("連線失敗");
    }
  };
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