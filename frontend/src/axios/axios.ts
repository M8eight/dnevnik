import { keycloak } from '@/lib/keycloak';
import { store } from '@/store';
import { selectHasRole } from '@/store/slices/authSlice';
import { selectSelectedStudentId } from '@/store/slices/selectedStudentSlice';
import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_BACKEND_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000,
});

api.interceptors.request.use( async (config) => {
  if (keycloak.token) {
    await keycloak.updateToken(30).catch(() => {keycloak.login()});
    config.headers.Authorization = `Bearer ${keycloak.token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      keycloak.login();
    }
    return Promise.reject(error)
  }
)

api.interceptors.request.use( async (config) => {
  if (selectHasRole("PARENT")) {
    const studentId = selectSelectedStudentId(store.getState());
    if (studentId) {
      config.headers["X-Student-Id"] = String(studentId);
    }
  }
  return config;
});

export default api;