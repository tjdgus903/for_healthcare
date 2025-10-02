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

function updateAttendanceButtonState() {
  const btn = document.getElementById('btnAttendanceCheck');
  if (btn) btn.disabled = !getToken();
}
document.addEventListener('DOMContentLoaded', updateAttendanceButtonState);
window.addEventListener('storage', (e) => {
  if (e.key === 'jwt') updateAttendanceButtonState();
});

// ====== API 호출 동작 ======
async function doLogin() {
  const email = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;

  // 1) 서버에 로그인 요청
  const res = await post('/auth/login', { email, password });

  // 2) 토큰 저장 (키는 'jwt'로 통일)
  setToken(res.token);         // localStorage.setItem('jwt', token)

  // 3) 헤더 토글 즉시 반영
  if (typeof applyAuthUI === 'function') applyAuthUI();

  // 4) next 있으면 그쪽으로, 없으면 홈으로
  const params = new URLSearchParams(location.search);
  const next = params.get('next') || '/';
  location.replace(next);
}

async function doSignup() {
  const email = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;
  const res = await post('/auth/signup', { email, password });
  setToken(res.token);
}

async function attendanceCheck() {
  if (!getToken()) { out('로그인 후 출석 체크를 할 수 있어요.'); return; }
  await post('/attendance/check');
  await fetchCalendar(); // 체크 후 갱신
}

async function fetchCalendar() {
  const v = document.getElementById('month').value; // yyyy-MM

  // 비로그인: API 호출 없이 달력만 그림(체크 없음)
  if (!getToken()) {
    renderCalendar(v, []); // checkedMap이 비어 있으므로 빈 달력
    out('로그인하면 출석 표시가 함께 보여요.');
    return;
  }

  // 로그인: 서버에서 출석 데이터 받아 렌더
  try {
    const days = await get(`/attendance/calendar?month=${encodeURIComponent(v)}`);
    renderCalendar(v, days);
  } catch (e) {
    out('달력을 불러오지 못했습니다.');
  }
}

function clearToken() { setToken(''); }
// 로그인 안 되어 있으면 막기(옵션)
const isAuthed = () => !!getToken();
// if (!isAuthed()) { out('로그인 후 시작할 수 있어요.'); return; }

async function startSession(typeValue){
  const type = document.getElementById('gameType').value; // "COLOR_TAP"
  const res = await api('/sessions/start', {
    method: 'POST',
    body: JSON.stringify({
      gameType: type,   // ← 서버 DTO 필드명과 일치해야 함 (gameType)
      // meta: { difficulty: 'EASY' } // 필요시
    })
  });
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