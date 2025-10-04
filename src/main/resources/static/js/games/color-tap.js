import { metrics } from '/js/game-bridge.esm.js';

// 팔레트(명도 대비 높은 색)
const COLORS = [
  { key:'red',    label:'RED',    css:'#e53935' },
  { key:'blue',   label:'BLUE',   css:'#1e88e5' },
  { key:'green',  label:'GREEN',  css:'#43a047' },
  { key:'orange', label:'ORANGE', css:'#fb8c00' },
  { key:'purple', label:'PURPLE', css:'#8e24aa' },
  { key:'black',  label:'BLACK',  css:'#111111' },
];

const ABSENT_RATE_DEFAULT = 0.25; // 25% 확률로 '정답 없음' 라운드
const rnd = (n) => Math.floor(Math.random()*n);
const pick = (arr) => arr[rnd(arr.length)];

function vibrate(ms){ try{ navigator.vibrate?.(ms); }catch{} }
function playBeep(on) {
  if (!on || typeof window.AudioContext === 'undefined') return () => {};
  const ctx = new AudioContext();
  return (ok=true) => {
    const o = ctx.createOscillator();
    const g = ctx.createGain();
    o.type = 'sine';
    o.frequency.value = ok ? 880 : 240;
    g.gain.value = 0.04;
    o.connect(g).connect(ctx.destination);
    o.start();
    setTimeout(()=>{ o.stop(); }, ok? 120:180);
  };
}

export default {
  /**
   * 시작 진입점
   * @param {string} sessionId
   * @param {{onScore?:(s:number,a:number)=>void, onTime?:(sec:number)=>void, onEnd?:(payload)=>void}} hooks
   */
  start(sessionId, hooks = {}) {
    // 파셜 요소 찾기
    const gridEl     = document.getElementById('ctapGrid');
    const targetChip = document.getElementById('ctapTargetChip');
    const targetText = document.getElementById('ctapTargetText');
    const selLevel   = document.getElementById('ctapLevel');
    const selOverlay = document.getElementById('ctapOverlay');
    const selSound   = document.getElementById('ctapSound');
    const btnNone    = document.getElementById('ctapNone');
    const btnPause   = document.getElementById('ctapPause');
    const btnRestart = document.getElementById('ctapRestart');
    const msgEl      = document.getElementById('ctapMessage');

    if (!gridEl) { console.error('color-tap: grid not found'); return; }

    // 상태
    const TTL = 30; // 총 플레이 시간(초)
    let absentRate = ABSENT_RATE_DEFAULT;
    let t0 = Date.now();
    let raf = 0;
    let paused = false;

    let score = 0, total = 0, correct = 0;
    let roundStart = 0;
    let current = { target:null, present:false, level:'NORMAL', overlay:'TEXT', sound:'OFF' };

    // 사운드 준비
    const beep = playBeep(true);

    // 레벨에 따른 그리드 크기
    function gridCols(level) {
      switch(level){
        case 'EASY':   return 2;
        case 'HARD':   return 4;
        case 'NORMAL':
        default:       return 3;
      }
    }

    function setGridTemplate(level){
      const cols = gridCols(level);
      gridEl.style.gridTemplateColumns = `repeat(${cols}, minmax(90px, 1fr))`;
    }

    function showMsg(s) {
      msgEl.textContent = s || '';
      if (s) setTimeout(() => { msgEl.textContent = ''; }, 1200);
    }

    function updateScore() {
      const acc = total ? correct/total : 0;
      hooks.onScore?.(score, acc);
    }

    function setTargetUI(color) {
      targetChip.style.background = color.css;
      targetText.textContent = color.label;
    }

    // 타이머
    function tick(){
      if (paused) { raf = requestAnimationFrame(tick); return; }
      const remain = Math.max(0, TTL - Math.floor((Date.now()-t0)/1000));
      hooks.onTime?.(remain);
      if (remain <= 0) {
        cancelAnimationFrame(raf);
        const acc = total ? correct/total : 0;
        hooks.onEnd?.({ score, accuracy: +acc.toFixed(3), meta:{ total, correct, absentRate } });
        return;
      }
      raf = requestAnimationFrame(tick);
    }

    // 라운드 생성
    function buildRound() {
      total++;
      const target = pick(COLORS);
      const present = Math.random() > absentRate; // false면 '없음'이 정답인 라운드

      current.target  = target;
      current.present = present;

      setTargetUI(target);

      // 그리드 채우기
      gridEl.innerHTML = '';
      const cols = gridCols(current.level);
      const cellCount = cols * cols;
      const cells = Array(cellCount).fill(null);

      if (present) {
        cells[rnd(cellCount)] = target;
      }
      for (let i=0; i<cellCount; i++) {
        if (!cells[i]) {
          const pool = present ? COLORS : COLORS.filter(c => c.key !== target.key);
          cells[i] = pick(pool);
        }
      }

      roundStart = performance.now();
      cells.forEach(color => {
        const btn = document.createElement('button');
        btn.className = 'ctap-cell';
        btn.style.background = color.css;
        btn.setAttribute('aria-label', color.label);

        // 오버레이
        const badge = document.createElement('span');
        badge.className = 'ctap-badge';
        if (current.overlay === 'TEXT') {
          badge.textContent = color.label;
        } else if (current.overlay === 'ICON') {
          // 간단한 도형(●) — 고대비에서 가독성 ↑
          badge.textContent = '●';
        } else {
          badge.classList.add('ctap-ghost'); // 숨김
        }
        btn.appendChild(badge);

        btn.onclick = () => handlePick(color.key, btn);
        gridEl.appendChild(btn);
      });
    }

    // 선택 처리
    async function handlePick(chosenKey, cellEl) {
      if (paused) return;
      const rt = Math.round(performance.now() - roundStart);
      const ok = (current.present && chosenKey === current.target.key);

      if (ok) {
        score += 10; correct++;
        cellEl.classList.add('ctap-correct');
        vibrate(20); beep(true);
      } else {
        cellEl.classList.add('ctap-wrong');
        vibrate(60); beep(false);
      }

      try {
        await metrics(sessionId, [
          { key:'reaction_ms', value:rt, unit:'ms' },
          {
            key: ok ? 'tap_correct' : 'tap_wrong',
            value:1,
            // 서버는 extra를 JSON 문자열 or jsonb로 받도록 만들었음 → 객체를 그대로 주면 Jackson이 string으로 직렬화함
            extra: { chosen: chosenKey, target: current.target.key, present: current.present, level: current.level }
          }
        ]);
      } catch {}

      updateScore();
      setTimeout(() => buildRound(), 120); // 짧은 피드백 후 다음 라운드
    }

    // 없음 버튼
    btnNone.onclick = async () => {
      if (paused) return;
      const rt = Math.round(performance.now() - roundStart);
      const ok = !current.present;

      if (ok) { score += 10; correct++; vibrate(20); beep(true); }
      else    { vibrate(60); beep(false); }

      try {
        await metrics(sessionId, [
          { key:'reaction_ms', value:rt, unit:'ms' },
          { key: ok ? 'tap_none_correct' : 'tap_none_wrong', value:1, extra:{ target: current.target.key, level: current.level } }
        ]);
      } catch {}

      showMsg(ok ? '정답! (없음)' : '오답이에요');
      updateScore();
      setTimeout(() => buildRound(), 140);
    };

    // 일시정지/재개
    btnPause.onclick = () => {
      paused = !paused;
      btnPause.textContent = paused ? '재개' : '일시정지';
      gridEl.style.pointerEvents = paused ? 'none' : 'auto';
      showMsg(paused ? '일시정지됨' : '');
    };

    // 다시 시작
    btnRestart.onclick = () => {
      score = 0; total = 0; correct = 0;
      t0 = Date.now();
      paused = false;
      btnPause.textContent = '일시정지';
      gridEl.style.pointerEvents = 'auto';
      updateScore();
      buildRound();
    };

    // 옵션 바인딩
    function applyOptions() {
      current.level   = selLevel?.value   || 'NORMAL';
      current.overlay = selOverlay?.value || 'TEXT';
      current.sound   = selSound?.value   || 'OFF';
      setGridTemplate(current.level);
    }

    selLevel?.addEventListener('change', () => { applyOptions(); buildRound(); });
    selOverlay?.addEventListener('change', () => { applyOptions(); buildRound(); });
    selSound?.addEventListener('change', () => { /* 현재는 비프 on/off만 → 상단의 playBeep 호출을 제어하려면 필요 시 개선 */ });

    // 초기 세팅
    applyOptions();
    updateScore();
    buildRound();
    raf = requestAnimationFrame(tick);
  }
};