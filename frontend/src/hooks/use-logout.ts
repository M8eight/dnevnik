import { keycloak } from "@/lib/keycloak";
import { store } from "@/store";
import { clearAuth } from "@/store/slices/authSlice";


export const clearLogout = () => {

    store.dispatch(clearAuth());
    localStorage.clear();
    sessionStorage.clear();

    if (keycloak) {
        keycloak.logout({
            redirectUri: window.location.origin + "/login",
        }).catch(() => {
            window.location.href = "/login";
        });
    } else {
        window.location.href = "/login";
    }
    
};