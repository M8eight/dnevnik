import React, { useState } from "react";
import { cn } from "@/lib/utils";
import { NavLink } from "react-router-dom";
import {
    BookOpen,
    Plus,
    Send,
    Search,
    Layers,
    CheckCircle2,
    Loader2,
    Hash,
    Trash2,
    ChevronRight,
    ChevronLeft,
} from "lucide-react";
import { useGetAllSubjects, useCreateSubject, useDeleteSubject } from "@/hooks/use-subject";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { SubjectResponse } from "@/services/subject-service";

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

// ─── SubjectCard ──────────────────────────────────────────────────────────────

const ACCENT_PALETTE = [
    { light: "bg-[var(--red-light)]/60", dot: "bg-[var(--red)]", text: "text-[var(--red)]" },
    { light: "bg-blue-50/70", dot: "bg-blue-500", text: "text-blue-600" },
    { light: "bg-emerald-50/70", dot: "bg-emerald-500", text: "text-emerald-600" },
    { light: "bg-violet-50/70", dot: "bg-violet-500", text: "text-violet-600" },
    { light: "bg-amber-50/70", dot: "bg-amber-500", text: "text-amber-600" },
    { light: "bg-pink-50/70", dot: "bg-pink-500", text: "text-pink-600" },
];

function SubjectCard({
    subject,
    index,
    onDelete,
    isDeleting,
}: {
    subject: SubjectResponse;
    index: number;
    onDelete: (id: number) => void;
    isDeleting: boolean;
}) {
    const accent = ACCENT_PALETTE[index % ACCENT_PALETTE.length];
    const [confirmDelete, setConfirmDelete] = useState(false);

    const handleDeleteClick = () => {
        if (confirmDelete) {
            onDelete(subject.id);
        } else {
            setConfirmDelete(true);
            setTimeout(() => setConfirmDelete(false), 3000);
        }
    };

    return (
        <div className="group glass-card rounded-[24px] p-5 backdrop-blur-md border border-white/40 hover:border-white/60 transition-all hover:shadow-xl hover:-translate-y-0.5">
            <div className="flex items-start gap-4">
                <div
                    className={cn(
                        "w-11 h-11 rounded-[14px] flex items-center justify-center shrink-0 ring-1 ring-black/5",
                        accent.light
                    )}
                >
                    <BookOpen className={cn("w-5 h-5", accent.text)} />
                </div>
                <div className="flex-1 min-w-0">
                    <p className="font-black text-[var(--navy)] text-[15px] leading-snug truncate">
                        {subject.name}
                    </p>
                    <div className="flex items-center gap-1.5 mt-1">
                        <Hash className="w-3 h-3 text-black/20" />
                        <span className="text-[11px] font-bold text-black/30 tracking-wider">
                            ID {subject.id}
                        </span>
                    </div>
                </div>

                {/* Delete button */}
                <button
                    onClick={handleDeleteClick}
                    disabled={isDeleting}
                    className={cn(
                        "shrink-0 w-8 h-8 rounded-[10px] flex items-center justify-center transition-all opacity-0 group-hover:opacity-100",
                        confirmDelete
                            ? "bg-[var(--red)] text-white scale-105 opacity-100"
                            : "bg-black/5 hover:bg-red-50 text-black/25 hover:text-[var(--red)]"
                    )}
                    title={confirmDelete ? "Нажмите ещё раз для подтверждения" : "Удалить предмет"}
                >
                    {isDeleting ? (
                        <Loader2 className="w-3.5 h-3.5 animate-spin" />
                    ) : (
                        <Trash2 className="w-3.5 h-3.5" />
                    )}
                </button>
            </div>

            {confirmDelete && (
                <p className="text-[11px] text-[var(--red)] font-bold mt-2 pl-[60px]">
                    Нажмите ещё раз для удаления
                </p>
            )}
        </div>
    );
}

// ─── CreateSubjectForm ────────────────────────────────────────────────────────

function CreateSubjectForm() {
    const [name, setName] = useState("");
    const [success, setSuccess] = useState(false);
    const createMutation = useCreateSubject();

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!name.trim()) return;
        createMutation.mutate(
            { subjectName: name.trim() },
            {
                onSuccess: () => {
                    setName("");
                    setSuccess(true);
                    setTimeout(() => setSuccess(false), 2500);
                },
            }
        );
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
                <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                    Название предмета
                </label>
                <Input
                    placeholder="Например: Математика"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    disabled={createMutation.isPending}
                    className="h-12 bg-white/40 border-black/10 rounded-2xl focus-visible:ring-[var(--red)] text-sm font-semibold placeholder:font-normal"
                />
            </div>

            <Button
                type="submit"
                disabled={!name.trim() || createMutation.isPending}
                className="w-full gap-2 bg-[var(--red)] hover:bg-[var(--red-dark)] text-white rounded-2xl py-6 text-sm font-bold shadow-lg shadow-[var(--red)]/20 transition-all active:scale-[0.98] disabled:opacity-40"
            >
                {createMutation.isPending ? (
                    <>
                        <Loader2 className="w-4 h-4 animate-spin" />
                        Создание...
                    </>
                ) : success ? (
                    <>
                        <CheckCircle2 className="w-4 h-4" />
                        Создан!
                    </>
                ) : (
                    <>
                        Создать предмет
                        <Send className="w-4 h-4" />
                    </>
                )}
            </Button>

            {createMutation.isError && (
                <p className="text-xs text-[var(--red)] font-semibold text-center">
                    Ошибка при создании. Попробуйте ещё раз.
                </p>
            )}
        </form>
    );
}

// ─── Main page ────────────────────────────────────────────────────────────────

export default function SubjectsPage() {
    const [page, setPage] = useState(0);
    const [pageSize] = useState(20);
    const [search, setSearch] = useState("");

    const { data: pageData, isLoading, isPlaceholderData } = useGetAllSubjects(page, pageSize);

    const subjects: SubjectResponse[] = pageData?.content ?? [];
    const totalPages = pageData?.totalPages ?? 0;
    const isLast = pageData?.last ?? true;
    const isFirst = pageData?.first ?? true;

    const deleteMutation = useDeleteSubject();

    const filtered = subjects.filter((s) =>
        s.name.toLowerCase().includes(search.toLowerCase())
    );

    // Функции навигации
    const handleNextPage = () => {
        if (!isLast) setPage((old) => old + 1);
    };

    const handlePrevPage = () => {
        setPage((old) => Math.max(old - 1, 0));
    };

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
            {/* ── Header ── */}
            <header className="mb-6">
                <div className="max-w-[1400px] mx-auto">
                    <div className="glass-card rounded-[24px] px-6 h-16 flex items-center justify-between border-none shadow-lg backdrop-blur-md">
                        {/* Logo */}
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 rounded-[14px] bg-[var(--red-light)]/60 flex items-center justify-center ring-1 ring-[var(--red)]/10">
                                <Layers className="w-5 h-5 text-[var(--red)]" />
                            </div>
                            <span className="font-serif font-black text-[1.2rem] text-[var(--navy)] tracking-tight">
                                Панель администратора
                            </span>
                        </div>

                        {/* Nav */}
                        <nav className="hidden lg:flex items-center gap-2">
                            <NavItem to="/admin/subject" label="Предмет" />
                            <NavItem to="/admin/period" label="Четверть" />
                            <NavItem to="/admin/class" label="Класс" />
                            <NavItem to="/admin/user" label="Пользователь" />
                            <NavItem to="/admin/schedule" label="Расписание" />
                        </nav>

                        {/* Avatar */}
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
                <div className="glass-card rounded-[24px] p-5 flex flex-col lg:flex-row lg:items-center justify-between gap-5 border-none shadow-lg backdrop-blur-md">
                    <div className="flex items-center gap-4">
                        <div className="hidden sm:flex w-12 h-12 rounded-[18px] bg-[var(--red-light)]/60 items-center justify-center ring-1 ring-[var(--red)]/10">
                            <BookOpen className="w-6 h-6 text-[var(--red)]" />
                        </div>
                        <div>
                            <h1 className="font-serif font-black text-2xl lg:text-3xl text-[var(--navy)] tracking-tight">
                                Предметы
                            </h1>
                            <p className="text-sm text-black/40 mt-0.5">
                                {isLoading
                                    ? "Загрузка..."
                                    : `${subjects.length} предмет${subjects.length === 1 ? "" : subjects.length < 5 ? "а" : "ов"} в системе`}
                            </p>
                        </div>
                    </div>

                    {/* Search */}
                    <div className="relative w-full lg:w-[280px]">
                        <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-black/30" />
                        <Input
                            placeholder="Поиск предмета..."
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            className="pl-10 h-11 bg-white/40 border-black/10 rounded-2xl text-sm font-semibold placeholder:font-normal focus-visible:ring-[var(--red)]"
                        />
                    </div>
                </div>
            </div>

            {/* ── Main grid ── */}
            <div className="max-w-[1400px] mx-auto grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="lg:col-span-2">
                    <div className="glass-card rounded-[32px] p-6 backdrop-blur-md min-h-[600px] flex flex-col">

                        {/* Заголовок списка */}
                        <div className="flex items-center justify-between mb-5">
                            <h2 className="font-serif font-black text-lg text-[var(--navy)] tracking-tight flex items-center gap-2">
                                <Layers className="w-5 h-5 text-[var(--red)]" />
                                Все предметы
                            </h2>
                            <div className="flex items-center gap-2">
                                {isPlaceholderData && <Loader2 className="w-4 h-4 animate-spin text-black/20" />}
                                <span className="text-xs font-bold text-black/30 bg-black/5 px-3 py-1 rounded-full">
                                    Страница {page + 1} из {totalPages || 1}
                                </span>
                            </div>
                        </div>

                        {/* Список предметов */}
                        <ScrollArea className="flex-1 pr-2">
                            {isLoading ? (
                                <div className="flex flex-col items-center justify-center py-20 text-black/30">
                                    <Loader2 className="w-8 h-8 animate-spin" />
                                </div>
                            ) : filtered.length > 0 ? (
                                <div className={cn(
                                    "grid grid-cols-1 sm:grid-cols-2 gap-3 transition-opacity",
                                    isPlaceholderData ? "opacity-50" : "opacity-100"
                                )}>
                                    {filtered.map((subject, idx) => (
                                        <SubjectCard
                                            key={subject.id}
                                            subject={subject}
                                            index={idx}
                                            onDelete={(id) => deleteMutation.mutate(id)}
                                            isDeleting={deleteMutation.isPending && deleteMutation.variables === subject.id}
                                        />
                                    ))}
                                </div>
                            ) : (
                                <div className="flex flex-col items-center justify-center py-20 text-black/25">
                                    <BookOpen className="w-12 h-12 mb-3 opacity-30" />
                                    <p className="font-bold text-sm">Предметы не найдены</p>
                                </div>
                            )}
                        </ScrollArea>

                        {/* ─── Блок пагинации ─── */}
                        <div className="flex items-center justify-between mt-6 pt-4 border-t border-black/5">
                            <Button
                                variant="ghost"
                                onClick={handlePrevPage}
                                disabled={isFirst || isLoading}
                                className="gap-2 rounded-xl hover:bg-white/40 font-bold text-[var(--navy)]"
                            >
                                <ChevronLeft className="w-4 h-4" /> Назад
                            </Button>

                            <div className="flex gap-1">
                                {[...Array(totalPages)].map((_, i) => (
                                    <button
                                        key={i}
                                        onClick={() => setPage(i)}
                                        className={cn(
                                            "w-8 h-8 rounded-lg text-xs font-black transition-all",
                                            page === i
                                                ? "bg-[var(--navy)] text-white shadow-md"
                                                : "text-black/40 hover:bg-white/40"
                                        )}
                                    >
                                        {i + 1}
                                    </button>
                                )).slice(Math.max(0, page - 2), Math.min(totalPages, page + 3))}
                            </div>

                            <Button
                                variant="ghost"
                                onClick={handleNextPage}
                                disabled={isLast || isLoading}
                                className="gap-2 rounded-xl hover:bg-white/40 font-bold text-[var(--navy)]"
                            >
                                Вперед <ChevronRight className="w-4 h-4" />
                            </Button>
                        </div>
                    </div>
                </div>

                <div className="lg:col-span-1">
                    <div className="sticky top-6">
                        <div className="glass-card rounded-[32px] p-6 backdrop-blur-md">
                            <h2 className="text-base font-black text-[var(--navy)] flex items-center gap-2 mb-5">
                                <Plus className="w-4 h-4 text-[var(--red)]" />
                                Создать предмет
                            </h2>
                            <CreateSubjectForm />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}