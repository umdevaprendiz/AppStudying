import i18n from "../i18n";

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
  // Sobrescreve o Accept-Language automático do navegador com o idioma
  // escolhido na interface — é o que permite o GlobalExceptionHandler do
  // backend devolver a mensagem de erro já traduzida (ver
  // ErrorMessageTranslations no backend).
  options.headers["Accept-Language"] = i18n.language || "en";
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
    const message = (data && (data.message || data.error)) || text || i18n.t("common.genericHttpError", { status: response.status });
    const error = new Error(message);
    error.status = response.status;
    throw error;
  }
  return data;
}
