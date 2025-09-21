<script>
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

async function login(email,pw){
  const res = await api('/auth/login',{method:'POST', body: JSON.stringify({email,password:pw})});
  store.token = res.token; toast('로그인 성공');
  document.querySelectorAll('.need-auth').forEach(e=> e.style.display='');
}
function logout(){ store.token=null; toast('로그아웃됨'); location.href='/' }
function requireAuth(){ if(!store.token){ toast('로그인이 필요합니다'); location.href='/login.html' } }
</script>
