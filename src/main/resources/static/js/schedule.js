const tid = localStorage.getItem('teacherId');
const tName = localStorage.getItem('teacherName');
const tGrade = localStorage.getItem('teacherGrade'); 
const tType = localStorage.getItem('teacherType') || 'HOMEROOM';

// 顯示老師名字
if(document.getElementById('teacherName')) {
    document.getElementById('teacherName').innerText = tName;
}

const DAYS = ['週一', '週二', '週三', '週四', '週五'];
const PALETTE = {
    '國文': '#FFB7B2', '英文': '#AEC6CF', '數學': '#B9D7EA',
    '自然': '#C8E6C9', '社會': '#E6EE9C', '體育': '#FFECB3', 
    '音樂': '#D1C4E9', '美術': '#F0F4C3', '電腦': '#B2DFDB'
};

let busySlots = [];
let courseNeeds = {}; 
let isMouseDown = false; 
let selectedSubject = null;
let isEraser = false;

// 自動執行初始化
(async function init() {
    try {
        await loadAvailability();
        await loadRequirements();
        renderGrid();
        await loadExistingSchedule(); 
        renderSidebar();
        autoSelectFirstSubject();     
    } catch (e) {
        console.error(e);
    }
})();

async function loadAvailability() {
    const res = await fetch(`/api/teachers/${tid}/availability`);
    busySlots = await res.json();
}

async function loadRequirements() {
    const res = await fetch(`/api/teachers/${tid}/courses`);
    const data = await res.json();
    data.forEach(c => courseNeeds[c.subject] = c.sessions);
}

function renderGrid() {
    const grid = document.getElementById('scheduleGrid');
    grid.innerHTML = ''; 

    const corner = document.createElement('div');
    corner.className = 'grid-header';
    corner.innerText = '節';
    grid.appendChild(corner);

    DAYS.forEach(d => {
        const header = document.createElement('div');
        header.className = 'grid-header';
        header.innerText = d;
        grid.appendChild(header);
    });

    for (let p = 1; p <= 8; p++) {
        const label = document.createElement('div');
        label.className = 'period-label';
        label.innerText = p;
        grid.appendChild(label);

        for (let d = 1; d <= 5; d++) {
            const cell = document.createElement('div');
            cell.className = 'grid-cell';
            cell.dataset.day = d;
            cell.dataset.period = p;

            const isBusy = busySlots.some(b => b.dayOfWeek === d && b.period === p);
            if (isBusy) {
                cell.classList.add('busy');
                cell.innerText = "✖";
            } else {
                cell.addEventListener('mousedown', () => { isMouseDown = true; paintCell(cell); });
                cell.addEventListener('mouseover', () => { if (isMouseDown) paintCell(cell); });
                cell.addEventListener('click', () => { paintCell(cell); });
            }
            grid.appendChild(cell);
        }
    }
    document.addEventListener('mouseup', () => isMouseDown = false);
}

function paintCell(cell) {
    if (cell.classList.contains('busy')) return;
    const oldSubject = cell.dataset.subject;

    if (isEraser) {
        if (oldSubject) {
            clearCell(cell);
            renderSidebar();
        }
        return;
    }

    if (!selectedSubject) return; 
    if (oldSubject === selectedSubject) return;

    const remaining = getRemainingCount(selectedSubject);
    if (remaining <= 0) return; 

    fillCell(cell, selectedSubject);
    renderSidebar(); 
}

function fillCell(cell, subject) {
    cell.dataset.subject = subject;
    cell.innerText = subject;
    cell.style.backgroundColor = getColor(subject);
}

function clearCell(cell) {
    delete cell.dataset.subject;
    cell.innerText = "";
    cell.style.backgroundColor = "white";
    // 清除額外資訊 (小字)
    const extraInfo = cell.querySelector('div');
    if(extraInfo) cell.removeChild(extraInfo);
}

function getColor(subject) {
    if (PALETTE[subject]) return PALETTE[subject];
    let hash = 0;
    for (let i = 0; i < subject.length; i++) hash = subject.charCodeAt(i) + ((hash << 5) - hash);
    const c = (hash & 0x00FFFFFF).toString(16).toUpperCase();
    return '#' + "00000".substring(0, 6 - c.length) + c;
}

function getRemainingCount(subject) {
    const total = courseNeeds[subject] || 0;
    const placed = document.querySelectorAll(`.grid-cell[data-subject="${subject}"]`).length;
    return total - placed;
}

function renderSidebar() {
    const pool = document.getElementById('coursePool');
    const currentActive = selectedSubject; 
    pool.innerHTML = '';
    
    for (const [subject, total] of Object.entries(courseNeeds)) {
        const remaining = getRemainingCount(subject);
        const div = document.createElement('div');
        div.className = `pool-item ${remaining <= 0 ? 'empty' : ''}`;
        
        if (subject === currentActive && !isEraser) {
            div.classList.add('active');
        }
        
        const colorBox = `<span style="display:inline-block; width:15px; height:15px; background:${getColor(subject)}; border-radius:50%; margin-right:10px; border:1px solid rgba(0,0,0,0.1);"></span>`;
        
        div.innerHTML = `
            <div style="display:flex; align-items:center;">
                ${colorBox} <span>${subject}</span>
            </div>
            <span class="badge ${remaining === 0 ? 'done' : ''}">${remaining > 0 ? '剩 '+remaining : 'OK'}</span>
        `;
        
        div.onclick = () => selectSubject(subject);
        pool.appendChild(div);
    }
}

function selectSubject(subject) {
    selectedSubject = subject;
    isEraser = false;
    document.getElementById('eraserBtn').classList.remove('active');
    renderSidebar(); 
}

function selectEraser() {
    isEraser = true;
    document.getElementById('eraserBtn').classList.add('active');
    renderSidebar(); 
}

function autoSelectFirstSubject() {
    for (const [subject, total] of Object.entries(courseNeeds)) {
        if (getRemainingCount(subject) > 0) {
            selectSubject(subject);
            break;
        }
    }
}

async function loadExistingSchedule() {
    let url = '';
    // 依據身分決定撈取哪個 API
    if (tType === 'HOMEROOM') {
        url = `/api/teachers/grade/${tGrade}/schedule`;
    } else {
        url = `/api/teachers/${tid}/schedule`;
    }

    const res = await fetch(url);
    const data = await res.json();

    // 先清空
    document.querySelectorAll('.grid-cell').forEach(c => {
        if(!c.classList.contains('busy')) clearCell(c);
    });

    data.forEach(item => {
        const cell = document.querySelector(`.grid-cell[data-day="${item.dayOfWeek}"][data-period="${item.period}"]`);
        if (cell && !cell.classList.contains('busy')) {
            fillCell(cell, item.subject);
            
            // 補充資訊
            if (tType === 'SUBJECT') {
                const info = document.createElement('div');
                info.style.fontSize = '10px';
                info.style.color = '#555';
                info.innerText = `去 ${item.teacher.grade || '?'} 年級`;
                cell.appendChild(info);
            } else if (tType === 'HOMEROOM' && item.teacher && item.teacher.id != tid) {
                 // 班導師看課表時，如果是科任老師的課，可以顯示名字 (這裡簡略)
            }
        }
    });
}

async function saveSchedule() {
    const items = [];
    document.querySelectorAll('.grid-cell').forEach(cell => {
        if (cell.dataset.subject) {
            items.push({
                dayOfWeek: parseInt(cell.dataset.day),
                period: parseInt(cell.dataset.period),
                subject: cell.dataset.subject
            });
        }
    });

    const res = await fetch(`/api/teachers/${tid}/schedule`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(items)
    });

    if(res.ok) alert("課表儲存成功！🎉");
    else alert("儲存失敗");
}

async function autoSchedule() {
    if(!confirm("這將會清除您目前的排課結果並重新安排，確定嗎？")) return;
    
    const btn = document.querySelector('.btn-save[onclick="autoSchedule()"]');
    const originalText = btn.innerText;
    btn.innerText = "🤖 協調中...";
    btn.disabled = true;

    try {
        const res = await fetch(`/api/teachers/${tid}/auto-schedule`, {
            method: 'POST'
        });
        const data = await res.json(); // 這裡現在拿到的是 Map {schedule: [], conflicts: []}

        if (res.ok) {
            // 1. 先重新載入當前畫面，顯示排進去的課程
            await loadExistingSchedule();

            // 2. 檢查是否有衝突
            const conflicts = data.conflicts; // 這是 List<String>
            if (conflicts && conflicts.length > 0) {
                // 有衝突！顯示對話框
                showChat(conflicts);
                alert("部分課程安排成功，但有幾堂課撞期了！\n請查看科任老師的留言。");
            } else {
                // 完全成功，沒有衝突
                alert("✨ 自動排課完美成功！");
                window.location.href = 'result.html';
            }
        } else {
            alert("系統錯誤");
        }
    } catch (e) {
        console.error(e);
        alert("連線失敗");
    } finally {
        btn.innerText = originalText;
        btn.disabled = false;
    }

    function showChat(conflicts) {
        const modal = document.getElementById('chatModal');
        const chatContent = document.getElementById('chatContent');
        chatContent.innerHTML = ''; // 清空舊訊息
    
        modal.style.display = 'flex';
    
        // 1. 系統訊息
        addMessage("系統", "已為您安排大部分課程，但以下科目發生時段衝突...", "system");
    
        // 2. 針對每個衝突科目，模擬老師發言
        // 使用 Set 去重，避免同個科目跳出太多次
        const uniqueConflicts = [...new Set(conflicts)];
        
        uniqueConflicts.forEach(subject => {
            const msg = getRandomComplaint(subject);
            addMessage(subject + "老師", msg, "teacher");
        });
    }   
}
