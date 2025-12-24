const tid = localStorage.getItem('teacherId');
const tName = localStorage.getItem('teacherName');
const tGrade = localStorage.getItem('teacherGrade'); 
const tType = localStorage.getItem('teacherType') || (tGrade ? 'HOMEROOM' : 'SUBJECT');

// 設定標題
if (tType === 'HOMEROOM') {
    document.getElementById('pageTitle').innerText = `${tGrade} 年級班級課表`;
    document.getElementById('pageSubtitle').innerText = `導師：${tName}`;
} else {
    document.getElementById('pageTitle').innerText = `${tName} 老師行程表`;
    document.getElementById('pageSubtitle').innerText = `科任教師`;
}

const DAYS = ['週一', '週二', '週三', '週四', '週五'];
const PALETTE = {
    '國文': '#FFCDD2', '英文': '#BBDEFB', '數學': '#E1BEE7',
    '自然': '#C8E6C9', '社會': '#FFF9C4', '體育': '#FFECB3', 
    '音樂': '#D1C4E9', '美術': '#F0F4C3', '電腦': '#B2DFDB'
};

renderGrid();
loadSchedule();

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
            grid.appendChild(cell);
        }
    }
}

async function loadSchedule() {
    let url = '';
    if (tType === 'HOMEROOM') {
        url = `/api/teachers/grade/${tGrade}/schedule`;
    } else {
        url = `/api/teachers/${tid}/schedule`;
    }

    try {
        const res = await fetch(url);
        const data = await res.json();

        data.forEach(item => {
            const cell = document.querySelector(`.grid-cell[data-day="${item.dayOfWeek}"][data-period="${item.period}"]`);
            if (cell) {
                // 科目標籤
                const subjectDiv = document.createElement('div');
                subjectDiv.className = 'subject-tag';
                subjectDiv.innerText = item.subject;
                subjectDiv.style.backgroundColor = PALETTE[item.subject] || '#EEE';
                cell.appendChild(subjectDiv);

                // 額外資訊 (老師或班級)
                const infoDiv = document.createElement('div');
                infoDiv.className = 'info-tag';

                if (tType === 'HOMEROOM') {
                    if (item.teacher && item.teacher.name !== tName) {
                        infoDiv.innerText = item.teacher.name + " 老師";
                    }
                } else {
                    if (item.teacher && item.teacher.grade) {
                        infoDiv.innerText = "去 " + item.teacher.grade + " 年級";
                    }
                }
                cell.appendChild(infoDiv);
            }
        });
    } catch (e) {
        console.error("載入失敗", e);
    }
}