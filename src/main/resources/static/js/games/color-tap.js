// /js/games/color-tap.js
import { metrics } from '/js/game-bridge.esm.js';

const COLORS = ['red','blue','green','orange','purple','black'];
const rnd = (n) => Math.floor(Math.random() * n);

export default {
  start(sessionId, hooks = {}) {
    const area = document.getElementById('gameArea');

    const targetBox = area.querySelector('#ct-target') || (() => {
      const el = document.createElement('div');
      el.id = 'ct-target';
      el.className = 'fw-bold mb-2';
      area.appendChild(el);
      return el;
    })();

    const grid = area.querySelector('#ct-grid') || (() => {
      const el = document.createElement('div');
      el.id = 'ct-grid';
      el.className = 'd-grid gap-2';
      el.style.gridTemplateColumns = 'repeat(3, 80px)';
      area.appendChild(el);
      return el;
    })();

    let score = 0, total = 0, correct = 0;
    const ttlSec = 30;
    const tStart = Date.now();

    function tick() {
      const elapsed = Math.floor((Date.now() - tStart)/1000);
      const remain = Math.max(0, ttlSec - elapsed);
      hooks.onTime?.(remain);
      if (remain > 0) requestAnimationFrame(tick);
      else {
        const acc = total ? correct/total : 0;
        hooks.onEnd?.({ score, accuracy: Number(acc.toFixed(3)), meta: { total, correct } });
      }
    }
    requestAnimationFrame(tick);

    function newRound() {
      total++;
      const target = COLORS[rnd(COLORS.length)];
      targetBox.textContent = `→ ${target.toUpperCase()} 를 눌러!`;

      grid.innerHTML = '';
      const started = performance.now();

      for (let i=0; i<9; i++) {
        const color = COLORS[rnd(COLORS.length)];
        const btn = document.createElement('button');
        btn.style.width = '80px';
        btn.style.height = '80px';
        btn.style.borderRadius = '8px';
        btn.style.border = 'none';
        btn.style.background = color;
        btn.title = color;

        btn.onclick = async () => {
          const rt = Math.round(performance.now() - started);
          const isCorrect = (color === target);
          if (isCorrect) { score += 10; correct++; }
          metrics(sessionId, [
            { key: 'reaction_ms', value: rt, unit: 'ms' },
            { key: isCorrect ? 'tap_correct' : 'tap_wrong', value: 1 }
          ]).catch(()=>{});
          const acc = total ? correct/total : 0;
          hooks.onScore?.(score, acc);
          newRound();
        };

        grid.appendChild(btn);
      }
    }

    newRound();
  }
}