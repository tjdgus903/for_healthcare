import { api } from '/js/app.esm.js';

let startedAt = 0;

export async function start(gameType, meta) {
  const res = await api('/sessions/start', {
    method: 'POST',
    body: JSON.stringify({ gameType, meta })
  });
  const sid = res.sessionId || res.id;
  sessionStorage.setItem('sessionId', sid);
  startedAt = Date.now();
  return { sessionId: sid, startedAt: res.startedAt, gameType: res.gameType };
}

export async function metrics(sessionId, items) {
  return api(`/sessions/${sessionId}/metrics`, {
    method: 'POST',
    body: JSON.stringify({ metrics: items })
  });
}

export async function end(sessionId, { score, accuracy, durationSec, meta } = {}) {
  const dur = durationSec ?? Math.floor((Date.now() - startedAt) / 1000);
  return api(`/sessions/${sessionId}/end`, {
    method: 'POST',
    body: JSON.stringify({ score, accuracy, durationSec: dur, meta })
  });
}

export function safeEndOnLeave() {
  const sid = sessionStorage.getItem('sessionId');
  if (!sid) return;
  const dur = Math.floor((Date.now() - startedAt) / 1000);
  const payload = JSON.stringify({ durationSec: dur });
  if (navigator.sendBeacon) {
    navigator.sendBeacon(`/sessions/${sid}/end`, new Blob([payload], { type: 'application/json' }));
  } else {
    fetch(`/sessions/${sid}/end`, { method: 'POST', body: payload, headers: { 'Content-Type':'application/json' }, keepalive: true });
  }
}