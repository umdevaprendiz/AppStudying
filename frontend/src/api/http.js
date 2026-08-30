export const API_BASE_URL = "";

function readCookie(name) {
  const match = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`));
  return match ? decodeURIComponent(match[1]) : null;
}

// O backend expõe o token CSRF via cookie (XSRF-TOKEN) legível por JS; toda
// requisição que muda estado precisa devolvê-lo no header X-XSRF-TOKEN. Ver
// SecurityConfig.java (CookieCsrfTokenRepository) e CsrfController.java.
export async function ensureCsrfCookie() {
  if (!readCookie("XSRF-TOKEN")) {
    await fetch(`${API_BASE_URL}/api/csrf`, { credentials: "include" });
  }
}

const SAFE_METHODS = new Set(["GET", "HEAD", "OPTIONS", "TRACE"]);

export async function request(method, path, { params, body } = {}) {
  let url = `${API_BASE_URL}${path}`;

  if (params) {
    const query = new URLSearchParams(
      Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== "")
    ).toString();
    if (query) url += `?${query}`;
  }

  const options = { method, headers: {}, credentials: "include" };
  if (body !== undefined) {
    options.headers["Content-Type"] = "application/json";
    options.body = JSON.stringify(body);
  }
  if (!SAFE_METHODS.has(method.toUpperCase())) {
    await ensureCsrfCookie();
    options.headers["X-XSRF-TOKEN"] = readCookie("XSRF-TOKEN");
  }

  const response = await fetch(url, options);
  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const message = (data && (data.message || data.error)) || text || `Erro ${response.status}`;
    throw new Error(message);
  }
  return data;
}
