// ===== common.js =====
const API = (() => {
  const LS_KEY = 'hc_token';

  function setToken(t) { localStorage.setItem(LS_KEY, t); }
  function getToken() { return localStorage.getItem(LS_KEY); }
  function clearToken() { localStorage.removeItem(LS_KEY); }

  async function request(path, { method='GET', body, headers={} } = {}) {
    const h = { 'Content-Type': 'application/json', ...headers };
    const token = getToken();
    if (token) h['Authorization'] = 'Bearer ' + token;

    const res = await fetch(path, { method, headers: h, body: body ? JSON.stringify(body) : undefined });
    if (!res.ok) {
      const text = await res.text();
      throw new Error(`${res.status} ${res.statusText} :: ${text}`);
    }
    const ct = res.headers.get('content-type') || '';
    return ct.includes('application/json') ? res.json() : res.text();
  }

  async function login(id, password) {
    const data = await request('/auth/login', { method: 'POST', body: { id, password } });
    setToken(data.accessToken);
    return data;
  }

  async function startSession(gameType, meta) {
    return request('/sessions/start', { method: 'POST', body: { gameType, meta } });
  }

  async function addMetrics(sessionId, metrics) {
    return request(`/sessions/${sessionId}/metrics`, { method: 'POST', body: { metrics } });
  }

  async function endSession(sessionId, payload) {
    return request(`/sessions/${sessionId}/end`, { method: 'POST', body: payload });
  }

  return { login, startSession, addMetrics, endSession, getToken, clearToken, request };
})();