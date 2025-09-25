const store = {
  get token(){ return localStorage.getItem('jwt') },
  set token(v){ v?localStorage.setItem('jwt',v):localStorage.removeItem('jwt') }
};

async function api(path, opts={}){
  const headers = Object.assign({'Content-Type':'application/json'}, opts.headers||{});
  if(store.token) headers['Authorization'] = 'Bearer ' + store.token;
  const res = await fetch(path, {...opts, headers});
  if(!res.ok){
    const t = await res.text().catch(()=>res.statusText);
    toast('오류: ' + (t||res.status));
    throw new Error(t||res.statusText);
  }
  const ct = res.headers.get('content-type')||'';
  return ct.includes('application/json') ? res.json() : res.text();
}

function toast(msg){
  let el = document.querySelector('.toast');
  if(!el){ el = document.createElement('div'); el.className='toast'; document.body.appendChild(el); }
  el.textContent = msg; el.style.display='block';
  setTimeout(()=> el.style.display='none', 2500);
}

function toggleHC(){
  const r=document.documentElement; r.setAttribute('data-theme', r.getAttribute('data-theme')==='hc'?'':'hc');
}

function requireAuth(){ if(!store.token){ toast('로그인이 필요합니다'); location.href='/login.html' } }

async function include(selector, url){
  const host = document.querySelector(selector);
  if(!host) return;
  const html = await fetch(url).then(r=>r.text());
  host.innerHTML = html;
}

// JWT payload 디코더 (email/role 읽기)
function decodeJwt(token){
  try{
    const b64 = token.split('.')[1].replace(/-/g,'+').replace(/_/g,'/');
    const json = decodeURIComponent(atob(b64).split('').map(c =>
      '%'+('00'+c.charCodeAt(0).toString(16)).slice(-2)
    ).join(''));
    return JSON.parse(json);
  }catch(e){ return null; }
}

// 현재 로그인 사용자 정보 (토큰에서 email/role 꺼내기)
function currentUser(){
  if(!store.token) return null;
  const p = decodeJwt(store.token);
  if(!p) return null;
  // 서버에서 email/role 클레임을 넣어줬으므로 그대로 사용
  return { id: p.sub, email: p.email || '', role: p.role || '' };
}

// 로그인/비로그인에 따라 버튼/계정 표시 토글
function applyAuthUI(){
  const authed = !!store.token;
  document.querySelectorAll('.need-auth').forEach(el => el.style.display = authed ? '' : 'none');
  document.querySelectorAll('.need-guest').forEach(el => el.style.display = authed ? 'none' : '');

  // 계정 표시 영역 업데이트
  const acct = document.getElementById('account');
  if (acct){
    if(authed){
      const u = currentUser();
      acct.textContent = u?.email ? `👤 ${u.email}` : '로그인됨';
      acct.title = u?.role ? `role: ${u.role}` : '';
    }else{
      acct.textContent = '';
      acct.removeAttribute('title');
    }
  }
}

// 로그인/로그아웃 시 UI 갱신하도록 호출
async function login(email,pw){
  const res = await api('/auth/login',{method:'POST', body: JSON.stringify({email,password:pw})});
  store.token = res.token; toast('로그인 성공');
  applyAuthUI();                // ← 추가
}
function logout(){
  store.token=null; toast('로그아웃됨');
  applyAuthUI();                // ← 추가
  location.href='/';
}