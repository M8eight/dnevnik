import { keycloak } from "@/lib/keycloak";
import { selectIsAuthenticated, selectRoles } from "@/store/slices/authSlice";
import { useEffect } from "react";
import { useSelector } from "react-redux";
import { Navigate, Outlet, useLocation } from "react-router-dom";

interface ProtectedRouteProps {
    roles?: string[];
}

export function ProtectedRoute({ roles }: ProtectedRouteProps ) {
    const isAuthenticated = useSelector(selectIsAuthenticated);
    const userRoles = useSelector(selectRoles);
    const location = useLocation();


    useEffect(() => {
        if (!isAuthenticated) {
            keycloak.login({
                redirectUri: window.location.origin + location.pathname,
            });
        }
    }, [isAuthenticated, location.pathname]);

    if (!isAuthenticated) return null;

    const hasAccess = !roles || roles.some((r) => userRoles.includes(r));
    if (!hasAccess) return <Navigate to="/forbidden" replace />;

    return <Outlet />;
}