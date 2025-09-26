(function(){
  async function waitSel(sel, tries=60){
    while(tries-- > 0){ const el=document.querySelector(sel); if(el) return el; await new Promise(r=>setTimeout(r,20)); }
    return null;
  }
  async function adjust(){
    const h = await waitSel('#appHeader');
    const f = await waitSel('#appFooter');
    const hh = h? Math.ceil(h.getBoundingClientRect().height): 64;
    const fh = f? Math.ceil(f.getBoundingClientRect().height): 72;
    document.documentElement.style.setProperty('--header-h', hh+'px');
    document.documentElement.style.setProperty('--footer-h', fh+'px');
  }
  addEventListener('resize', adjust);
  addEventListener('orientationchange', adjust);
  document.addEventListener('readystatechange', adjust);
  setTimeout(adjust,0); setTimeout(adjust,150); setTimeout(adjust,400);
  window.__adjustLayout = adjust;
})();
