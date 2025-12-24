const tid = localStorage.getItem('teacherId');
if(!tid) window.location.href = "login.html";

const DAYS = ['週一', '週二', '週三', '週四', '週五'];
let isMouseDown = false;

// 初始化
renderGrid();
loadAvailability();

function renderGrid() {
    const grid = document.getElementById('scheduleGrid');
    grid.innerHTML = ''; 

    // 表頭
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

    // 格子
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
            cell.innerText = "可排";
            
            // 綁定事件
            cell.addEventListener('mousedown', () => { isMouseDown = true; toggle(cell); });
            cell.addEventListener('mouseover', () => { if(isMouseDown) toggle(cell); });
            cell.addEventListener('click', () => toggle(cell));

            grid.appendChild(cell);
        }
    }
    document.addEventListener('mouseup', () => isMouseDown = false);
}

function toggle(cell) {
    if (cell.classList.contains('busy')) {
        cell.classList.remove('busy');
        cell.innerText = "可排";
    } else {
        cell.classList.add('busy');
        cell.innerText = "不排";
    }
}

function loadAvailability() {
    fetch(`/api/teachers/${tid}/availability`)
        .then(res => res.json())
        .then(data => {
            data.forEach(slot => {
                const cell = document.querySelector(`.grid-cell[data-day="${slot.dayOfWeek}"][data-period="${slot.period}"]`);
                if(cell) {
                    cell.classList.add('busy');
                    cell.innerText = "不排";
                }
            });
        });
}

function finish() {
    const busySlots = [];
    document.querySelectorAll('.grid-cell.busy').forEach(cell => {
        busySlots.push({
            dayOfWeek: parseInt(cell.dataset.day),
            period: parseInt(cell.dataset.period)
        });
    });

    fetch(`/api/teachers/${tid}/availability`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(busySlots)
    }).then(() => {
        window.location.href = "schedule.html";
    });
}