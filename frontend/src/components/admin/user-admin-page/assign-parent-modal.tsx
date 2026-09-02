import {
    useAssignStudentToParent,
    useUnassignStudentFromParent,
    useStudentWithParent,
} from "@/hooks/use-student";
import type { UserResponse } from "@/services/user-service";
import {
    UserRound,
    X,
    Loader2,
    GraduationCap,
    UserMinus,
    Search,
    Filter,
    UserPlus,
} from "lucide-react";
import { useState, useRef, useEffect, useCallback } from "react";
import { Button } from "../../ui/button";
import { Input } from "../../ui/input";
import { useUnassignedToStudentParents } from "@/hooks/use-parent";

export default function AssignParentModal({
    student,
    onClose,
}: {
    student: UserResponse;
    onClose: () => void;
}) {
    const [parentSearch, setParentSearch] = useState("");
    const [debouncedParentSearch, setDebouncedParentSearch] = useState("");
    const overlayRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const t = setTimeout(() => setDebouncedParentSearch(parentSearch), 400);
        return () => clearTimeout(t);
    }, [parentSearch]);

    const { data: studentData, isLoading: isStudentLoading } = useStudentWithParent(student.id);

    const {
        data,
        isLoading: isParentsLoading,
        isFetchingNextPage,
        hasNextPage,
        fetchNextPage,
    } = useUnassignedToStudentParents(20, debouncedParentSearch || undefined);

    const allParents = data?.pages.flatMap((page) => page.content) ?? [];

    const assignMutation = useAssignStudentToParent();
    const unassignMutation = useUnassignStudentFromParent();

    const currentParent = studentData?.parent ?? null;
    const hasParent = !!currentParent;

    const handleOverlayClick = (e: React.MouseEvent) => {
        if (e.target === overlayRef.current) onClose();
    };

    useEffect(() => {
        document.body.style.overflow = "hidden";
        return () => {
            document.body.style.overflow = "";
        };
    }, []);

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

    const isPending = assignMutation.isPending || unassignMutation.isPending;

    return (
        <div
            ref={overlayRef}
            onClick={handleOverlayClick}
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm px-4"
        >
            <div className="w-full max-w-lg bg-white/80 backdrop-blur-xl rounded-[32px] shadow-2xl border border-white/60 flex flex-col max-h-[85vh] overflow-hidden">
                <div className="px-6 pt-6 pb-4 border-b border-black/5 flex items-start justify-between gap-4">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-[14px] bg-blue-50/70 flex items-center justify-center text-blue-600 shrink-0">
                            <GraduationCap className="w-4 h-4" />
                        </div>
                        <div>
                            <p className="font-black text-[var(--navy)] text-base leading-tight">
                                {student.firstName} {student.lastName}
                            </p>
                            <p className="text-xs font-semibold text-black/40">
                                @{student.username}
                            </p>
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
                    <div>
                        <p className="text-xs font-bold tracking-widest uppercase text-black/30 mb-3">
                            Привязанный родитель
                        </p>

                        {isStudentLoading ? (
                            <div className="flex justify-center py-4">
                                <Loader2 className="w-5 h-5 animate-spin text-black/30" />
                            </div>
                        ) : !currentParent ? (
                            <div className="flex items-center gap-2 py-3 px-4 rounded-2xl bg-black/[0.03] text-black/30">
                                <UserRound className="w-4 h-4 shrink-0" />
                                <p className="text-xs font-semibold">Родитель не привязан</p>
                            </div>
                        ) : (
                            <div className="flex items-center justify-between px-4 py-2.5 rounded-2xl bg-violet-50/50 border border-violet-100/60">
                                <div className="flex items-center gap-3">
                                    <div className="w-7 h-7 rounded-[10px] bg-violet-100/70 flex items-center justify-center text-violet-600">
                                        <UserRound className="w-3.5 h-3.5" />
                                    </div>
                                    <div>
                                        <p className="text-sm font-bold text-[var(--navy)] leading-none">
                                            {currentParent.firstName} {currentParent.lastName}
                                        </p>
                                        <p className="text-[11px] font-semibold text-black/30 mt-0.5">
                                            @{currentParent.username}
                                        </p>
                                    </div>
                                </div>
                                <button
                                    onClick={() =>
                                        unassignMutation.mutate({
                                            studentId: student.id,
                                            parentId: currentParent.id,
                                        })
                                    }
                                    disabled={isPending}
                                    className="w-7 h-7 rounded-xl flex items-center justify-center text-black/20 hover:text-red-500 hover:bg-red-50 transition-all disabled:opacity-40"
                                    title="Отвязать"
                                >
                                    <UserMinus className="w-3.5 h-3.5" />
                                </button>
                            </div>
                        )}
                    </div>

                    {!hasParent && (
                        <>
                            <div className="flex items-center gap-3">
                                <div className="flex-1 h-px bg-black/8" />
                                <span className="text-[10px] font-bold tracking-widest uppercase text-black/20">
                                    Добавить родителя
                                </span>
                                <div className="flex-1 h-px bg-black/8" />
                            </div>

                            <div className="space-y-3">
                                <div className="relative">
                                    <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-black/30" />
                                    <Input
                                        placeholder="Поиск по имени..."
                                        value={parentSearch}
                                        onChange={(e) => setParentSearch(e.target.value)}
                                        className="h-10 pl-10 bg-white/50 border-black/10 rounded-2xl text-sm font-semibold placeholder:font-normal focus-visible:ring-[var(--red)]"
                                    />
                                </div>

                                {isParentsLoading ? (
                                    <div className="flex justify-center py-4">
                                        <Loader2 className="w-5 h-5 animate-spin text-black/30" />
                                    </div>
                                ) : allParents.length === 0 ? (
                                    <div className="flex items-center gap-2 py-3 px-4 rounded-2xl bg-black/[0.03] text-black/30">
                                        <Filter className="w-4 h-4 shrink-0" />
                                        <p className="text-xs font-semibold">Родители не найдены</p>
                                    </div>
                                ) : (
                                    <div className="space-y-1.5">
                                        {allParents.map((parent) => (
                                            <div
                                                key={parent.id}
                                                className="flex items-center justify-between px-4 py-2.5 rounded-2xl hover:bg-white/50 transition-colors"
                                            >
                                                <div className="flex items-center gap-3">
                                                    <div className="w-7 h-7 rounded-[10px] bg-violet-50/70 flex items-center justify-center text-violet-500">
                                                        <UserRound className="w-3.5 h-3.5" />
                                                    </div>
                                                    <div>
                                                        <p className="text-sm font-bold text-[var(--navy)] leading-none">
                                                            {parent.firstName} {parent.lastName}
                                                        </p>
                                                        <p className="text-[11px] font-semibold text-black/30 mt-0.5">
                                                            @{parent.username}
                                                        </p>
                                                    </div>
                                                </div>

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
                                            </div>
                                        ))}

                                        <div
                                            ref={loadMoreRef}
                                            className="h-8 flex items-center justify-center"
                                        >
                                            {isFetchingNextPage && (
                                                <Loader2 className="w-4 h-4 animate-spin text-black/30" />
                                            )}
                                            {!hasNextPage && allParents.length > 0 && (
                                                <span className="text-[10px] font-bold text-black/20">
                                                    Конец списка
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                )}
                            </div>
                        </>
                    )}

                    {hasParent && (
                        <p className="text-[11px] font-semibold text-black/30 text-center">
                            Чтобы привязать другого родителя — сначала отвяжите текущего
                        </p>
                    )}
                </div>

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