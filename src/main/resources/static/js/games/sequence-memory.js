// /js/games/sequence-memory.js
import { metrics } from '/js/game-bridge.esm.js';

const ALL_COLORS = [
  { key: 'blue',   cls: 'c-blue'   },
  { key: 'green',  cls: 'c-green'  },
  { key: 'yellow', cls: 'c-yellow' },
  { key: 'red',    cls: 'c-red'    },
  { key: 'purple', cls: 'c-purple' },
];

const rnd    = (n) => Math.floor(Math.random() * n);
const wait   = (ms) => new Promise(r => setTimeout(r, ms));
const nowms  = () => (performance?.now?.() ?? Date.now());

const SHOW_MS = 520;
const GAP_MS  = 140;

export default {
  start(sessionId, hooks = {}) {
    // 바인딩
    const $  = (id) => document.getElementById(id);
    const qs = (sel)=> document.querySelector(sel);

    const roundView = $('round');
    const lenView   = $('len');
    const scoreView = $('scoreView');   // play.html 상단 HUD
    const timeView  = $('timeView');    // play.html 상단 HUD
    const statusEl  = $('statusText');

    const btnStart   = $('btnStart');   // play.html 상단 버튼
    const btnRestart = $('btnRestart'); // 파셜 버튼
    const btnPause   = $('btnPause');   // 파셜 버튼

    const selColors  = $('colors');
    const selLimit   = $('limit');
    const selReward  = $('reward');
    const selPenalty = $('penalty');

    const beads    = $('beads');
    const padsHost = $('pads');

    // 필수 노드 체크
    const need = [roundView, lenView, scoreView, timeView, statusEl, btnStart, btnRestart, btnPause, selColors, selLimit, selReward, selPenalty, beads, padsHost];
    if (need.some(n => !n)) {
      console.error('[SEQUENCE] 필수 노드가 없습니다.', { roundView, lenView, scoreView, timeView, statusEl, btnStart, btnRestart, btnPause, selColors, selLimit, selReward, selPenalty, beads, padsHost });
      statusEl.textContent = '구성 요소 누락으로 실행할 수 없습니다.';
      return;
    }

    // 상태
    let COLORS     = ALL_COLORS.slice(0, Number(selColors.value || 4));
    let ttlSec     = Number(selLimit.value || 60);
    let rewardBase = Number(selReward.value || 10);
    let penalty    = Number(selPenalty.value || 5);

    let startedAt = 0;
    let rafId     = 0;
    let paused    = false;
    let playing   = false;   // 시퀀스 재생 중
    let accepting = false;   // 사용자 입력 허용

    let score = 0;
    let round = 1;
    let seq   = [];
    let step  = 0;
    let lastStepAt = 0;

    // 유틸
    const setMsg = (t) => { statusEl.textContent = t; };
    const setPadsDisabled = (v) => padsHost.querySelectorAll('.pad').forEach(p => p.disabled = !!v);

    const flashPad = (btn) => {
      if (!btn) return;
      const old = btn.style.filter;
      btn.style.filter = 'brightness(1.25) saturate(1.15)';
      setTimeout(() => (btn.style.filter = old), 200);
    };

    // 렌더
    const labels = ['A','B','C','D','E'];

    function setPadsColumns(){
      const cols = Math.min(COLORS.length, 5);
      padsHost.style.display='grid';
      padsHost.style.gridTemplateColumns = `repeat(${cols}, minmax(120px, 1fr))`;
      padsHost.style.gap = '18px';
      padsHost.style.justifyContent = 'center';
    }

    function renderPads(){
      padsHost.innerHTML='';
      setPadsColumns();
      COLORS.forEach((c,i)=>{
        const b = document.createElement('button');
        b.type='button';
        b.className = `pad ${c.cls}`;
        b.dataset.color = c.key;
        b.setAttribute('aria-label', c.key);
        b.innerHTML = `<span class="lbl">${labels[i] ?? ''}</span>`;
        b.addEventListener('click', ()=> onPress(c.key, b));
        padsHost.appendChild(b);
      });
    }

    function renderBeads(n){
      beads.innerHTML='';
      for (let i=0;i<n;i++){
        const d=document.createElement('div');
        d.className='bead';
        beads.appendChild(d);
      }
    }

    function syncSettings() {
      COLORS     = ALL_COLORS.slice(0, Number(selColors.value || COLORS.length));
      ttlSec     = Number(selLimit.value   || ttlSec);
      rewardBase = Number(selReward.value  || rewardBase);
      penalty    = Number(selPenalty.value || penalty);
      renderPads();
    }

    // 게임 로직
    function resetUI(){
      score=0; round=1; seq=[]; step=0;
      paused=false; playing=false; accepting=false;
      roundView.textContent='1';
      lenView.textContent='1';
      scoreView.textContent='0';
      timeView.textContent=`${ttlSec}s`;
      setMsg('시작을 누르면 시퀀스가 재생됩니다.');
      beads.innerHTML='';
      renderPads();
      setPadsDisabled(true);
    }

    function extendSeq(){
      const next = COLORS[rnd(COLORS.length)].key;
      seq.push(next);
      lenView.textContent = String(seq.length);
    }

    async function playSequence(){
      playing=true; accepting=false; setPadsDisabled(true);
      setMsg('시퀀스를 잘 보세요…');
      renderBeads(seq.length);

      for (let i=0;i<seq.length;i++){
        while (paused) { await wait(100); }
        const bead = beads.children[i];
        bead?.classList.add('on');

        const key = seq[i];
        const btn = padsHost.querySelector(`[data-color="${key}"]`);
        flashPad(btn);

        metrics(sessionId, [
          { key:'seq_show', value:i+1, unit:'step', extra:{ color:key, round } }
        ]).catch(()=>{});

        await wait(SHOW_MS);
        bead?.classList.remove('on');
        await wait(GAP_MS);
      }

      playing=false; accepting=true; setPadsDisabled(false);
      setMsg('이제 순서대로 눌러보세요!');
      step=0; lastStepAt = nowms();
    }

    function onPress(colorKey, btn){
      if (!accepting || paused || !startedAt) return;

      flashPad(btn);

      const need = seq[step];
      const rt = Math.round(nowms() - lastStepAt);
      lastStepAt = nowms();

      const ok = (colorKey === need);
      metrics(sessionId, [
        { key:'reaction_ms', value:rt, unit:'ms' },
        { key: ok ? 'seq_step_ok' : 'seq_step_ng', value:1, extra:{ step:step+1, choose:colorKey, need, round } }
      ]).catch(()=>{});

      if (ok){
        step++;
        if (step >= seq.length){
          score += rewardBase * seq.length;
          scoreView.textContent = String(score);
          hooks.onScore?.(score, 1);

          round++;
          roundView.textContent = String(round);
          extendSeq();
          playSequence();
        }
      } else {
        score = Math.max(0, score - penalty);
        scoreView.textContent = String(score);
        setMsg('틀렸어요! 같은 시퀀스를 다시 시도합니다.');
        padsHost.classList.remove('shake'); void padsHost.offsetWidth; padsHost.classList.add('shake');
        playSequence();
      }
    }

    function remainSec(){
      const gone = Math.floor((Date.now() - startedAt)/1000);
      return Math.max(0, ttlSec - gone);
    }

    function endGame(byTimer=false){
      cancelAnimationFrame(rafId);
      accepting=false; playing=false; setPadsDisabled(true);
      hooks.onEnd?.({
        score,
        accuracy: 1,
        meta:{
          colors: COLORS.map(c=>c.key),
          seqLen: seq.length,
          round,
          byTimer,
          rewardBase,
          penalty,
          limitSec: ttlSec
        }
      });
    }

    function tick(){
      if (!startedAt) return;
      if (paused){ rafId = requestAnimationFrame(tick); return; }
      const r = remainSec();
      timeView.textContent = `${r}s`;
      hooks.onTime?.(r);
      if (r <= 0){
        setMsg('시간 종료! 다시를 눌러 재시작하세요.');
        endGame(true);
      } else {
        rafId = requestAnimationFrame(tick);
      }
    }

    // 이벤트
    [selColors, selLimit, selReward, selPenalty].forEach(el=>{
      el.addEventListener('change', ()=>{
        const r = startedAt ? remainSec() : ttlSec;
        syncSettings();
        if (startedAt) timeView.textContent = `${r}s`;
      });
    });

    btnStart.addEventListener('click', async ()=>{
      syncSettings();
      resetUI();
      startedAt = Date.now();
      timeView.textContent = `${ttlSec}s`;
      cancelAnimationFrame(rafId);
      rafId = requestAnimationFrame(tick);
      extendSeq();
      await playSequence();
    });

    btnRestart.addEventListener('click', ()=> btnStart.click());

    btnPause.addEventListener('click', ()=>{
      if (!startedAt) return;
      paused = !paused;
      btnPause.textContent = paused ? '재개' : '일시정지';
      if (!paused) lastStepAt = nowms();
    });

    // 초기 세팅
    resetUI();
  }
};