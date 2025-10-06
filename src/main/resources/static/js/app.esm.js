export const getToken = () => localStorage.getItem('jwt') || '';
export const setToken = (t) => localStorage.setItem('jwt', t || '');
export const isAuthed  = () => !!getToken();

export async function api(path, opts = {}) {
  const headers = Object.assign({'Content-Type':'application/json'}, opts.headers || {});
  const jwt = getToken();
  if (jwt) headers['Authorization'] = 'Bearer ' + jwt;

  const res = await fetch(path, { ...opts, headers, credentials: 'include' });
  const ct = res.headers.get('content-type') || '';
  const body = ct.includes('application/json') ? await res.json() : await res.text();

  if (!res.ok) {
    // 에러 객체에 status를 실어둠 (401/403 분기용)
    const err = new Error(typeof body === 'string' ? body : JSON.stringify(body));
    err.status = res.status;
    throw err;
  }
  return body;
}

const store = {
  get token(){ return localStorage.getItem('jwt') },
  set token(v){ v ? localStorage.setItem('jwt', v) : localStorage.removeItem('jwt') }
};

function toast(msg){
  let el = document.querySelector('.toast');
  if(!el){
    el = document.createElement('div');
    el.className = 'toast';
    el.style.position = 'fixed';
    el.style.left = '50%';
    el.style.bottom = '24px';
    el.style.transform = 'translateX(-50%)';
    el.style.background = '#333';
    el.style.color = '#fff';
    el.style.padding = '10px 14px';
    el.style.borderRadius = '8px';
    el.style.zIndex = '9999';
    document.body.appendChild(el);
  }
  el.textContent = msg;
  el.style.display = 'block';
  setTimeout(()=> el.style.display='none', 2000);
}

function decodeJwt(token){
  try{
    const b64 = token.split('.')[1].replace(/-/g,'+').replace(/_/g,'/');
    const json = decodeURIComponent(atob(b64).split('').map(c =>
      '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    ).join(''));
    return JSON.parse(json);
  }catch(e){ return null; }
}

function currentUser(){
  if(!store.token) return null;
  const p = decodeJwt(store.token);
  if(!p) return null;
  return { id: p.sub, email: p.email || '', role: p.role || '' };
}

function applyAuthUI(){
  const authed = !!store.token;

  document.querySelectorAll('.need-auth')
    .forEach(el => el.style.display = authed ? '' : 'none');
  document.querySelectorAll('.need-guest')
    .forEach(el => el.style.display = authed ? 'none' : '');

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

function toggleHC(){
  const r=document.documentElement;
  r.setAttribute('data-theme', r.getAttribute('data-theme')==='hc' ? '' : 'hc');
}

// ===== API 래퍼 =====
async function api(path, opts = {}){
  const headers = Object.assign({'Content-Type':'application/json'}, opts.headers || {});
  if (store.token) headers['Authorization'] = 'Bearer ' + store.token;

  const res = await fetch(path, { ...opts, headers, credentials: 'include' });
  const ct = res.headers.get('content-type') || '';
  const body = ct.includes('application/json') ? await res.json() : await res.text();

  if (!res.ok) {
    toast('오류: ' + (typeof body === 'string' ? body : JSON.stringify(body)));
    throw new Error(res.status + ' ' + (typeof body === 'string' ? body : JSON.stringify(body)));
  }
  return body;
}

// ===== 로그인/로그아웃 =====
async function doLogin(){
  const email = document.getElementById('email')?.value?.trim() || '';
  const password = document.getElementById('password')?.value || '';

  const res = await api('/auth/login',{
    method:'POST',
    body: JSON.stringify({ email, password })
  });

  store.token = res.token;
  applyAuthUI();

  const params = new URLSearchParams(location.search);
  const next = params.get('next') || '/';
  location.replace(next);
}

function logout(){
  store.token = null;
  applyAuthUI();
  location.replace('/');
}

export { store, api, toast, toggleHC, decodeJwt, currentUser, applyAuthUI, doLogin, logout };

if (typeof window !== 'undefined') {
  window.doLogin = doLogin;
  window.logout  = logout;
  window.toggleHC = toggleHC;
  window.applyAuthUI = applyAuthUI; // header 프래그먼트에서 호출
}