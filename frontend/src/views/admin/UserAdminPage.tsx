import React, { useEffect, useState, useRef } from "react";
import { cn } from "@/lib/utils";
import { NavLink } from "react-router-dom";
import {
    Users,
    Plus,
    Send,
    Layers,
    CheckCircle2,
    Loader2,
    GraduationCap,
    BookUser,
    UserRound,
    ChevronLeft,
    ChevronRight,
    Filter,
    Search,
    Trash2,
    Link2,
    X,
    UserMinus,
    UserPlus,
} from "lucide-react";
import {
    useCreateStudent,
    useCreateParent,
    useCreateTeacher,
    useDeleteUser,
    useFindUsersByFilter,
} from "@/hooks/use-user";
import {
    useAssignStudentToParent,
    useUnassignStudentFromParent,
} from "@/hooks/use-student";
import { useParent } from "@/hooks/use-parent";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import type { UserResponse, UserRole } from "@/services/user-service";

// ─── NavItem ──────────────────────────────────────────────────────────────────

function NavItem({ to, label }: { to: string; label: string }) {
    return (
        <NavLink
            to={to}
            className={({ isActive }) =>
                cn(
                    "glass-pill px-5 h-10 flex items-center rounded-2xl text-[12px] font-extrabold uppercase tracking-wider transition-all",
                    isActive
                        ? "text-[var(--navy)] bg-white/40 shadow-sm"
                        : "text-black/30 hover:text-[var(--navy)] hover:bg-white/20"
                )
            }
        >
            {label}
        </NavLink>
    );
}

// ─── Role config ──────────────────────────────────────────────────────────────

const ROLES: {
    value: UserRole;
    label: string;
    icon: React.ReactNode;
    color: string;
    iconBg: string;
}[] = [
    {
        value: "STUDENT",
        label: "Ученик",
        icon: <GraduationCap className="w-4 h-4" />,
        color: "text-blue-600",
        iconBg: "bg-blue-50/70",
    },
    {
        value: "PARENT",
        label: "Родитель",
        icon: <UserRound className="w-4 h-4" />,
        color: "text-violet-600",
        iconBg: "bg-violet-50/70",
    },
    {
        value: "TEACHER",
        label: "Учитель",
        icon: <BookUser className="w-4 h-4" />,
        color: "text-emerald-600",
        iconBg: "bg-emerald-50/70",
    },
];

// ─── RoleTab ──────────────────────────────────────────────────────────────────

function RoleTab({
    role,
    active,
    onClick,
}: {
    role: (typeof ROLES)[number];
    active: boolean;
    onClick: () => void;
}) {
    return (
        <button
            type="button"
            onClick={onClick}
            className={cn(
                "flex-1 flex items-center justify-center gap-2 h-11 rounded-2xl text-[12px] font-extrabold uppercase tracking-wider transition-all",
                active
                    ? cn("bg-white/60 shadow-sm", role.color)
                    : "text-black/30 hover:text-black/50 hover:bg-white/30"
            )}
        >
            <span className={active ? role.color : ""}>{role.icon}</span>
            {role.label}
        </button>
    );
}

// ─── AssignStudentsModal ──────────────────────────────────────────────────────

function AssignStudentsModal({
    parent,
    onClose,
}: {
    parent: UserResponse;
    onClose: () => void;
}) {
    const [studentSearch, setStudentSearch] = useState("");
    const [debouncedStudentSearch, setDebouncedStudentSearch] = useState("");
    const overlayRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const t = setTimeout(() => setDebouncedStudentSearch(studentSearch), 400);
        return () => clearTimeout(t);
    }, [studentSearch]);

    // Данные родителя с детьми
    const { data: parentData, isLoading: isParentLoading } = useParent(parent.id);

    // Список всех учеников для поиска
    const { data: allStudents, isLoading: isStudentsLoading } = useFindUsersByFilter(
        0,
        20,
        "STUDENT",
        debouncedStudentSearch || undefined
    );

    const assignMutation = useAssignStudentToParent();
    const unassignMutation = useUnassignStudentFromParent();

    const childrenIds = new Set(parentData?.children.map((c) => c.id) ?? []);

    const handleOverlayClick = (e: React.MouseEvent) => {
        if (e.target === overlayRef.current) onClose();
    };

    // Блокируем скролл страницы пока открыта модалка
    useEffect(() => {
        document.body.style.overflow = "hidden";
        return () => { document.body.style.overflow = ""; };
    }, []);

    const isPending = assignMutation.isPending || unassignMutation.isPending;

    return (
        <div
            ref={overlayRef}
            onClick={handleOverlayClick}
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm px-4"
        >
            <div className="w-full max-w-lg bg-white/80 backdrop-blur-xl rounded-[32px] shadow-2xl border border-white/60 flex flex-col max-h-[85vh] overflow-hidden">

                {/* Header */}
                <div className="px-6 pt-6 pb-4 border-b border-black/5 flex items-start justify-between gap-4">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-[14px] bg-violet-50/70 flex items-center justify-center text-violet-600 shrink-0">
                            <UserRound className="w-4 h-4" />
                        </div>
                        <div>
                            <p className="font-black text-[var(--navy)] text-base leading-tight">
                                {parent.firstName} {parent.lastName}
                            </p>
                            <p className="text-xs font-semibold text-black/40">@{parent.username}</p>
                        </div>
                    </div>
                    <button
                        onClick={onClose}
                        className="w-8 h-8 rounded-xl flex items-center justify-center text-black/30 hover:text-black/60 hover:bg-black/5 transition-all shrink-0 mt-1"
                    >
                        <X className="w-4 h-4" />
                    </button>
                </div>

                <div className="flex-1 overflow-y-auto p-6 space-y-5">

                    {/* Текущие дети */}
                    <div>
                        <p className="text-xs font-bold tracking-widest uppercase text-black/30 mb-3">
                            Привязанные ученики
                            {parentData && (
                                <span className="ml-2 normal-case font-bold text-black/20">
                                    ({parentData.children.length})
                                </span>
                            )}
                        </p>

                        {isParentLoading ? (
                            <div className="flex justify-center py-4">
                                <Loader2 className="w-5 h-5 animate-spin text-black/30" />
                            </div>
                        ) : parentData?.children.length === 0 ? (
                            <div className="flex items-center gap-2 py-3 px-4 rounded-2xl bg-black/[0.03] text-black/30">
                                <GraduationCap className="w-4 h-4 shrink-0" />
                                <p className="text-xs font-semibold">Нет привязанных учеников</p>
                            </div>
                        ) : (
                            <div className="space-y-2">
                                {parentData?.children.map((child) => (
                                    <div
                                        key={child.id}
                                        className="flex items-center justify-between px-4 py-2.5 rounded-2xl bg-blue-50/50 border border-blue-100/60"
                                    >
                                        <div className="flex items-center gap-3">
                                            <div className="w-7 h-7 rounded-[10px] bg-blue-100/70 flex items-center justify-center text-blue-600">
                                                <GraduationCap className="w-3.5 h-3.5" />
                                            </div>
                                            <div>
                                                <p className="text-sm font-bold text-[var(--navy)] leading-none">
                                                    {child.firstName} {child.lastName}
                                                </p>
                                                <p className="text-[11px] font-semibold text-black/30 mt-0.5">
                                                    @{child.username}
                                                </p>
                                            </div>
                                        </div>
                                        <button
                                            onClick={() =>
                                                unassignMutation.mutate({
                                                    studentId: child.id,
                                                    parentId: parent.id,
                                                })
                                            }
                                            disabled={isPending}
                                            className="w-7 h-7 rounded-xl flex items-center justify-center text-black/20 hover:text-red-500 hover:bg-red-50 transition-all disabled:opacity-40"
                                            title="Отвязать"
                                        >
                                            <UserMinus className="w-3.5 h-3.5" />
                                        </button>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>

                    {/* Разделитель */}
                    <div className="flex items-center gap-3">
                        <div className="flex-1 h-px bg-black/8" />
                        <span className="text-[10px] font-bold tracking-widest uppercase text-black/20">
                            Добавить ученика
                        </span>
                        <div className="flex-1 h-px bg-black/8" />
                    </div>

                    {/* Поиск учеников */}
                    <div className="space-y-3">
                        <div className="relative">
                            <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-black/30" />
                            <Input
                                placeholder="Поиск по имени..."
                                value={studentSearch}
                                onChange={(e) => setStudentSearch(e.target.value)}
                                className="h-10 pl-10 bg-white/50 border-black/10 rounded-2xl text-sm font-semibold placeholder:font-normal focus-visible:ring-[var(--red)]"
                            />
                        </div>

                        {isStudentsLoading ? (
                            <div className="flex justify-center py-4">
                                <Loader2 className="w-5 h-5 animate-spin text-black/30" />
                            </div>
                        ) : allStudents?.content.length === 0 ? (
                            <div className="flex items-center gap-2 py-3 px-4 rounded-2xl bg-black/[0.03] text-black/30">
                                <Filter className="w-4 h-4 shrink-0" />
                                <p className="text-xs font-semibold">Ученики не найдены</p>
                            </div>
                        ) : (
                            <div className="space-y-1.5">
                                {allStudents?.content.map((student) => {
                                    const isAlreadyLinked = childrenIds.has(student.id);
                                    return (
                                        <div
                                            key={student.id}
                                            className={cn(
                                                "flex items-center justify-between px-4 py-2.5 rounded-2xl transition-colors",
                                                isAlreadyLinked
                                                    ? "bg-blue-50/40 opacity-60"
                                                    : "hover:bg-white/50"
                                            )}
                                        >
                                            <div className="flex items-center gap-3">
                                                <div className="w-7 h-7 rounded-[10px] bg-blue-50/70 flex items-center justify-center text-blue-500">
                                                    <GraduationCap className="w-3.5 h-3.5" />
                                                </div>
                                                <div>
                                                    <p className="text-sm font-bold text-[var(--navy)] leading-none">
                                                        {student.firstName} {student.lastName}
                                                    </p>
                                                    <p className="text-[11px] font-semibold text-black/30 mt-0.5">
                                                        @{student.username}
                                                    </p>
                                                </div>
                                            </div>

                                            {isAlreadyLinked ? (
                                                <span className="text-[10px] font-extrabold uppercase tracking-widest text-blue-400 px-2 py-1 rounded-full bg-blue-50">
                                                    Привязан
                                                </span>
                                            ) : (
                                                <button
                                                    onClick={() =>
                                                        assignMutation.mutate({
                                                            studentId: student.id,
                                                            parentId: parent.id,
                                                        })
                                                    }
                                                    disabled={isPending}
                                                    className="w-7 h-7 rounded-xl flex items-center justify-center text-black/20 hover:text-violet-600 hover:bg-violet-50 transition-all disabled:opacity-40"
                                                    title="Привязать"
                                                >
                                                    <UserPlus className="w-3.5 h-3.5" />
                                                </button>
                                            )}
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </div>
                </div>

                {/* Footer */}
                <div className="px-6 py-4 border-t border-black/5">
                    <Button
                        onClick={onClose}
                        className="w-full rounded-2xl bg-[var(--navy)] hover:bg-[var(--navy)]/90 text-white font-bold text-sm py-5"
                    >
                        Готово
                    </Button>
                </div>
            </div>
        </div>
    );
}

// ─── CreateUserForm ───────────────────────────────────────────────────────────

function CreateUserForm() {
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

// ─── Main page ────────────────────────────────────────────────────────────────

export default function UserAdminPage() {
    const [page, setPage] = useState(0);
    const [size] = useState(10);
    const [searchName, setSearchName] = useState("");
    const [debouncedSearch, setDebouncedSearch] = useState("");
    const [filterRole, setFilterRole] = useState<UserRole | "ALL">("ALL");

    // Модалка привязки
    const [assignParent, setAssignParent] = useState<UserResponse | null>(null);

    useEffect(() => {
        const timer = setTimeout(() => setDebouncedSearch(searchName), 500);
        return () => clearTimeout(timer);
    }, [searchName]);

    useEffect(() => { setPage(0); }, [debouncedSearch, filterRole]);

    const { data: usersData, isLoading: isUsersLoading } = useFindUsersByFilter(
        page,
        size,
        filterRole === "ALL" ? undefined : filterRole,
        debouncedSearch || undefined
    );

    const deleteMutation = useDeleteUser();

    const handleDelete = (id: number, name: string) => {
        if (window.confirm(`Вы уверены, что хотите удалить пользователя ${name}?`)) {
            deleteMutation.mutate(id);
        }
    };

    const fieldClass = "h-11 bg-white/40 border-black/10 rounded-2xl focus-visible:ring-[var(--red)] text-sm font-semibold placeholder:font-normal";

    const renderPagination = () => {
        if (!usersData || usersData.totalPages <= 1) return null;
        return (
            <div className="flex items-center gap-3">
                <p className="text-xs font-semibold text-black/40 hidden sm:block">
                    Страница {usersData.number + 1} из {usersData.totalPages}
                </p>
                <div className="flex gap-1.5">
                    <Button variant="outline" size="icon" onClick={() => setPage(p => Math.max(0, p - 1))} disabled={usersData.first || isUsersLoading} className="h-8 w-8 rounded-xl bg-white/40 border-black/10 hover:bg-white/60 transition-all">
                        <ChevronLeft className="w-4 h-4" />
                    </Button>
                    <Button variant="outline" size="icon" onClick={() => setPage(p => p + 1)} disabled={usersData.last || isUsersLoading} className="h-8 w-8 rounded-xl bg-white/40 border-black/10 hover:bg-white/60 transition-all">
                        <ChevronRight className="w-4 h-4" />
                    </Button>
                </div>
            </div>
        );
    };

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">

            {/* Модалка привязки */}
            {assignParent && (
                <AssignStudentsModal
                    parent={assignParent}
                    onClose={() => setAssignParent(null)}
                />
            )}

            {/* ── Header ── */}
            <header className="mb-6">
                <div className="max-w-[1400px] mx-auto">
                    <div className="glass-card rounded-[24px] px-6 h-16 flex items-center justify-between border-none shadow-lg backdrop-blur-md">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 rounded-[14px] bg-[var(--red-light)]/60 flex items-center justify-center ring-1 ring-[var(--red)]/10">
                                <Layers className="w-5 h-5 text-[var(--red)]" />
                            </div>
                            <span className="font-serif font-black text-[1.2rem] text-[var(--navy)] tracking-tight">
                                Панель администратора
                            </span>
                        </div>
                        <nav className="hidden lg:flex items-center gap-2">
                            <NavItem to="/admin/subject" label="Предмет" />
                            <NavItem to="/admin/period" label="Четверть" />
                            <NavItem to="/admin/class" label="Класс" />
                            <NavItem to="/admin/user" label="Пользователь" />
                            <NavItem to="/admin/schedule" label="Расписание" />
                        </nav>
                        <div className="flex items-center gap-4">
                            <div className="text-right hidden sm:block">
                                <p className="text-[13px] font-black text-[var(--navy)] leading-none mb-1">Администратор</p>
                                <p className="text-[9px] font-extrabold tracking-[0.2em] uppercase text-black/25">Admin</p>
                            </div>
                            <div className="w-11 h-11 rounded-[15px] bg-[var(--navy-light)]/40 ring-1 ring-black/[0.05] flex items-center justify-center shadow-inner">
                                <span className="font-serif font-black text-[15px] text-[var(--navy)]">А</span>
                            </div>
                        </div>
                    </div>
                </div>
            </header>

            {/* ── Controls bar ── */}
            <div className="max-w-[1400px] mx-auto mb-6">
                <div className="glass-card rounded-[24px] p-5 flex items-center gap-5 border-none shadow-lg backdrop-blur-md">
                    <div className="hidden sm:flex w-12 h-12 rounded-[18px] bg-[var(--red-light)]/60 items-center justify-center ring-1 ring-[var(--red)]/10">
                        <Users className="w-6 h-6 text-[var(--red)]" />
                    </div>
                    <div>
                        <h1 className="font-serif font-black text-2xl lg:text-3xl text-[var(--navy)] tracking-tight">Пользователи</h1>
                        <p className="text-sm text-black/40 mt-0.5">Создание учеников, родителей и учителей</p>
                    </div>
                </div>
            </div>

            {/* ── Main Content Grid ── */}
            <div className="max-w-[1400px] mx-auto grid grid-cols-1 xl:grid-cols-[400px_1fr] gap-6 xl:gap-8 mt-6">

                {/* Левая колонка */}
                <div className="flex flex-col gap-6">
                    <div className="glass-card rounded-[32px] p-6 backdrop-blur-md">
                        <h2 className="text-base font-black text-[var(--navy)] flex items-center gap-2 mb-5">
                            <Plus className="w-4 h-4 text-[var(--red)]" />
                            Создать пользователя
                        </h2>
                        <CreateUserForm />
                    </div>
                </div>

                {/* Правая колонка */}
                <div className="flex flex-col gap-4">

                    {/* Фильтры */}
                    <div className="glass-card rounded-[24px] p-4 flex flex-col sm:flex-row gap-4 items-center justify-between backdrop-blur-md">
                        <div className="relative w-full sm:max-w-md">
                            <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-black/30" />
                            <Input
                                placeholder="Поиск по имени или фамилии..."
                                value={searchName}
                                onChange={(e) => setSearchName(e.target.value)}
                                className={cn(fieldClass, "pl-10")}
                            />
                        </div>
                        <div className="flex gap-1 bg-black/5 rounded-[18px] p-1 w-full sm:w-auto overflow-x-auto">
                            <button
                                onClick={() => setFilterRole("ALL")}
                                className={cn(
                                    "px-4 h-9 rounded-2xl text-[12px] font-extrabold uppercase tracking-wider transition-all whitespace-nowrap",
                                    filterRole === "ALL" ? "bg-white/60 shadow-sm text-[var(--navy)]" : "text-black/30 hover:text-black/50"
                                )}
                            >
                                Все
                            </button>
                            {ROLES.map((r) => (
                                <button
                                    key={r.value}
                                    onClick={() => setFilterRole(r.value)}
                                    className={cn(
                                        "px-4 h-9 flex items-center gap-2 rounded-2xl text-[12px] font-extrabold uppercase tracking-wider transition-all whitespace-nowrap",
                                        filterRole === r.value ? cn("bg-white/60 shadow-sm", r.color) : "text-black/30 hover:text-black/50"
                                    )}
                                >
                                    {r.icon}
                                    <span className="hidden sm:inline">{r.label}</span>
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Список */}
                    <div className="glass-card rounded-[32px] p-2 flex-1 flex flex-col backdrop-blur-md min-h-[500px]">

                        <div className="p-4 px-6 flex items-center justify-between border-b border-black/5 flex-wrap gap-4">
                            <div className="flex items-center gap-3">
                                <h2 className="text-base font-black text-[var(--navy)] flex items-center gap-2">
                                    <Users className="w-4 h-4 text-[var(--red)]" />
                                    Список пользователей
                                </h2>
                                {usersData && (
                                    <span className="text-xs font-bold text-black/40 bg-black/5 px-3 py-1 rounded-full">
                                        Всего: {usersData.totalElements}
                                    </span>
                                )}
                            </div>
                            {renderPagination()}
                        </div>

                        <div className="flex-1 p-2 overflow-y-auto">
                            {isUsersLoading ? (
                                <div className="h-full flex items-center justify-center text-black/30">
                                    <Loader2 className="w-8 h-8 animate-spin" />
                                </div>
                            ) : usersData?.content.length === 0 ? (
                                <div className="h-full flex flex-col items-center justify-center text-black/30 gap-2">
                                    <Filter className="w-8 h-8" />
                                    <p className="text-sm font-semibold">Пользователи не найдены</p>
                                </div>
                            ) : (
                                <div className="space-y-2">
                                    {usersData?.content.map((user: UserResponse) => {
                                        const primaryRoleConfig = ROLES.find(r => r.value === user.roles[0]);
                                        const isParent = user.roles.includes("PARENT");

                                        return (
                                            <div key={user.id} className="group flex flex-col sm:flex-row sm:items-center justify-between p-3 px-4 rounded-[20px] hover:bg-white/40 transition-colors gap-3">

                                                <div className="flex items-center gap-4">
                                                    <div className={cn("w-10 h-10 rounded-[14px] flex shrink-0 items-center justify-center", primaryRoleConfig?.iconBg, primaryRoleConfig?.color)}>
                                                        {primaryRoleConfig?.icon || <UserRound className="w-4 h-4" />}
                                                    </div>
                                                    <div>
                                                        <p className="font-bold text-sm text-[var(--navy)]">
                                                            {user.firstName} {user.lastName}
                                                        </p>
                                                        <p className="text-xs font-semibold text-black/40">@{user.username}</p>
                                                    </div>
                                                </div>

                                                <div className="flex items-center justify-between sm:justify-end gap-3 w-full sm:w-auto">
                                                    <div className="flex flex-wrap gap-1">
                                                        {user.roles.map((roleValue) => {
                                                            const roleConfig = ROLES.find(r => r.value === roleValue);
                                                            if (!roleConfig) return null;
                                                            return (
                                                                <span
                                                                    key={roleValue}
                                                                    className={cn("text-[10px] font-extrabold uppercase tracking-widest px-3 py-1 rounded-full bg-white/50", roleConfig.color)}
                                                                >
                                                                    {roleConfig.label}
                                                                </span>
                                                            );
                                                        })}
                                                    </div>

                                                    <div className="flex items-center gap-1">
                                                        {/* Кнопка привязки — только для родителей */}
                                                        {isParent && (
                                                            <button
                                                                onClick={() => setAssignParent(user)}
                                                                className="w-8 h-8 shrink-0 rounded-xl flex items-center justify-center text-black/20 hover:text-violet-600 hover:bg-violet-50 transition-all"
                                                                title="Управление учениками"
                                                            >
                                                                <Link2 className="w-4 h-4" />
                                                            </button>
                                                        )}

                                                        <button
                                                            onClick={() => handleDelete(user.id, `${user.firstName} ${user.lastName}`)}
                                                            disabled={deleteMutation.isPending}
                                                            className="w-8 h-8 shrink-0 rounded-xl flex items-center justify-center text-black/20 hover:text-red-500 hover:bg-red-50 transition-all disabled:opacity-50"
                                                        >
                                                            <Trash2 className="w-4 h-4" />
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </div>

                        <div className="p-4 px-6 border-t border-black/5 flex items-center justify-between">
                            <span className="text-xs font-bold text-black/30">Конец списка</span>
                            {renderPagination()}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}