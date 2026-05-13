import { useEffect, useState } from "react";
import { Loader2, X, CheckCircle2, Save } from "lucide-react";
import { cn } from "@/lib/utils";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { ROLES } from "@/constants/component-constants";
import type { UserResponse, UserRole } from "@/services/user-service";
import {
    useUpdateUser,
    useStudentDetails,
    useTeacherDetails,
} from "@/hooks/use-user";

interface Props {
    user: UserResponse;
    onClose: () => void;
}

type DetailsState = {
    STUDENT: { studyProfile: string };
    TEACHER: { email: string; phoneNumber: string };
    PARENT: Record<string, never>;
};

export default function EditUserModal({ user, onClose }: Props) {
    const [selectedRoles, setSelectedRoles] = useState<UserRole[]>(user.roles);

    const [fields, setFields] = useState({
        username: user.username,
        firstName: user.firstName,
        lastName: user.lastName,
        password: "",
    });

    const [details, setDetails] = useState<DetailsState>({
        STUDENT: { studyProfile: "" },
        TEACHER: { email: "", phoneNumber: "" },
        PARENT: {},
    });

    const [success, setSuccess] = useState(false);

    // Загружаем детали только для тех ролей что есть у юзера изначально
    const { data: studentData, isLoading: isStudentLoading } = useStudentDetails(
        user.roles.includes("STUDENT") ? user.id : null
    );
    const { data: teacherData, isLoading: isTeacherLoading } = useTeacherDetails(
        user.roles.includes("TEACHER") ? user.id : null
    );

    const isDetailsLoading =
        (user.roles.includes("STUDENT") && isStudentLoading) ||
        (user.roles.includes("TEACHER") && isTeacherLoading);

    // Префилл когда загрузилось
    useEffect(() => {
        if (studentData) {
            setDetails((prev) => ({
                ...prev,
                STUDENT: { studyProfile: studentData.studyProfile ?? "" },
            }));
        }
    }, [studentData]);

    useEffect(() => {
        if (teacherData) {
            setDetails((prev) => ({
                ...prev,
                TEACHER: {
                    email: teacherData.email ?? "",
                    phoneNumber: teacherData.phoneNumber ?? "",
                },
            }));
        }
    }, [teacherData]);

    const { mutate: update, isPending, isError } = useUpdateUser(user.id);

    const handleChange =
        (field: keyof typeof fields) =>
            (e: React.ChangeEvent<HTMLInputElement>) =>
                setFields((prev) => ({ ...prev, [field]: e.target.value }));

    const toggleRole = (role: UserRole) => {
        setSelectedRoles((prev) =>
            prev.includes(role)
                ? prev.length > 1 ? prev.filter((r) => r !== role) : prev // минимум одна роль
                : [...prev, role]
        );
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        // details консистентен с roles
        const requestDetails: Record<string, unknown> = {};
        selectedRoles.forEach((role) => {
            if (role === "STUDENT") requestDetails["STUDENT"] = details.STUDENT;
            if (role === "TEACHER") requestDetails["TEACHER"] = details.TEACHER;
            if (role === "PARENT") requestDetails["PARENT"] = {};
        });

        update(
            {
                userId: user.id,
                user: {
                    username: fields.username.trim(),
                    firstName: fields.firstName.trim(),
                    lastName: fields.lastName.trim(),
                },
                ...(fields.password.trim() ? { password: fields.password.trim() } : {}),
                roles: selectedRoles,
                details: requestDetails,
            },
            {
                onSuccess: () => {
                    setSuccess(true);
                    setTimeout(() => {
                        setSuccess(false);
                        onClose();
                    }, 1500);
                },
            }
        );
    };

    const fieldClass =
        "h-11 bg-white/40 border-black/10 rounded-2xl focus-visible:ring-[var(--red)] text-sm font-semibold placeholder:font-normal";

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm p-4">
            <div className="glass-card w-full max-w-md rounded-[32px] p-6 shadow-2xl max-h-[90vh] overflow-y-auto">

                {/* Header */}
                <div className="flex items-center justify-between mb-6">
                    <div>
                        <h2 className="font-serif font-black text-xl text-[var(--navy)]">Редактировать</h2>
                        <p className="text-xs text-black/40 font-semibold mt-0.5">@{user.username}</p>
                    </div>
                    <button
                        onClick={onClose}
                        className="w-9 h-9 rounded-xl flex items-center justify-center text-black/30 hover:text-black/60 hover:bg-black/5 transition-all"
                    >
                        <X className="w-4 h-4" />
                    </button>
                </div>

                {isDetailsLoading ? (
                    <div className="flex items-center justify-center h-40 text-black/30">
                        <Loader2 className="w-6 h-6 animate-spin" />
                    </div>
                ) : (
                    <form onSubmit={handleSubmit} className="space-y-4">

                        {/* Базовые поля */}
                        <div className="grid grid-cols-2 gap-3">
                            <div className="space-y-1.5">
                                <label className="text-xs font-bold tracking-widest uppercase text-black/30">Имя</label>
                                <Input value={fields.firstName} onChange={handleChange("firstName")} disabled={isPending} className={fieldClass} />
                            </div>
                            <div className="space-y-1.5">
                                <label className="text-xs font-bold tracking-widest uppercase text-black/30">Фамилия</label>
                                <Input value={fields.lastName} onChange={handleChange("lastName")} disabled={isPending} className={fieldClass} />
                            </div>
                        </div>

                        <div className="space-y-1.5">
                            <label className="text-xs font-bold tracking-widest uppercase text-black/30">Логин</label>
                            <Input value={fields.username} onChange={handleChange("username")} disabled={isPending} className={fieldClass} />
                        </div>

                        <div className="space-y-1.5">
                            <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                                Новый пароль{" "}
                                <span className="normal-case font-normal">(оставьте пустым чтобы не менять)</span>
                            </label>
                            <Input
                                type="password"
                                placeholder="••••••••"
                                value={fields.password}
                                onChange={handleChange("password")}
                                disabled={isPending}
                                className={fieldClass}
                            />
                        </div>

                        {/* Роли */}
                        <div className="space-y-1.5">
                            <label className="text-xs font-bold tracking-widest uppercase text-black/30">Роли</label>
                            <div className="flex gap-1 bg-black/5 rounded-[18px] p-1">
                                {ROLES.map((r) => {
                                    const isActive = selectedRoles.includes(r.value);
                                    return (
                                        <button
                                            key={r.value}
                                            type="button"
                                            onClick={() => toggleRole(r.value)}
                                            className={cn(
                                                "flex-1 h-9 flex items-center justify-center gap-1.5 rounded-2xl text-[11px] font-extrabold uppercase tracking-wider transition-all",
                                                isActive
                                                    ? cn("bg-white/60 shadow-sm", r.color)
                                                    : "text-black/30 hover:text-black/50"
                                            )}
                                        >
                                            {r.icon}
                                            <span className="hidden sm:inline">{r.label}</span>
                                        </button>
                                    );
                                })}
                            </div>
                        </div>

                        {/* Details — только для выбранных ролей */}
                        {selectedRoles.includes("STUDENT") && (
                            <div className="space-y-1.5">
                                <label className="text-xs font-bold tracking-widest uppercase text-black/30">Профиль обучения</label>
                                <Input
                                    value={details.STUDENT.studyProfile}
                                    onChange={(e) =>
                                        setDetails((prev) => ({
                                            ...prev,
                                            STUDENT: { studyProfile: e.target.value },
                                        }))
                                    }
                                    disabled={isPending}
                                    className={fieldClass}
                                    placeholder="СОЦ ЭКОНОМ"
                                />
                            </div>
                        )}

                        {selectedRoles.includes("TEACHER") && (
                            <>
                                <div className="space-y-1.5">
                                    <label className="text-xs font-bold tracking-widest uppercase text-black/30">Email учителя</label>
                                    <Input
                                        type="email"
                                        value={details.TEACHER.email}
                                        onChange={(e) =>
                                            setDetails((prev) => ({
                                                ...prev,
                                                TEACHER: { ...prev.TEACHER, email: e.target.value },
                                            }))
                                        }
                                        disabled={isPending}
                                        className={fieldClass}
                                    />
                                </div>
                                <div className="space-y-1.5">
                                    <label className="text-xs font-bold tracking-widest uppercase text-black/30">Телефон учителя</label>
                                    <Input
                                        value={details.TEACHER.phoneNumber}
                                        onChange={(e) =>
                                            setDetails((prev) => ({
                                                ...prev,
                                                TEACHER: { ...prev.TEACHER, phoneNumber: e.target.value },
                                            }))
                                        }
                                        disabled={isPending}
                                        className={fieldClass}
                                    />
                                </div>
                            </>
                        )}

                        <Button
                            type="submit"
                            disabled={isPending}
                            className="w-full gap-2 text-white bg-[var(--navy)] hover:bg-[var(--navy)]/90 rounded-2xl py-6 text-sm font-bold shadow-lg transition-all active:scale-[0.98] disabled:opacity-40 mt-2"
                        >
                            {isPending ? (
                                <><Loader2 className="w-4 h-4 animate-spin" />Сохранение...</>
                            ) : success ? (
                                <><CheckCircle2 className="w-4 h-4" />Сохранено!</>
                            ) : (
                                <><Save className="w-4 h-4" />Сохранить изменения</>
                            )}
                        </Button>

                        {isError && (
                            <p className="text-xs text-[var(--red)] font-semibold text-center">
                                Ошибка при сохранении. Попробуйте ещё раз.
                            </p>
                        )}
                    </form>
                )}
            </div>
        </div>
    );
}