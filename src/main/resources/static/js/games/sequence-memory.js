import { metrics } from '/js/game-bridge.esm.js';

// 5색 팔레트 (난이도에 따라 앞에서 N개만 사용)
const ALL_COLORS = [
  { key:'green',  cls:'c-green'  },
  { key:'red',    cls:'c-red'    },
  { key:'blue',   cls:'c-blue'   },
  { key:'yellow', cls:'c-yellow' },
  { key:'purple', cls:'c-purple' },
];

const rnd = (n) => Math.floor(Math.random() * n);
const choice = (arr) => arr[rnd(arr.length)];
const wait = (ms) => new Promise(r=>setTimeout(r,ms));

const SHOW_MS = 650;
const GAP_MS  = 200;

export default {
  start(sessionId, hooks = {}) {
    const $ = (id) => document.getElementById(id);
    // HUD
    const roundView = $('smemRound'), lenView = $('smemLen'),
          scoreView = $('smemScore'), timeView = $('smemTime');
    const btnStart = $('smemStart'), btnRestart = $('smemRestart'), btnPause = $('smemPause');

    // Controls
    const selColors  = $('smemColors');   // 3~5
    const selLimit   = $('smemLimit');    // 45/60/90/120
    const selReward  = $('smemReward');   // ×8/×10/×12
    const selPenalty = $('smemPenalty');  // -3/-5/-8

    // Stage
    const beads = $('smemBeads');
    const msg   = $('smemMsg');
    const padsHost = document.querySelector('.smem-pads');

    // 상태
    let COLORS = ALL_COLORS.slice(0, Number(selColors.value));
    let ttlSec = Number(selLimit.value);
    let rewardBase = Number(selReward.value);
    let penalty = Number(selPenalty.value);

    let startedAt = 0, timerId = 0, paused = false, playing = false;
    let score = 0, round = 1, seq = [], input = [];
    let lastStepAt = 0;

    // 패드 렌더
    function renderPads() {
      padsHost.innerHTML = '';
      COLORS.forEach(c => {
        const b = document.createElement('button');
        b.className = `pad ${c.cls}`;
        b.dataset.color = c.key;
        b.setAttribute('aria-label', c.key);
        b.addEventListener('click', () => handlePad(c.key));
        padsHost.appendChild(b);
      });
    }

    function syncSettings() {
      COLORS = ALL_COLORS.slice(0, Number(selColors.value));
      ttlSec = Number(selLimit.value);
      rewardBase = Number(selReward.value);
      penalty = Number(selPenalty.value);
    }

    [selColors, selLimit, selReward, selPenalty].forEach(el => {
      el.addEventListener('change', () => {
        const remain = Math.max(0, ttlSec - Math.floor((Date.now() - startedAt)/1000));
        syncSettings();
        renderPads();
        // 남은 시간 표시 보정(게임 중에도 헤더 숫자만 반영)
        if (startedAt) timeView.textContent = remain + 's';
      });
    });

    // 기본 UI
    function resetUI() {
      score = 0; round = 1; seq = []; input = [];
      paused = false; playing = false;
      roundView.textContent = '1';
      lenView.textContent = '1';
      scoreView.textContent = '0';
      msg.textContent = '시작을 누르면 시퀀스를 봅니다.';
      beads.innerHTML = '';
      renderPads();
    }
    resetUI();

    function renderBeads(n) {
      beads.innerHTML = '';
      for (let i=0;i<n;i++){
        const d = document.createElement('div');
        d.className = 'bead';
        beads.appendChild(d);
      }
    }

    async function playSequence() {
      playing = true;
      msg.textContent = '시퀀스를 잘 보세요…';
      renderBeads(seq.length);

      for (let i=0;i<seq.length;i++){
        if (paused) { i--; await wait(120); continue; }

        const bead = beads.children[i];
        bead.classList.add('on');

        const colorKey = seq[i];
        const pad = padsHost.querySelector(`[data-color="${colorKey}"]`);
        pad.classList.add('flash');

        // 지표: extra를 '객체'로 보냅니다 (문자열 X)
        metrics(sessionId, [
          { key:'seq_show', value:i+1, unit:'step', extra: { color: colorKey, round } }
        ]).catch(()=>{});

        await wait(SHOW_MS);
        bead.classList.remove('on');
        pad.classList.remove('flash');
        await wait(GAP_MS);
      }

      playing = false;
      input = [];
      msg.textContent = '이제 순서대로 눌러보세요!';
      lastStepAt = performance.now();
    }

    function extendSeq() {
      const colorKey = choice(COLORS).key;
      seq.push(colorKey);
      lenView.textContent = String(seq.length);
    }

    function handlePad(colorKey) {
      if (playing || paused || !startedAt) return;

      const need = seq[input.length];
      const rt = Math.round(performance.now() - lastStepAt);
      lastStepAt = performance.now();

      const ok = (colorKey === need);
      metrics(sessionId, [
        { key:'reaction_ms', value:rt, unit:'ms' },
        { key: ok ? 'seq_step_ok' : 'seq_step_ng', value:1, extra: { step: input.length+1, choose: colorKey, need, round } }
      ]).catch(()=>{});

      if (ok) {
        input.push(colorKey);
        if (input.length === seq.length) {
          // 라운드 성공: 길이 × 가점
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
        msg.textContent = '틀렸어요! 같은 시퀀스를 다시 시도합니다.';
        padsHost.classList.add('shake'); setTimeout(()=>padsHost.classList.remove('shake'), 250);
        playSequence();
      }
    }

    function endGame(byTimer=false) {
      cancelAnimationFrame(timerId);
      const acc = 1; // 누적 정답률 대신 라운드 성공 모델 → 1로 보고
      hooks.onEnd?.({ score, accuracy: acc, meta: {
        colors: COLORS.map(c=>c.key), seqLen: seq.length, round, byTimer, rewardBase, penalty, limitSec: ttlSec
      }});
    }

    function tick() {
      if (paused) return;
      const remain = Math.max(0, ttlSec - Math.floor((Date.now() - startedAt)/1000));
      timeView.textContent = remain + 's';
      hooks.onTime?.(remain);
      if (remain <= 0) endGame(true);
      else timerId = requestAnimationFrame(tick);
    }

    // 버튼들
    btnStart.onclick = async () => {
      syncSettings();
      resetUI();
      startedAt = Date.now();
      timeView.textContent = ttlSec + 's';
      extendSeq();
      cancelAnimationFrame(timerId);
      timerId = requestAnimationFrame(tick);
      await playSequence();
    };

    btnRestart.onclick = () => btnStart.click();

    btnPause.onclick = () => {
      if (!startedAt) return;
      paused = !paused;
      btnPause.textContent = paused ? '계속' : '일시정지';
      if (!paused) {
        lastStepAt = performance.now();
        timerId = requestAnimationFrame(tick);
      }
    };
  }
};