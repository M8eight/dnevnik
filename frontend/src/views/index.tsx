import { keycloak } from "@/lib/keycloak";
import { selectRoles } from "@/store/slices/authSlice";
import { useEffect } from "react";
import { useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";

export default function Index() {

    const roles = useSelector(selectRoles);
    const navigate = useNavigate();

    useEffect(() => {
        if (!keycloak.authenticated) {
            keycloak.login();
        } else {
            if (roles.includes("ADMIN")) {
                navigate("/admin/subject", { replace: true });
            } else if (roles.includes("STUDENT")) {
                navigate("/student/home", { replace: true });
            } else if (roles.includes("TEACHER")) {
                navigate("/teacher/journal", { replace: true });
            } else if (roles.includes("PARENT")) {
                navigate("/parent/home", { replace: true });
            } else {
                navigate("/", { replace: true });
            }
        }
    }, [navigate, roles]);

    return (
        <>
            <div className="min-h-screen flex items-center justify-center">
                <div className="text-center">
                    <p className="text-lg">Перенаправление...</p>
                </div>
            </div>
        </>

    );
}