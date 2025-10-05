import { metrics } from '/js/game-bridge.esm.js';

/* 심플 팔레트(이미지처럼 3색) */
const PALETTE = [
  { key:'green', css:'#76c043' }, // 연두
  { key:'red',   css:'#e74c3c' }, // 레드
  { key:'blue',  css:'#3498db' }  // 블루
];

const ABSENT_RATE = 0.25; /* '없음' 라운드 확률 */
const rnd = (n)=>Math.floor(Math.random()*n);
const pick=(arr)=>arr[rnd(arr.length)];

function vibrate(ms){ try{ navigator.vibrate?.(ms); }catch{} }

export default {
  start(sessionId, hooks = {}) {
    const stage = document.getElementById('ctStage');
    const dots  = Array.from(document.querySelectorAll('.ct-min .ct-dot')); // 타깃 힌트 점 5개
    const elTime= document.getElementById('ctTime');
    const elScore=document.getElementById('ctScore');
    const elMsg = document.getElementById('ctMsg');
    const selLv = document.getElementById('ctLevel');
    const btnNone = document.getElementById('ctNone');
    const btnPause= document.getElementById('ctPause');
    const btnRestart=document.getElementById('ctRestart');

    let paused=false, t0=Date.now(), raf=0;
    let score=0, total=0, correct=0;
    let level = selLv?.value || 'NORMAL';
    let target = null, present=false, roundStart=0;

    // 레이아웃(난이도)
    function side(level){
      if(level==='EASY') return 2;
      if(level==='HARD') return 4;
      return 3;
    }
    function applyGrid(){
      const n=side(level);
      stage.style.gridTemplateColumns = `repeat(${n}, 1fr)`;
    }

    // 타겟 힌트(세로 점 색)
    function paintTargetDots(colorCss){
      dots.forEach(d=>{ d.style.background = colorCss; });
    }

    function setMsg(s){ elMsg.textContent=s||''; if(s) setTimeout(()=>elMsg.textContent='',900); }
    function updateScore(){
      const acc = total? (correct/total):0;
      hooks.onScore?.(score,acc);
      elScore.textContent = String(score);
    }

    // 타이머
    const TTL = 30;
    function tick(){
      if (paused) return raf=requestAnimationFrame(tick);
      const remain = Math.max(0, TTL - Math.floor((Date.now()-t0)/1000));
      elTime.textContent = String(remain);
      hooks.onTime?.(remain);
      if(remain<=0){
        cancelAnimationFrame(raf);
        const acc = total? +(correct/total).toFixed(3):0;
        hooks.onEnd?.({ score, accuracy: acc, meta:{ total, correct, palette:'GRB', absent:ABSENT_RATE, level }});
        return;
      }
      raf=requestAnimationFrame(tick);
    }

    // 라운드 생성
    function buildRound(){
      total++;
      target = pick(PALETTE);
      present = Math.random() > ABSENT_RATE;
      paintTargetDots(target.css);

      stage.innerHTML='';
      const n = side(level);
      const count = n*n;
      const cells = Array(count).fill(null);

      if(present) cells[rnd(count)] = target;
      for(let i=0;i<count;i++){
        if(!cells[i]){
          const pool = present ? PALETTE : PALETTE.filter(c=>c.key!==target.key);
          cells[i] = pick(pool);
        }
      }

      roundStart = performance.now();
      cells.forEach(color=>{
        const tile = document.createElement('button');
        tile.className='tile';
        tile.style.background = color.css;
        tile.style.borderColor = '#0f0f0f';
        tile.onclick = () => onPick(color.key, tile);
        stage.appendChild(tile);
      });
    }

    async function sendMetrics(items){
      try{ await metrics(sessionId, items); }catch{}
    }

    async function onPick(chosenKey, el){
      if(paused) return;
      const rt = Math.round(performance.now()-roundStart);
      const ok = (present && chosenKey===target.key);
      if(ok){ score+=10; correct++; el.classList.add('ok'); vibrate(20); }
      else  { el.classList.add('ng'); vibrate(50); }

      sendMetrics([
        { key:'reaction_ms', value:rt, unit:'ms' },
        { key: ok?'tap_correct':'tap_wrong', value:1, extra:{ chosen:chosenKey, target:target.key, present, level } }
      ]);

      updateScore();
      setTimeout(buildRound, 110);
    }

    // 없음(타깃이 실제 없을 때 정답)
    btnNone.onclick = ()=>{
      if(paused) return;
      const rt = Math.round(performance.now()-roundStart);
      const ok = !present;
      if(ok){ score+=10; correct++; vibrate(20); } else vibrate(50);

      sendMetrics([
        { key:'reaction_ms', value:rt, unit:'ms' },
        { key: ok?'tap_none_correct':'tap_none_wrong', value:1, extra:{ target:target.key, level } }
      ]);

      updateScore();
      setMsg(ok?'정답':'오답');
      setTimeout(buildRound, 120);
    };

    btnPause.onclick = ()=>{
      paused = !paused;
      btnPause.textContent = paused ? '▶' : '❙❙';
      stage.style.pointerEvents = paused ? 'none':'auto';
      if(!paused) { // 재개 시 라운드 타이밍 초기화
        roundStart = performance.now();
      }
    };

    btnRestart.onclick = ()=>{
      score=0; total=0; correct=0; paused=false;
      btnPause.textContent='❙❙';
      stage.style.pointerEvents='auto';
      t0=Date.now();
      updateScore(); buildRound();
    };

    selLv?.addEventListener('change', ()=>{
      level = selLv.value || 'NORMAL';
      applyGrid(); buildRound();
    });

    // 초기화
    applyGrid(); updateScore(); buildRound();
    raf=requestAnimationFrame(tick);
  }
};