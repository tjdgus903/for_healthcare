// ====== 기본 유틸 ======
const API = location.origin;
const outEl = () => document.getElementById('out');
const tokenPreviewEl = () => document.getElementById('tokenPreview');

const out = (v) => {
  const el = outEl();
  if (!el) return;
  el.textContent = (typeof v === 'string') ? v : JSON.stringify(v, null, 2);
};

const getToken = () => localStorage.getItem('jwt') || '';
const setToken = (t) => {
  localStorage.setItem('jwt', t || '');
  const el = tokenPreviewEl();
  if (el) el.textContent = (t ? t.slice(0, 30) + '...' : '-');
};

// 초기 토큰 미리 반영
setToken(getToken());

async function req(path, opt = {}) {
  const headers = { ...(opt.headers || {}) };
  const jwt = getToken();
  if (jwt) headers['Authorization'] = 'Bearer ' + jwt;

  const res = await fetch(API + path, { ...opt, headers });
  let body;
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) body = await res.json();
  else body = await res.text();

  console.log(opt.method || 'GET', path, res.status, body);
  out({ status: res.status, body });

  if (!res.ok) throw new Error(res.status + ' ' + (typeof body === 'string' ? body : JSON.stringify(body)));
  return body;
}

function get(p) { return req(p); }
function post(p, data) {
  return req(p, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: data ? JSON.stringify(data) : null
  });
}

// ====== 화면 조작 함수 ======
function toggleTheme() {
  const r = document.documentElement;
  r.setAttribute('data-theme', r.getAttribute('data-theme') === 'high-contrast' ? '' : 'high-contrast');
}

function renderCalendar(yyyyMm, days) {
  const host = document.getElementById('calendar');
  if (!host) return;

  const [y, m] = yyyyMm.split('-').map(Number);
  const first = new Date(y, m - 1, 1);
  const last  = new Date(y, m, 0);
  const startW = first.getDay();

  const checkedMap = new Map(days.map(d => [d.date, !!d.checked]));

  let html = `<div><b>${yyyyMm}</b></div>
    <table class="cal">
      <thead><tr>
        <th>일</th><th>월</th><th>화</th><th>수</th><th>목</th><th>금</th><th>토</th>
      </tr></thead><tbody>`;

  let d = 1;
  const rows = Math.ceil((startW + last.getDate()) / 7);
  for (let r = 0; r < rows; r++) {
    html += '<tr>';
    for (let c = 0; c < 7; c++) {
      const cellIndex = r * 7 + c;
      const isCurrent = cellIndex >= startW && d <= last.getDate();
      if (!isCurrent) {
        html += `<td class="muted"><div>&nbsp;</div></td>`;
      } else {
        const dateStr = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
        const isChecked = checkedMap.get(dateStr);
        html += `<td class="${isChecked ? 'checked' : ''}" title="${dateStr}"><div>${d}</div></td>`;
        d++;
      }
    }
    html += '</tr>';
  }
  html += `</tbody></table><div class="legend">초록색: 출석(O)</div>`;

  host.innerHTML = html;
}

// ====== API 호출 동작 ======
async function doLogin() {
  const email = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;
  const res = await post('/auth/login', { email, password });
  setToken(res.token);
}

async function doSignup() {
  const email = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;
  const res = await post('/auth/signup', { email, password });
  setToken(res.token);
}

async function attendanceCheck() {
  await post('/attendance/check');
  await fetchCalendar(); // 체크 후 갱신
}

async function fetchCalendar() {
  const v = document.getElementById('month').value; // yyyy-MM
  const days = await get(`/attendance/calendar?month=${encodeURIComponent(v)}`);
  renderCalendar(v, days);
}

function clearToken() { setToken(''); }

async function startSession() {
  const type = document.getElementById('gameType').value;
  const res = await post('/sessions/start?type=' + encodeURIComponent(type));
  document.getElementById('sessionId').value = res.sessionId || res.id || '';
}

async function endSession() {
  const id = document.getElementById('sessionId').value;
  const payload = { score: 10, accuracy: 0.9, meta: { demo: true } };
  const res = await post(`/sessions/${id}/end`, payload);
  out(res);
}

async function downloadFile(path) {
  const jwt = getToken();
  const res = await fetch(API + path, { headers: jwt ? { Authorization: 'Bearer ' + jwt } : {} });
  if (!res.ok) { out(await res.text()); return; }
  const blob = await res.blob();
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = 'export.zip';
  a.click();
  URL.revokeObjectURL(a.href);
  out('다운로드 시작');
}

async function verifyStub() {
  const body = {
    platform: "ANDROID",
    productId: "sub_premium_monthly",
    purchaseToken: "dev-token-" + Date.now(),
    devMode: true
  };
  const res = await post('/subs/verify', body);
  out(res);
}

// ====== 이벤트 바인딩 ======
document.addEventListener('DOMContentLoaded', () => {
  // 버튼 ID 매핑
  const bind = (id, handler) => {
    const el = document.getElementById(id);
    if (el) el.addEventListener('click', handler);
  };

  bind('btnLogin', doLogin);
  bind('btnSignup', doSignup);
  bind('btnClearToken', clearToken);

  bind('btnAttendanceCheck', attendanceCheck);
  bind('btnFetchCalendar', fetchCalendar);

  bind('btnStartSession', startSession);
  bind('btnEndSession', endSession);

  bind('btnSubsStatus', () => get('/subs/status'));
  bind('btnAdsConfig',  () => get('/ads/config'));

  bind('btnMyReport', () => get('/reports/me'));
  bind('btnConsents',  () => get('/privacy/consents'));
  bind('btnExportZip', () => downloadFile('/privacy/export.zip'));

  bind('btnVerifyStub', verifyStub);
  bind('btnToggleTheme', toggleTheme);

  // 최초 진입 시 현재 month로 달력 표시 (선택)
  const monthEl = document.getElementById('month');
  if (monthEl && monthEl.value) fetchCalendar();
});