import { useParent } from "@/hooks/use-parent";
import { useAssignStudentToParent, useUnassignStudentFromParent } from "@/hooks/use-student";
import { useFindUsersByFilter } from "@/hooks/use-user";
import { cn } from "@/lib/utils";
import type { UserResponse } from "@/services/user-service";
import { UserRound, X, Loader2, GraduationCap, UserMinus, Search, Filter, UserPlus } from "lucide-react";
import { useState, useRef, useEffect } from "react";
import { Button } from "../../ui/button";
import { Input } from "../../ui/input";


export default function AssignStudentsModal({
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