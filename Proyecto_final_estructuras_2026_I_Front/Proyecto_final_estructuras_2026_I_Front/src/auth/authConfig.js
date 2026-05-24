export const AUTH_ENDPOINTS = {
  login: import.meta.env.VITE_AUTH_LOGIN_ENDPOINT || '/usuarios/login',
};

export const AUTH_STORAGE_KEYS = {
  accessToken: 'fintech_auth_access_token',
  refreshToken: 'fintech_auth_refresh_token',
  user: 'fintech_auth_user',
};

export const AUTH_EVENTS = {
  sessionChanged: 'fintech:auth-session-changed',
  loggedOut: 'fintech:auth-logged-out',
};
