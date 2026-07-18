import { keycloak } from "@/lib/keycloak";

export const clearLogout = () => {

    if (keycloak) {
        keycloak.logout({
            redirectUri: window.location.origin + '/login'
        });
    } else {
        console.error("Keycloak instance is not available.");
        window.location.href = "/login";
    }

};