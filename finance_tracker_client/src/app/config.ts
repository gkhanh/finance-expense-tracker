export const API_CONFIG = {
    // Use full URL for local development (Angular serve), relative path for Docker/Prod
    apiUrl: window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' 
        ? 'http://localhost:8080/api' 
        : '/api'
};