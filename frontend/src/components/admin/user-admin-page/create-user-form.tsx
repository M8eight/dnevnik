import { ROLES } from "@/constants/component-constants";
import { useCreateStudent, useCreateParent, useCreateTeacher } from "@/hooks/use-user";
import { cn } from "@/lib/utils";
import type { UserRole } from "@/services/user-service";
import { Loader2, CheckCircle2, Send } from "lucide-react";
import { Input } from "@/components/ui/input";
import RoleTab from "./role-tab";
import { useState } from "react";
import { Button } from "@/components/ui/button";


export default function CreateUserForm() {
    const [role, setRole] = useState<UserRole>("STUDENT");
    const [success, setSuccess] = useState(false);

    const [base, setBase] = useState({
        username: "",
        password: "",
        firstName: "",
        lastName: "",
    });

    const [studentDetails, setStudentDetails] = useState("");
    const [teacherEmail, setTeacherEmail] = useState("");
    const [teacherPhone, setTeacherPhone] = useState("");

    const studentMutation = useCreateStudent();
    const parentMutation = useCreateParent();
    const teacherMutation = useCreateTeacher();

    const isPending =
        studentMutation.isPending ||
        parentMutation.isPending ||
        teacherMutation.isPending;

    const isError =
        studentMutation.isError ||
        parentMutation.isError ||
        teacherMutation.isError;

    const isBaseValid =
        base.username.trim() &&
        base.password.trim() &&
        base.firstName.trim() &&
        base.lastName.trim();

    const isRoleValid =
        role === "STUDENT"
            ? !!studentDetails.trim()
            : role === "TEACHER"
            ? !!teacherEmail.trim() && !!teacherPhone.trim()
            : true;

    const isValid = isBaseValid && isRoleValid;

    const handleBaseChange =
        (field: keyof typeof base) =>
        (e: React.ChangeEvent<HTMLInputElement>) => {
            setBase((prev) => ({ ...prev, [field]: e.target.value }));
        };

    const handleRoleChange = (newRole: UserRole) => {
        setRole(newRole);
        studentMutation.reset();
        parentMutation.reset();
        teacherMutation.reset();
    };

    const handleSuccess = () => {
        setBase({ username: "", password: "", firstName: "", lastName: "" });
        setStudentDetails("");
        setTeacherEmail("");
        setTeacherPhone("");
        setSuccess(true);
        setTimeout(() => setSuccess(false), 2500);
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!isValid || isPending) return;

        const userBase = {
            username: base.username.trim(),
            password: base.password.trim(),
            firstName: base.firstName.trim(),
            lastName: base.lastName.trim(),
        };

        if (role === "STUDENT") {
            studentMutation.mutate(
                { user: userBase, role: "STUDENT", details: { studentDetails: studentDetails.trim() } },
                { onSuccess: handleSuccess }
            );
        } else if (role === "PARENT") {
            parentMutation.mutate(
                { user: userBase, role: "PARENT", details: {} },
                { onSuccess: handleSuccess }
            );
        } else {
            teacherMutation.mutate(
                { user: userBase, role: "TEACHER", details: { email: teacherEmail.trim(), phoneNumber: teacherPhone.trim() } },
                { onSuccess: handleSuccess }
            );
        }
    };

    const fieldClass =
        "h-11 bg-white/40 border-black/10 rounded-2xl focus-visible:ring-[var(--red)] text-sm font-semibold placeholder:font-normal";

    const activeRole = ROLES.find((r) => r.value === role)!;

    return (
        <form onSubmit={handleSubmit} className="space-y-5">
            <div className="space-y-1.5">
                <label className="text-xs font-bold tracking-widest uppercase text-black/30">Роль</label>
                <div className="flex gap-1 bg-black/5 rounded-[18px] p-1">
                    {ROLES.map((r) => (
                        <RoleTab key={r.value} role={r} active={role === r.value} onClick={() => handleRoleChange(r.value)} />
                    ))}
                </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                    <label className="text-xs font-bold tracking-widest uppercase text-black/30">Имя</label>
                    <Input placeholder="Иван" value={base.firstName} onChange={handleBaseChange("firstName")} disabled={isPending} className={fieldClass} />
                </div>
                <div className="space-y-1.5">
                    <label className="text-xs font-bold tracking-widest uppercase text-black/30">Фамилия</label>
                    <Input placeholder="Иванов" value={base.lastName} onChange={handleBaseChange("lastName")} disabled={isPending} className={fieldClass} />
                </div>
            </div>

            <div className="space-y-1.5">
                <label className="text-xs font-bold tracking-widest uppercase text-black/30">Логин</label>
                <Input placeholder="ivanov_ivan" value={base.username} onChange={handleBaseChange("username")} disabled={isPending} className={fieldClass} />
            </div>

            <div className="space-y-1.5">
                <label className="text-xs font-bold tracking-widest uppercase text-black/30">Пароль</label>
                <Input placeholder="••••••••" value={base.password} onChange={handleBaseChange("password")} disabled={isPending} className={fieldClass} />
            </div>

            {role === "STUDENT" && (
                <div className="space-y-1.5">
                    <label className="text-xs font-bold tracking-widest uppercase text-black/30">Детали ученика</label>
                    <Input placeholder="Профиль ученика" value={studentDetails} onChange={(e) => setStudentDetails(e.target.value)} disabled={isPending} className={fieldClass} />
                </div>
            )}

            {role === "TEACHER" && (
                <>
                    <div className="space-y-1.5">
                        <label className="text-xs font-bold tracking-widest uppercase text-black/30">Email</label>
                        <Input type="email" placeholder="teacher@school.ru" value={teacherEmail} onChange={(e) => setTeacherEmail(e.target.value)} disabled={isPending} className={fieldClass} />
                    </div>
                    <div className="space-y-1.5">
                        <label className="text-xs font-bold tracking-widest uppercase text-black/30">Телефон</label>
                        <Input placeholder="+79001234567" value={teacherPhone} onChange={(e) => setTeacherPhone(e.target.value)} disabled={isPending} className={fieldClass} />
                    </div>
                </>
            )}

            <Button
                type="submit"
                disabled={!isValid || isPending}
                className={cn(
                    "w-full gap-2 text-white rounded-2xl py-6 text-sm font-bold shadow-lg transition-all active:scale-[0.98] disabled:opacity-40",
                    role === "STUDENT"
                        ? "bg-blue-600 hover:bg-blue-700 shadow-blue-200"
                        : role === "PARENT"
                        ? "bg-violet-600 hover:bg-violet-700 shadow-violet-200"
                        : "bg-emerald-600 hover:bg-emerald-700 shadow-emerald-200"
                )}
            >
                {isPending ? (
                    <><Loader2 className="w-4 h-4 animate-spin" />Создание...</>
                ) : success ? (
                    <><CheckCircle2 className="w-4 h-4" />Создан!</>
                ) : (
                    <>Создать {activeRole.label.toLowerCase()}а<Send className="w-4 h-4" /></>
                )}
            </Button>

            {isError && (
                <p className="text-xs text-[var(--red)] font-semibold text-center">
                    Ошибка при создании. Попробуйте ещё раз.
                </p>
            )}
        </form>
    );
}