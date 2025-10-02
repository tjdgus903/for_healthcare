const store = {
  get token(){ return localStorage.getItem('jwt') },
  set token(v){ v ? localStorage.setItem('jwt', v) : localStorage.removeItem('jwt') }
};

function decodeJwt(token){
  try{
    const b64 = token.split('.')[1].replace(/-/g,'+').replace(/_/g,'/');
    const json = decodeURIComponent(atob(b64).split('').map(c =>
      '%'+('00'+c.charCodeAt(0).toString(16)).slice(-2)
    ).join(''));
    return JSON.parse(json);
  }catch(e){ return null; }
}

function currentUser(){
  if(!store.token) return null;
  const p = decodeJwt(store.token);
  return p ? { id: p.sub, email: p.email || '', role: p.role || '' } : null;
}

// 헤더 토글 핵심
function applyAuthUI(){
  const authed = !!store.token;

  document.querySelectorAll('.need-auth')
    .forEach(el => el.style.display = authed ? '' : 'none');

  document.querySelectorAll('.need-guest')
    .forEach(el => el.style.display = authed ? 'none' : '');

  const acct = document.getElementById('account');
  if (acct){
    if (authed){
      const u = currentUser();
      acct.textContent = u?.email ? `👤 ${u.email}` : '로그인됨';
      acct.title = u?.role ? `role: ${u.role}` : '';
    } else {
      acct.textContent = '';
      acct.removeAttribute('title');
    }
  }
}

// 선택: 고대비 버튼
function toggleHC(){
  const r=document.documentElement;
  r.setAttribute('data-theme', r.getAttribute('data-theme')==='hc'?'':'hc');
}

// 전역 노출
window.applyAuthUI = applyAuthUI;
window.toggleHC = toggleHC;

// 페이지가 로드될 때 한 번 반영
document.addEventListener('DOMContentLoaded', () => applyAuthUI());