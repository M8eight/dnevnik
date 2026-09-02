import { useEffect, useState, useRef, useCallback } from "react";
import { cn } from "@/lib/utils";
import {
    Users,
    Plus,
    Loader2,
    UserRound,
    Filter,
    Search,
    Trash2,
    Link2,
    Pencil,
} from "lucide-react";
import {
    useDeleteUser,
    useFindUsersByFilter,
} from "@/hooks/use-user";
import { Input } from "@/components/ui/input";
import type { UserResponse, UserRole } from "@/services/user-service";
import AdminNavbar from "@/components/layout/navbars/AdminNavbar";
import { ROLES } from "@/constants/component-constants";
import AssignStudentsModal from "@/components/admin/user-admin-page/assign-students-modal";
import CreateUserForm from "@/components/admin/user-admin-page/create-user-form";
import EditUserModal from "@/components/admin/user-admin-page/edit-user-modal";
import AssignParentModal from "@/components/admin/user-admin-page/assign-parent-modal";

export default function UserAdminPage() {
    const size = 20;
    const [searchName, setSearchName] = useState("");
    const [debouncedSearch, setDebouncedSearch] = useState("");
    const [filterRole, setFilterRole] = useState<UserRole | "ALL">("ALL");

    const [assignParent, setAssignParent] = useState<UserResponse | null>(null);
    const [assignStudent, setAssignStudent] = useState<UserResponse | null>(null);
    const [editUser, setEditUser] = useState<UserResponse | null>(null);

    useEffect(() => {
        const timer = setTimeout(() => setDebouncedSearch(searchName), 500);
        return () => clearTimeout(timer);
    }, [searchName]);

    const {
        data,
        isLoading: isUsersLoading,
        isFetchingNextPage,
        hasNextPage,
        fetchNextPage,
    } = useFindUsersByFilter(
        size,
        filterRole === "ALL" ? undefined : filterRole,
        debouncedSearch || undefined
    );

    const users = data?.pages.flatMap((page) => page.content) ?? [];

    const totalElements = data?.pages[0]?.totalElements ?? 0;

    const deleteMutation = useDeleteUser();

    const handleDelete = (id: number, name: string) => {
        if (window.confirm(`Вы уверены, что хотите удалить пользователя ${name}?`)) {
            deleteMutation.mutate(id);
        }
    };

    const observerRef = useRef<IntersectionObserver | null>(null);
    const loadMoreRef = useCallback(
        (node: HTMLDivElement | null) => {
            if (isFetchingNextPage) return;

            if (observerRef.current) {
                observerRef.current.disconnect();
            }

            observerRef.current = new IntersectionObserver((entries) => {
                if (entries[0].isIntersecting && hasNextPage && !isFetchingNextPage) {
                    fetchNextPage();
                }
            });

            if (node) {
                observerRef.current.observe(node);
            }
        },
        [hasNextPage, isFetchingNextPage, fetchNextPage]
    );

    const fieldClass =
        "h-11 bg-white/40 border-black/10 rounded-2xl focus-visible:ring-[var(--red)] text-sm font-semibold placeholder:font-normal";

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
            {assignParent && (
                <AssignStudentsModal
                    parent={assignParent}
                    onClose={() => setAssignParent(null)}
                />
            )}

            {assignStudent && (
                <AssignParentModal
                    student={assignStudent}
                    onClose={() => setAssignStudent(null)}
                />
            )}

            {editUser && (
                <EditUserModal user={editUser} onClose={() => setEditUser(null)} />
            )}

            <AdminNavbar />

            <div className="max-w-[1400px] mx-auto mb-6">
                <div className="glass-card rounded-[24px] p-5 flex items-center gap-5 border-none shadow-lg backdrop-blur-md">
                    <div className="hidden sm:flex w-12 h-12 rounded-[18px] bg-[var(--red-light)]/60 items-center justify-center ring-1 ring-[var(--red)]/10">
                        <Users className="w-6 h-6 text-[var(--red)]" />
                    </div>
                    <div>
                        <h1 className="font-serif font-black text-2xl lg:text-3xl text-[var(--navy)] tracking-tight">
                            Пользователи
                        </h1>
                        <p className="text-sm text-black/40 mt-0.5">
                            Создание учеников, родителей и учителей
                        </p>
                    </div>
                </div>
            </div>

            <div className="max-w-[1800px] mx-auto grid grid-cols-1 xl:grid-cols-[500px_1fr] gap-6 xl:gap-8 mt-6">
                <div className="flex flex-col gap-6">
                    <div className="glass-card rounded-[32px] p-6 backdrop-blur-md">
                        <h2 className="text-base font-black text-[var(--navy)] flex items-center gap- mb-5">
                            <Plus className="w-4 h-4 text-[var(--red)]" />
                            Создать пользователя
                        </h2>
                        <CreateUserForm />
                    </div>
                </div>

                <div className="flex flex-col gap-4">
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
                        <div className="flex flex-wrap gap-1 bg-black/5 rounded-[18px] p-1 w-full sm:w-auto">
                            <button
                                onClick={() => setFilterRole("ALL")}
                                className={cn(
                                    "px-4 h-9 rounded-2xl text-[12px] font-extrabold uppercase tracking-wider transition-all",
                                    filterRole === "ALL"
                                        ? "bg-white/60 shadow-sm text-[var(--navy)]"
                                        : "text-black/30 hover:text-black/50"
                                )}
                            >
                                Все
                            </button>
                            {ROLES.map((r) => (
                                <button
                                    key={r.value}
                                    onClick={() => setFilterRole(r.value)}
                                    className={cn(
                                        "px-4 h-9 flex items-center gap-2 rounded-2xl text-[12px] font-extrabold uppercase tracking-wider transition-all",
                                        filterRole === r.value
                                            ? cn("bg-white/60 shadow-sm", r.color)
                                            : "text-black/30 hover:text-black/50"
                                    )}
                                >
                                    {r.icon}
                                    <span className="hidden sm:inline">{r.label}</span>
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className="glass-card rounded-[32px] p-2 flex-1 flex flex-col backdrop-blur-md">
                        <div className="p-4 px-6 flex items-center justify-between border-b border-black/5 flex-wrap gap-4">
                            <div className="flex items-center gap-3">
                                <h2 className="text-base font-black text-[var(--navy)] flex items-center gap-2">
                                    <Users className="w-4 h-4 text-[var(--red)]" />
                                    Список пользователей
                                </h2>
                                {totalElements > 0 && (
                                    <span className="text-xs font-bold text-black/40 bg-black/5 px-3 py-1 rounded-full">
                                        Всего: {totalElements}
                                    </span>
                                )}
                            </div>
                        </div>

                        <div className="flex-1 p-2 overflow-y-auto max-h-[70vh]">
                            {isUsersLoading ? (
                                <div className="h-full flex items-center justify-center text-black/30">
                                    <Loader2 className="w-8 h-8 animate-spin" />
                                </div>
                            ) : users.length === 0 ? (
                                <div className="h-full flex flex-col items-center justify-center text-black/30 gap-2">
                                    <Filter className="w-8 h-8" />
                                    <p className="text-sm font-semibold">Пользователи не найдены</p>
                                </div>
                            ) : (
                                <div className="space-y-2">
                                    {users.map((user) => {
                                        const primaryRoleConfig = ROLES.find(
                                            (r) => r.value === user.roles[0]
                                        );
                                        const isParent = user.roles.includes("PARENT");
                                        const isStudent = user.roles.includes("STUDENT");

                                        return (
                                            <div
                                                key={user.id}
                                                className="group flex flex-col sm:flex-row sm:items-center justify-between p-3 px-4 rounded-[20px] hover:bg-white/40 transition-colors gap-3"
                                            >
                                                <div className="flex items-center gap-4">
                                                    <div
                                                        className={cn(
                                                            "w-10 h-10 rounded-[14px] flex shrink-0 items-center justify-center",
                                                            primaryRoleConfig?.iconBg,
                                                            primaryRoleConfig?.color
                                                        )}
                                                    >
                                                        {primaryRoleConfig?.icon || (
                                                            <UserRound className="w-4 h-4" />
                                                        )}
                                                    </div>
                                                    <div>
                                                        <p className="font-bold text-sm text-[var(--navy)]">
                                                            {user.firstName} {user.lastName}
                                                        </p>
                                                        <p className="text-xs font-semibold text-black/40">
                                                            @{user.username}
                                                        </p>
                                                    </div>
                                                </div>

                                                <div className="flex items-center justify-between sm:justify-end gap-3 w-full sm:w-auto">
                                                    <div className="flex flex-wrap gap-1">
                                                        {user.roles.map((roleValue) => {
                                                            const roleConfig = ROLES.find(
                                                                (r) => r.value === roleValue
                                                            );
                                                            if (!roleConfig) return null;
                                                            return (
                                                                <span
                                                                    key={roleValue}
                                                                    className={cn(
                                                                        "text-[10px] font-extrabold uppercase tracking-widest px-3 py-1 rounded-full bg-white/50",
                                                                        roleConfig.color
                                                                    )}
                                                                >
                                                                    {roleConfig.label}
                                                                </span>
                                                            );
                                                        })}
                                                    </div>

                                                    <div className="flex items-center gap-1">
                                                        {isParent && (
                                                            <button
                                                                onClick={() => setAssignParent(user)}
                                                                className="w-8 h-8 shrink-0 rounded-xl flex items-center justify-center text-black/20 hover:text-violet-600 hover:bg-violet-50 transition-all"
                                                                title="Управление учениками"
                                                            >
                                                                <Link2 className="w-4 h-4" />
                                                            </button>
                                                        )}

                                                        {isStudent && (
                                                            <button
                                                                onClick={() => setAssignStudent(user)}
                                                                className="w-8 h-8 shrink-0 rounded-xl flex items-center justify-center text-black/20 hover:text-blue-600 hover:bg-blue-50/70 transition-all"
                                                                title="Управление учениками"
                                                            >
                                                                <Link2 className="w-4 h-4" />
                                                            </button>
                                                        )}

                                                        <button
                                                            onClick={() => setEditUser(user)}
                                                            className="w-8 h-8 shrink-0 rounded-xl flex items-center justify-center text-black/20 hover:text-[var(--navy)] hover:bg-black/5 transition-all"
                                                            title="Редактировать"
                                                        >
                                                            <Pencil className="w-4 h-4" />
                                                        </button>

                                                        <button
                                                            onClick={() =>
                                                                handleDelete(
                                                                    user.id,
                                                                    `${user.firstName} ${user.lastName}`
                                                                )
                                                            }
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

                                    <div ref={loadMoreRef} className="h-10 flex items-center justify-center">
                                        {isFetchingNextPage && (
                                            <Loader2 className="w-5 h-5 animate-spin text-black/30" />
                                        )}
                                        {!hasNextPage && users.length > 0 && (
                                            <span className="text-xs font-bold text-black/30">
                                                Конец списка
                                            </span>
                                        )}
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}