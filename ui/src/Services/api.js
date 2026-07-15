export const API_BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

// Wrapper for backend calls: prefixes the API base URL and sends the
// JSON headers + session cookie every endpoint expects.
export const apiFetch = (path, options = {}) =>
  fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    ...options,
  });
