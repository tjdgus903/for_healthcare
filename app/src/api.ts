// 백엔드 API 래퍼
export async function api(path: string, options: RequestInit = {}) {
  const token = localStorage.getItem("jwt"); // Capacitor Preferences로 교체 가능
  const headers = {
    "Content-Type": "application/json",
    ...(token ? { "Authorization": `Bearer ${token}` } : {})
  };
  const res = await fetch(`/api${path}`, { ...options, headers });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function login(email: string, password: string) {
  const res = await api("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password })
  });
  localStorage.setItem("jwt", res.token); // Capacitor Preferences로 교체 예정
  return res;
}