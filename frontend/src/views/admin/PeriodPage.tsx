import { useState } from "react";
import {
    CalendarDays,
    Plus,
    Loader2,
    BookOpen,
    Search,
} from "lucide-react";
import { useCloseAcademicPeriod, useDeleteAcademicPeriod, useGetAcademicPeriods, useOpenAcademicPeriod } from "@/hooks/use-academic-period";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { AcademicPeriodResponse } from "@/services/academic-period-service";
import AdminNavbar from "@/templates/navbars/AdminNavbar";
import PeriodCard from "@/components/admin/academic-period-page/period-card";
import CreatePeriodForm from "@/components/admin/academic-period-page/create-period-form";


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
            <AdminNavbar />

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

                <div className="lg:col-span-2">
                    <div className="glass-card rounded-[32px] p-6 backdrop-blur-md min-h-[500px] flex flex-col">

                        <h2 className="font-serif font-black text-lg text-[var(--navy)] tracking-tight flex items-center gap-2 mb-5">
                            <CalendarDays className="w-5 h-5 text-[var(--red)]" />
                            Все четверти
                        </h2>

                        {/* Список четвертей */}
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