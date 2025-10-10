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
    // [CHANGE] 한 개 타깃 원
    const dotTarget = document.getElementById('ctTarget');

    const elTime = document.getElementById('ctTime');
    const elScore = document.getElementById('ctScore');
    const elMsg = document.getElementById('ctMsg');
    const selLv = document.getElementById('ctLevel');

    const btnNoneTop = document.getElementById('ctNone');
    const btnNoneBottom = document.getElementById('ctNoneBottom'); // [CHANGE] 하단 큰 버튼
    const btnPause = document.getElementById('ctPause');
    const btnRestart = document.getElementById('ctRestart');

    // [CHANGE] 종료 오버레이 요소
    const endLayer = document.getElementById('ctEnd');
    const endScore = document.getElementById('ctEndScore');
    const endAcc = document.getElementById('ctEndAcc');
    const btnPlayAgain = document.getElementById('ctPlayAgain');

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

    // [CHANGE] 타깃 원 색상 칠하기
    function paintTarget(colorCss){
      if (dotTarget) dotTarget.style.background = colorCss;
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

        // [CHANGE] 게임 종료 시에만 서버 전송 (요약 데이터 1회)
        const endedAt = new Date().toISOString();
        const summary = {
          score,
          accuracy: acc,
          endedAt,
          total,
          correct,
          level
        };
        // 서버로 단 한 번 전송
        postSummary(sessionId, summary).catch(()=>{ /* 전송 실패 무시(옵션) */ });

        // 오버레이 표시
        showEnd(summary);

        hooks.onEnd?.(summary);
        return;
      }
      raf=requestAnimationFrame(tick);
    }

    // 라운드 생성
    function buildRound(){
      total++;
      target = pick(PALETTE);
      present = Math.random() > ABSENT_RATE;
      paintTarget(target.css);

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

    // [CHANGE] 진행 중 개별 메트릭 전송 제거 → 로컬 계산만
    function onPick(chosenKey, el){
      if(paused) return;
      const ok = (present && chosenKey===target.key);
      if(ok){ score+=10; correct++; el.classList.add('ok'); vibrate(20); }
      else  { el.classList.add('ng'); vibrate(50); }

      updateScore();
      setTimeout(buildRound, 110);
    }

    // 없음(타깃이 실제 없을 때 정답)
    function onNone(){
      if(paused) return;
      const ok = !present;
      if(ok){ score+=10; correct++; vibrate(20); } else vibrate(50);

      updateScore();
      setMsg(ok?'정답':'오답');
      setTimeout(buildRound, 120);
    }

    btnNoneTop.onclick = onNone;
    btnNoneBottom.onclick = onNone; // [CHANGE] 보조 버튼 동일 동작

    btnPause.onclick = ()=>{
      paused = !paused;
      btnPause.textContent = paused ? '▶' : '❙❙';
      stage.style.pointerEvents = paused ? 'none':'auto';
      if(!paused){ roundStart = performance.now(); }
    };

    btnRestart.onclick = ()=>{
      restart();
    };

    selLv?.addEventListener('change', ()=>{
      level = selLv.value || 'NORMAL';
      applyGrid(); buildRound();
    });

    // [CHANGE] 종료 오버레이 표시/재시작
    function showEnd({score, accuracy}){
      endScore.textContent = String(score);
      endAcc.textContent = `${Math.round((accuracy||0)*100)}%`;
      endLayer.hidden = false;
    }
    function hideEnd(){ endLayer.hidden = true; }
    btnPlayAgain.onclick = ()=>{
      hideEnd();
      restart();
    };

    function restart(){
      score=0; total=0; correct=0; paused=false;
      btnPause.textContent='❙❙';
      stage.style.pointerEvents='auto';
      t0=Date.now();
      hideEnd();
      updateScore(); buildRound();
      cancelAnimationFrame(raf);
      raf=requestAnimationFrame(tick);
    }

    // [CHANGE] 종료 요약 전송 전용 함수
    async function postSummary(sessionId, summary){
      // metrics(sessionId, items) 형태 유지. 서버에서 summary만 저장하도록 구성.
      // 예: [{ key:'summary', value:score, extra:{ ... } }]
      const payload = [
        { key:'summary', value: summary.score, extra: {
            accuracy: summary.accuracy,
            endedAt: summary.endedAt,
            total: summary.total,
            correct: summary.correct,
            level: summary.level
        } }
      ];
      await metrics(sessionId, payload);
    }

    // 초기화
    applyGrid(); updateScore(); buildRound();
    raf=requestAnimationFrame(tick);
  }
};