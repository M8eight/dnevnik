import React, { useState } from "react";
import { cn } from "@/lib/utils";
import { NavLink } from "react-router-dom";
import {
    CalendarDays,
    Plus,
    Send,
    Layers,
    CheckCircle2,
    Loader2,
    Hash,
    Trash2,
    Lock,
    LockOpen,
    BookOpen,
    Search,
} from "lucide-react";
import { useCloseAcademicPeriod, useCreateAcademicPeriod, useDeleteAcademicPeriod, useGetAcademicPeriods, useOpenAcademicPeriod } from "@/hooks/use-academic-period";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { AcademicPeriodResponse } from "@/services/academic-period-service";

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

// ─── Accent palette ───────────────────────────────────────────────────────────

const ACCENT_PALETTE = [
    { light: "bg-[var(--red-light)]/60", icon: "text-[var(--red)]" },
    { light: "bg-blue-50/70",            icon: "text-blue-600" },
    { light: "bg-emerald-50/70",         icon: "text-emerald-600" },
    { light: "bg-violet-50/70",          icon: "text-violet-600" },
    { light: "bg-amber-50/70",           icon: "text-amber-600" },
    { light: "bg-pink-50/70",            icon: "text-pink-600" },
];

// ─── Format date ──────────────────────────────────────────────────────────────

function formatDate(iso: string) {
    return new Date(iso).toLocaleDateString("ru-RU", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
    });
}

// ─── PeriodCard ───────────────────────────────────────────────────────────────

function PeriodCard({
    period,
    index,
    onDelete,
    onToggle,
    isDeleting,
    isToggling,
}: {
    period: AcademicPeriodResponse;
    index: number;
    onDelete: (id: number) => void;
    onToggle: (period: AcademicPeriodResponse) => void;
    isDeleting: boolean;
    isToggling: boolean;
}) {
    const accent = ACCENT_PALETTE[index % ACCENT_PALETTE.length];
    const [confirmDelete, setConfirmDelete] = useState(false);

    const handleDeleteClick = () => {
        if (confirmDelete) {
            onDelete(period.id);
        } else {
            setConfirmDelete(true);
            setTimeout(() => setConfirmDelete(false), 3000);
        }
    };

    return (
        <div className="group glass-card rounded-[24px] p-5 backdrop-blur-md border border-white/40 hover:border-white/60 transition-all hover:shadow-xl hover:-translate-y-0.5">
            <div className="flex items-start gap-4">
                {/* Icon */}
                <div
                    className={cn(
                        "w-11 h-11 rounded-[14px] flex items-center justify-center shrink-0 ring-1 ring-black/5",
                        accent.light
                    )}
                >
                    <CalendarDays className={cn("w-5 h-5", accent.icon)} />
                </div>

                {/* Info */}
                <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                        <p className="font-black text-[var(--navy)] text-[15px] leading-snug truncate">
                            {period.name}
                        </p>
                        {/* Status badge */}
                        <span
                            className={cn(
                                "text-[10px] font-extrabold px-2.5 py-0.5 rounded-full tracking-wider uppercase",
                                period.isClosed
                                    ? "bg-black/5 text-black/35"
                                    : "bg-emerald-50 text-emerald-600 ring-1 ring-emerald-200"
                            )}
                        >
                            {period.isClosed ? "Закрыта" : "Открыта"}
                        </span>
                    </div>

                    <div className="flex items-center gap-3 mt-1.5 flex-wrap">
                        <div className="flex items-center gap-1">
                            <Hash className="w-3 h-3 text-black/20" />
                            <span className="text-[11px] font-bold text-black/30 tracking-wider">
                                {period.schoolYear}
                            </span>
                        </div>
                        <span className="text-[11px] font-semibold text-black/30">
                            {formatDate(period.startDate)} — {formatDate(period.endDate)}
                        </span>
                    </div>
                </div>

                {/* Actions */}
                <div className="flex items-center gap-1.5 shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
                    {/* Open / Close toggle */}
                    <button
                        onClick={() => onToggle(period)}
                        disabled={isToggling}
                        title={period.isClosed ? "Открыть четверть" : "Закрыть четверть"}
                        className={cn(
                            "w-8 h-8 rounded-[10px] flex items-center justify-center transition-all",
                            period.isClosed
                                ? "bg-emerald-50 hover:bg-emerald-100 text-emerald-600"
                                : "bg-black/5 hover:bg-amber-50 text-black/35 hover:text-amber-500"
                        )}
                    >
                        {isToggling ? (
                            <Loader2 className="w-3.5 h-3.5 animate-spin" />
                        ) : period.isClosed ? (
                            <LockOpen className="w-3.5 h-3.5" />
                        ) : (
                            <Lock className="w-3.5 h-3.5" />
                        )}
                    </button>

                    {/* Delete */}
                    <button
                        onClick={handleDeleteClick}
                        disabled={isDeleting}
                        title={confirmDelete ? "Нажмите ещё раз для подтверждения" : "Удалить четверть"}
                        className={cn(
                            "w-8 h-8 rounded-[10px] flex items-center justify-center transition-all",
                            confirmDelete
                                ? "bg-[var(--red)] text-white scale-105"
                                : "bg-black/5 hover:bg-red-50 text-black/25 hover:text-[var(--red)]"
                        )}
                    >
                        {isDeleting ? (
                            <Loader2 className="w-3.5 h-3.5 animate-spin" />
                        ) : (
                            <Trash2 className="w-3.5 h-3.5" />
                        )}
                    </button>
                </div>
            </div>

            {confirmDelete && (
                <p className="text-[11px] text-[var(--red)] font-bold mt-2 pl-[60px]">
                    Нажмите ещё раз для удаления
                </p>
            )}
        </div>
    );
}

// ─── CreatePeriodForm ─────────────────────────────────────────────────────────

function CreatePeriodForm() {
    const [form, setForm] = useState({
        name: "",
        schoolYear: "",
        startDate: "",
        endDate: "",
    });
    const [success, setSuccess] = useState(false);
    const createMutation = useCreateAcademicPeriod();

    const isValid =
        form.name.trim() &&
        form.schoolYear.trim() &&
        form.startDate &&
        form.endDate;

    const handleChange = (field: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm((prev) => ({ ...prev, [field]: e.target.value }));
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!isValid) return;
        createMutation.mutate(
            {
                name: form.name.trim(),
                schoolYear: form.schoolYear.trim(),
                startDate: form.startDate,
                endDate: form.endDate,
            },
            {
                onSuccess: () => {
                    setForm({ name: "", schoolYear: "", startDate: "", endDate: "" });
                    setSuccess(true);
                    setTimeout(() => setSuccess(false), 2500);
                },
            }
        );
    };

    const fieldClass =
        "h-11 bg-white/40 border-black/10 rounded-2xl focus-visible:ring-[var(--red)] text-sm font-semibold placeholder:font-normal";

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            {/* Name */}
            <div className="space-y-1.5">
                <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                    Название
                </label>
                <Input
                    placeholder="Введите название четверти"
                    value={form.name}
                    onChange={handleChange("name")}
                    disabled={createMutation.isPending}
                    className={fieldClass}
                />
            </div>

            {/* School year */}
            <div className="space-y-1.5">
                <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                    Учебный год
                </label>
                <Input
                    placeholder="Например: 2024-2025"
                    value={form.schoolYear}
                    onChange={handleChange("schoolYear")}
                    disabled={createMutation.isPending}
                    className={fieldClass}
                />
            </div>

            {/* Dates */}
            <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                    <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                        Начало
                    </label>
                    <Input
                        type="date"
                        value={form.startDate}
                        onChange={handleChange("startDate")}
                        disabled={createMutation.isPending}
                        className={fieldClass}
                    />
                </div>
                <div className="space-y-1.5">
                    <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                        Конец
                    </label>
                    <Input
                        type="date"
                        value={form.endDate}
                        onChange={handleChange("endDate")}
                        disabled={createMutation.isPending}
                        className={fieldClass}
                    />
                </div>
            </div>

            <Button
                type="submit"
                disabled={!isValid || createMutation.isPending}
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
                        Создана!
                    </>
                ) : (
                    <>
                        Создать четверть
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

export default function AcademicPeriodPage() {
    const [search, setSearch] = useState("");

    const { data: periods = [], isLoading } = useGetAcademicPeriods();

    const openMutation = useOpenAcademicPeriod();
    const closeMutation = useCloseAcademicPeriod();
    const deleteMutation = useDeleteAcademicPeriod();

    const handleToggle = (period: AcademicPeriodResponse) => {
        if (period.isClosed) {
            openMutation.mutate(period.id);
        } else {
            closeMutation.mutate(period.id);
        }
    };

    const filtered = periods.filter(
        (p) =>
            p.name.toLowerCase().includes(search.toLowerCase()) ||
            p.schoolYear.toLowerCase().includes(search.toLowerCase())
    );

    const openCount = periods.filter((p) => !p.isClosed).length;

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
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
                                <p className="text-[13px] font-black text-[var(--navy)] leading-none mb-1">
                                    Администратор
                                </p>
                                <p className="text-[9px] font-extrabold tracking-[0.2em] uppercase text-black/25">
                                    Admin
                                </p>
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
                            <CalendarDays className="w-6 h-6 text-[var(--red)]" />
                        </div>
                        <div>
                            <h1 className="font-serif font-black text-2xl lg:text-3xl text-[var(--navy)] tracking-tight">
                                Четверти
                            </h1>
                            <p className="text-sm text-black/40 mt-0.5">
                                {isLoading
                                    ? "Загрузка..."
                                    : `${periods.length} четверт${periods.length === 1 ? "ь" : periods.length < 5 ? "и" : "ей"} · ${openCount} открыт${openCount === 1 ? "а" : "о"}`}
                            </p>
                        </div>
                    </div>

                    {/* Search */}
                    <div className="relative w-full lg:w-[280px]">
                        <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-black/30" />
                        <Input
                            placeholder="Поиск четверти..."
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            className="pl-10 h-11 bg-white/40 border-black/10 rounded-2xl text-sm font-semibold placeholder:font-normal focus-visible:ring-[var(--red)]"
                        />
                    </div>
                </div>
            </div>

            {/* ── Main grid ── */}
            <div className="max-w-[1400px] mx-auto grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* List */}
                <div className="lg:col-span-2">
                    <div className="glass-card rounded-[32px] p-6 backdrop-blur-md min-h-[500px] flex flex-col">
                        <h2 className="font-serif font-black text-lg text-[var(--navy)] tracking-tight flex items-center gap-2 mb-5">
                            <CalendarDays className="w-5 h-5 text-[var(--red)]" />
                            Все четверти
                        </h2>

                        <ScrollArea className="flex-1 pr-2">
                            {isLoading ? (
                                <div className="flex flex-col items-center justify-center py-20 text-black/30">
                                    <Loader2 className="w-8 h-8 animate-spin" />
                                </div>
                            ) : filtered.length > 0 ? (
                                <div className="flex flex-col gap-3">
                                    {filtered.map((period, idx) => (
                                        <PeriodCard
                                            key={period.id}
                                            period={period}
                                            index={idx}
                                            onDelete={(id) => deleteMutation.mutate(id)}
                                            onToggle={handleToggle}
                                            isDeleting={
                                                deleteMutation.isPending &&
                                                deleteMutation.variables === period.id
                                            }
                                            isToggling={
                                                (openMutation.isPending && openMutation.variables === period.id) ||
                                                (closeMutation.isPending && closeMutation.variables === period.id)
                                            }
                                        />
                                    ))}
                                </div>
                            ) : (
                                <div className="flex flex-col items-center justify-center py-20 text-black/25">
                                    <BookOpen className="w-12 h-12 mb-3 opacity-30" />
                                    <p className="font-bold text-sm">Четверти не найдены</p>
                                </div>
                            )}
                        </ScrollArea>
                    </div>
                </div>

                {/* Form */}
                <div className="lg:col-span-1">
                    <div className="sticky top-6">
                        <div className="glass-card rounded-[32px] p-6 backdrop-blur-md">
                            <h2 className="text-base font-black text-[var(--navy)] flex items-center gap-2 mb-5">
                                <Plus className="w-4 h-4 text-[var(--red)]" />
                                Создать четверть
                            </h2>
                            <CreatePeriodForm />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}