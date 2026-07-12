import { keycloak } from "@/lib/keycloak";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";


export default function LoginPage() {
    const navigate = useNavigate();

    useEffect(() => {
        if (!keycloak.authenticated) {
            keycloak.login();
            return;
        }

        navigate("/", { replace: true });

    }, [navigate]);

    return (
        <div className="min-h-screen flex items-center justify-center">
            <div className="text-center">
                <p className="text-lg">Перенаправление...</p>
            </div>
        </div>
    );
}