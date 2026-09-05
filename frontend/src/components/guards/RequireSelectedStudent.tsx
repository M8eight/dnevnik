import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Loader2 } from "lucide-react";
import { useStudentsByParentId } from "@/hooks/use-student";
import { selectSelectedStudentId, setSelectedStudent } from "@/store/slices/selectedStudentSlice";
import { selectRoles } from "@/store/slices/authSlice";
import NoStudentsAttached from "../parent/no-students-attached";

export default function RequireSelectedStudent({ children }: { children: React.ReactNode }) {
    const dispatch = useDispatch();
    const roles = useSelector(selectRoles);
    const isParent = roles.includes("PARENT");
    const selectedStudentId = useSelector(selectSelectedStudentId);

    const { data: students, isLoading } = useStudentsByParentId({ enabled: isParent });

    useEffect(() => {
        if (!isParent || !students || students.length === 0) return;

        const stillValid = selectedStudentId != null && students.some((s) => s.id === selectedStudentId);
        if (!stillValid) {
            dispatch(setSelectedStudent(students[0].id));
        }
    }, [isParent, students, selectedStudentId, dispatch]);

    if (!isParent) {
        return <>{children}</>;
    }

    if (isLoading) {
        return (
            <div className="flex items-center justify-center min-h-[60vh]">
                <Loader2 className="w-6 h-6 text-(--red) animate-spin" />
            </div>
        );
    }

    if (!students || students.length === 0) {
        return <NoStudentsAttached />;
    }

    const isReady = selectedStudentId != null && students.some((s) => s.id === selectedStudentId);
    if (!isReady) {
        // короткий кадр, пока эффект выше не проставил дефолт
        return (
            <div className="flex items-center justify-center min-h-[60vh]">
                <Loader2 className="w-6 h-6 text-(--red) animate-spin" />
            </div>
        );
    }

    return <>{children}</>;
}