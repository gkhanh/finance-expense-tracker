export const API_CONFIG = {
    // If we are on port 4200 (Angular Dev Server), go directly to backend port 8080.
    // Otherwise (Docker/Production on port 80/443), use relative path so Nginx proxies it.
    apiUrl: window.location.port === '4200' 
        ? 'http://localhost:8080/api' 
        : '/api'
};