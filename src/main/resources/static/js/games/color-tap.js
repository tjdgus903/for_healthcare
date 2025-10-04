import { metrics } from '/js/game-bridge.esm.js';

const COLORS = ['red','blue','green','orange','purple','black'];

// 라운드 중 '정답이 없는' 라운드가 나오게 할 확률 (0.0~1.0)
const ABSENT_RATE = 0.25;         // 25% 확률로 '정답 없음' 라운드

const rnd = (n) => Math.floor(Math.random() * n);
const choice = (arr) => arr[rnd(arr.length)];

export default {
  start(sessionId, hooks = {}) {
    const area = document.getElementById('gameArea');
    area.innerHTML = '';

    // 상단: 안내 + 타깃
    const hint = document.createElement('div');
    hint.className = 'mb-2';
    hint.innerHTML = `<div class="small text-muted">그리드에서 <b>지시된 색</b>을 탭하세요. <u>보이지 않으면 "없음"</u>을 누르세요.</div>`;
    area.appendChild(hint);

    const targetBox = document.createElement('div');
    targetBox.style.fontWeight = '700';
    targetBox.style.marginBottom = '8px';
    area.appendChild(targetBox);

    // 그리드
    const grid = document.createElement('div');
    grid.style.display = 'grid';
    grid.style.gridTemplateColumns = 'repeat(3, 80px)';
    grid.style.gap = '8px';
    area.appendChild(grid);

    // 하단: 없으면/다음 버튼 영역
    const actions = document.createElement('div');
    actions.className = 'mt-2 d-flex gap-2';
    area.appendChild(actions);

    const noneBtn = document.createElement('button');
    noneBtn.className = 'btn btn-outline-secondary';
    noneBtn.textContent = '없음';
    noneBtn.title = '보여지는 타깃 색이 없으면 누르세요';
    actions.appendChild(noneBtn);

    // 상태
    const ttlSec = 30;
    const tStart = Date.now();
    let score = 0, total = 0, correct = 0;
    let roundStart = 0;
    let current = { target: null, present: false }; // present: 타깃이 실제로 그리드에 존재하는지?

    // 타이머
    function tick() {
      const elapsed = Math.floor((Date.now() - tStart)/1000);
      const remain = Math.max(0, ttlSec - elapsed);
      hooks.onTime?.(remain);
      if (remain > 0) requestAnimationFrame(tick);
      else {
        const acc = total ? correct/total : 0;
        hooks.onEnd?.({
          score,
          accuracy: Number(acc.toFixed(3)),
          meta: { total, correct, absentRate: ABSENT_RATE }
        });
      }
    }
    requestAnimationFrame(tick);

    function updateScoreUI() {
      const acc = total ? correct/total : 0;
      hooks.onScore?.(score, acc);
    }

    function buildRound() {
      total++;
      // 1) 타깃 및 '존재여부' 결정
      const target = choice(COLORS);
      const present = Math.random() > ABSENT_RATE;  // true면 타깃 칸 1개 이상 존재

      current = { target, present };
      targetBox.textContent = `→ ${target.toUpperCase()} 를 눌러! (없으면 "없음")`;

      // 2) 그리드 생성
      grid.innerHTML = '';
      const cells = Array(9).fill(null);

      if (present) {
        // 최소 1칸은 타깃으로 보장
        const idx = rnd(9);
        cells[idx] = target;
      }

      // 나머지 칸 채우기 (present=false면 모든 칸이 타깃이 아닌 색만 나오게)
      for (let i = 0; i < 9; i++) {
        if (!cells[i]) {
          const pool = present ? COLORS : COLORS.filter(c => c !== target);
          cells[i] = choice(pool);
        }
      }

      // 렌더링 + 핸들러
      roundStart = performance.now();
      cells.forEach(color => {
        const btn = document.createElement('button');
        btn.style.width = '80px';
        btn.style.height = '80px';
        btn.style.borderRadius = '8px';
        btn.style.border = 'none';
        btn.style.background = color;
        btn.title = color;
        btn.setAttribute('aria-label', color);

        btn.onclick = async () => {
          const rt = Math.round(performance.now() - roundStart);
          const isCorrect = (color === current.target) && current.present;
          if (isCorrect) { score += 10; correct++; }

          // 지표
          metrics(sessionId, [
            { key: 'reaction_ms', value: rt, unit: 'ms' },
            { key: isCorrect ? 'tap_correct' : 'tap_wrong', value: 1, extra: { chosen: color, target: current.target, present: current.present } }
          ]).catch(()=>{});

          updateScoreUI();
          buildRound();
        };

        grid.appendChild(btn);
      });
    }

    // '없음' 버튼
    noneBtn.onclick = async () => {
      const rt = Math.round(performance.now() - roundStart);
      const isCorrect = (current.present === false);   // 타깃이 실제로 없던 라운드여야 정답
      if (isCorrect) { score += 10; correct++; }

      metrics(sessionId, [
        { key: 'reaction_ms', value: rt, unit: 'ms' },
        { key: isCorrect ? 'tap_none_correct' : 'tap_none_wrong', value: 1, extra: { target: current.target } }
      ]).catch(()=>{});

      updateScoreUI();
      buildRound();
    };

    // 첫 라운드 시작
    buildRound();
  }
};