import { keycloak } from "@/lib/keycloak";
// import { store } from "@/store";
// import { clearAuth } from "@/store/slices/authSlice";


export const clearLogout = () => {

    // store.dispatch(clearAuth());
    // localStorage.clear();
    // sessionStorage.clear();

    if (keycloak) {
        keycloak.logout({
            redirectUri: window.location.origin + '/login'
        });
    } else {
        console.error("Keycloak instance is not available.");
        window.location.href = "/login";
    }

};