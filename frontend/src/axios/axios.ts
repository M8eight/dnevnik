import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_BACKEND_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000,
});

//todo написать интерцепторы для авторизации

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // редирект на логин
    }
    return Promise.reject(error)
  }
)

export default api;