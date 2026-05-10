import { useEffect, useState } from "react";
import { cn } from "@/lib/utils";
import {
    Users,
    Plus,
    Loader2,
    UserRound,
    ChevronLeft,
    ChevronRight,
    Filter,
    Search,
    Trash2,
    Link2,
} from "lucide-react";
import {
    useDeleteUser,
    useFindUsersByFilter,
} from "@/hooks/use-user";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import type { UserResponse, UserRole } from "@/services/user-service";
import AdminNavbar from "@/templates/navbars/AdminNavbar";
import { ROLES } from "@/constants/component-constants";
import AssignStudentsModal from "@/components/admin/user-admin-page/assign-students-modal";
import CreateUserForm from "@/components/admin/user-admin-page/create-user-form";


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
            <AdminNavbar />

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