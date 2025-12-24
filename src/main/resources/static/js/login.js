let isLogin = true;

function toggleMode() {
    isLogin = !isLogin;
    document.getElementById('title').innerText = isLogin ? "教師登入" : "建立新帳號";
    document.getElementById('btnText').innerText = isLogin ? "登入" : "註冊";
    document.getElementById('toggleText').innerText = isLogin ? "還沒有帳號？點此註冊" : "已有帳號？返回登入";
}

async function handleAction() {
    const name = document.getElementById('name').value;
    const password = document.getElementById('password').value;
    
    if(!name || !password) { alert("請輸入帳號和密碼"); return; }

    const api = isLogin ? '/api/teachers/login' : '/api/teachers/register';

    try {
        const res = await fetch(api, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ name: name, password: password })
        });

        if (res.ok) {
            const data = await res.text();
            if (data) {
                const teacher = JSON.parse(data);
                
                if (isLogin) {
                    // 登入成功，儲存所有重要資訊
                    localStorage.setItem('teacherId', teacher.id);
                    localStorage.setItem('teacherName', teacher.name);
                    localStorage.setItem('teacherGrade', teacher.grade || ""); 
                    localStorage.setItem('teacherType', teacher.type || "");   
                    
                    alert("登入成功！");
                    // 根據是否有設定過年級，決定跳轉頁面 (可選)
                    // 這裡先統一跳到設定年級頁，或直接去排課頁
                    window.location.href = "grade.html";
                } else {
                    alert("註冊成功！請直接登入。");
                    toggleMode();
                }
            } else {
                alert("登入失敗：帳號或密碼錯誤");
            }
        } else {
            alert("系統錯誤");
        }
    } catch (e) {
        console.error(e);
        alert("連線失敗");
    }
}