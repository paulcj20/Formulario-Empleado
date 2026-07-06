// Lee la configuración desde variables de entorno de CRA (prefijo REACT_APP_).
export const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

// Bandera para usar el adaptador mock (true) o el backend real (false).
export const USE_MOCK = (process.env.REACT_APP_USE_MOCK || 'true') === 'true';
