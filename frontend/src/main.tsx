import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Provider } from 'react-redux';
import './index.css'
import App from './App.tsx'
import { store } from './store/index.ts'
import { keycloak } from './lib/keycloak.ts';
import { setAuth } from './store/slices/authSlice.ts';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,
      retry: 2,
    },
  }
})

keycloak.init({
  onLoad: 'check-sso',
  silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
  pkceMethod: 'S256',
})
  .then((auth) => {
    if (auth) {
      store.dispatch(setAuth({
        username: keycloak.tokenParsed?.preferred_username ?? null,
        roles: keycloak.tokenParsed?.realm_access?.roles ?? [],
        user_id: keycloak.tokenParsed?.user_id ?? null
      }));
    }

    createRoot(document.getElementById('root')!).render(
      <StrictMode>
        <Provider store={store}>
          <QueryClientProvider client={queryClient}>
            <App />
          </QueryClientProvider>
        </Provider>
      </StrictMode>
    )

  })
  .catch((error) => {
    console.error("Keycloak init error", error);

    createRoot(document.getElementById('root')!).render(
      <StrictMode>
        <div>Ошибка инициализации авторизации. Попробуйте перезагрузить страницу.</div>
      </StrictMode>
    )
  });

setInterval(() => {
  if (keycloak.authenticated) {
    keycloak.updateToken(30).catch(() => {
      keycloak.login();
    });
  }
}, 30000)    