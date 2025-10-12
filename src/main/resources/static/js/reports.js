import { get } from '/js/frontend.esm.js';

const state = {
  charts: {}
};

function hhmmssFromSec(total) {
  const s = Math.max(0, Number(total) || 0);
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  const sec = Math.floor(s % 60);
  const pad = (n) => String(n).padStart(2, '0');
  return `${pad(h)}:${pad(m)}:${pad(sec)}`;
}

function percent(v) {
  if (v == null) return '-';
  return `${Math.round(v * 100)}%`;
}

function labelName(gameType) {
  // 필요 시 보기 좋은 이름으로 매핑
  switch (String(gameType)) {
    case 'COLOR_TAP': return '컬러 탭';
    case 'SEQUENCE_MEMORY': return '시퀀스 메모리';
    case 'SHAPE_MATCH': return '매칭 퍼즐';
    default: return String(gameType);
  }
}

function setText(id, text) {
  const el = document.getElementById(id);
  if (el) el.textContent = text;
}

function upsertChart(key, type, data, options) {
  const ctx = document.getElementById(key);
  if (!ctx) return;
  if (state.charts[key]) {
    state.charts[key].data = data;
    state.charts[key].options = options || {};
    state.charts[key].update();
  } else {
    state.charts[key] = new Chart(ctx, { type, data, options });
  }
}

function renderKPI(resp) {
  const t = resp?.totals;
  setText('kpiSessions', t?.sessions ?? '-');
  setText('kpiDuration', t ? hhmmssFromSec(t.totalDurationSec) : '-');
  setText('kpiAvgScore', t?.avgScore != null ? Math.round(t.avgScore) : '-');
  setText('kpiAvgAcc', t?.avgAccuracy != null ? percent(t.avgAccuracy) : '-');
}

function renderCharts(resp) {
  const rows = resp?.perGame || [];
  const labels = rows.map(r => labelName(r.gameType));
  const sessions = rows.map(r => r.sessions || 0);
  const avgScore = rows.map(r => Math.round(r.avgScore || 0));
  const avgAccPct = rows.map(r => Math.round((r.avgAccuracy || 0) * 100));
  const durationSec = rows.map(r => r.sumDurationSec || 0);

  // 1) 세션 수 (bar)
  upsertChart('chSessions', 'bar', {
    labels,
    datasets: [{ label: '세션 수', data: sessions }]
  }, {
    responsive: true,
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
  });

  // 2) 평균 점수 (bar)
  upsertChart('chAvgScore', 'bar', {
    labels,
    datasets: [{ label: '평균 점수', data: avgScore }]
  }, {
    responsive: true,
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true } }
  });

  // 3) 평균 정확도 (bar, %)
  upsertChart('chAvgAcc', 'bar', {
    labels,
    datasets: [{ label: '정확도(%)', data: avgAccPct }]
  }, {
    responsive: true,
    plugins: { legend: { display: false } },
    scales: {
      y: {
        beginAtZero: true,
        suggestedMax: 100,
        ticks: { callback: (v) => `${v}%` }
      }
    }
  });

  // 4) 플레이 시간 비중 (doughnut)
  upsertChart('chDuration', 'doughnut', {
    labels,
    datasets: [{ label: '플레이 시간(초)', data: durationSec }]
  }, {
    responsive: true,
    plugins: {
      legend: { position: 'bottom' },
      tooltip: {
        callbacks: {
          label: (ctx) => {
            const sec = ctx.parsed;
            return ` ${ctx.label}: ${hhmmssFromSec(sec)} (${sec}초)`;
          }
        }
      }
    }
  });
}

async function loadReport(days = 30) {
  const res = await get(`/reports/me?rangeDays=${encodeURIComponent(days)}`);
  const out = document.getElementById('out');
  if (out) out.textContent = JSON.stringify(res, null, 2);

  renderKPI(res);
  renderCharts(res);
}

function bindEvents() {
  const sel = document.getElementById('selRange');
  const btnReload = document.getElementById('btnReload');
  const btnPrint = document.getElementById('btnPrint');

  const reload = () => {
    const days = Number(sel?.value || 30);
    loadReport(days).catch(err => {
      console.error(err);
      alert('리포트를 불러오지 못했습니다.');
    });
  };

  btnReload?.addEventListener('click', reload);
  sel?.addEventListener('change', reload);
  btnPrint?.addEventListener('click', () => window.print());

  // 최초 1회 로드
  reload();
}

document.addEventListener('DOMContentLoaded', bindEvents);