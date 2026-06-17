import { useState } from "react";
import {
    CalendarDays,
    Plus,
    Loader2,
    BookOpen,
    Search,
} from "lucide-react";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import AdminNavbar from "@/components/layout/navbars/AdminNavbar";

import YearCard from "@/components/admin/academic-year-page/year-card";
import CreateYearForm from "@/components/admin/academic-year-page/create-year-form";

import { 
    useGetAcademicYears, 
    useDeleteAcademicYear, 
    useSetActiveAcademicYear 
} from "@/hooks/use-academic-year";
import type { AcademicYearResponse } from "@/services/academic-year-service";

export default function AcademicYearPage() {
    const [search, setSearch] = useState("");

    // Получаем список годов
    const { data: academicYears = [], isLoading } = useGetAcademicYears();

    // Мутации
    const setActiveMutation = useSetActiveAcademicYear();
    const deleteMutation = useDeleteAcademicYear();

    // Обработчик активации/деактивации года
    const handleToggleActive = (year: AcademicYearResponse) => {
        // Передаем объект согласно обновленному хуку
        setActiveMutation.mutate({ id: year.id, active: !year.isActive });
    };

    // Фильтрация для поиска
    const filteredYears = academicYears.filter((year) => 
        year.name.toLowerCase().includes(search.toLowerCase())
    );

    const activeCount = academicYears.filter((y) => y.isActive).length;

    const getYearWord = (count: number) => {
        const rules = new Intl.PluralRules("ru-RU");
        const form = rules.select(count);
        if (form === "one") return "год";
        if (form === "few") return "года";
        return "лет";
    };

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
            {/* ── Header ── */}
            <AdminNavbar />

            {/* ── Controls bar ── */}
            <div className="max-w-[1400px] mx-auto mb-6">
                <div className="glass-card rounded-[24px] p-5 flex flex-col lg:flex-row justify-between lg:items-center gap-5 border-none shadow-lg backdrop-blur-md">

                    <div className="flex items-center gap-4">
                        <div className="hidden sm:flex w-12 h-12 rounded-[18px] bg-[var(--red-light)]/60 items-center justify-center ring-1 ring-[var(--red)]/10">
                            <CalendarDays className="w-6 h-6 text-[var(--red)]" />
                        </div>
                        <div>
                            <h1 className="font-serif font-black text-2xl lg:text-3xl text-[var(--navy)] tracking-tight">
                                Учебные годы
                            </h1>
                            <p className="text-sm text-black/40 mt-0.5">
                                {isLoading
                                    ? "Загрузка..."
                                    : `${academicYears.length} ${getYearWord(academicYears.length)} · ${activeCount} активен`}
                            </p>
                        </div>
                    </div>

                    <div className="flex flex-col lg:flex-row items-start lg:items-center gap-4 w-full lg:w-auto">
                        <div className="relative w-full lg:w-[280px] float-end">
                            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-black/30" />
                            <Input
                                placeholder="Поиск года..."
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                className="pl-10 h-11 bg-white/40 border-black/10 rounded-2xl text-sm font-semibold placeholder:font-normal focus-visible:ring-[var(--red)]"
                            />
                        </div>
                    </div>
                </div>
            </div>

            {/* ── Main grid ── */}
            <div className="max-w-[1400px] mx-auto grid grid-cols-1 lg:grid-cols-3 gap-6">

                <div className="lg:col-span-2">
                    <div className="glass-card rounded-[32px] p-6 backdrop-blur-md min-h-[500px] flex flex-col">

                        <h2 className="font-serif font-black text-lg text-[var(--navy)] tracking-tight flex items-center gap-2 mb-5">
                            <CalendarDays className="w-5 h-5 text-[var(--red)]" />
                            Все учебные годы
                        </h2>

                        {/* Список годов */}
                        <ScrollArea className="flex-1 pr-2">
                            {isLoading ? (
                                <div className="flex flex-col items-center justify-center py-20 text-black/30">
                                    <Loader2 className="w-8 h-8 animate-spin" />
                                </div>
                            ) : filteredYears.length > 0 ? (
                                <div className="flex flex-col gap-3">
                                    {filteredYears.map((year, idx) => (
                                        <YearCard
                                            key={year.id}
                                            year={year}
                                            index={idx}
                                            onDelete={(id) => deleteMutation.mutate(id)}
                                            onToggleActive={handleToggleActive}
                                            isDeleting={
                                                deleteMutation.isPending &&
                                                deleteMutation.variables === year.id
                                            }
                                            isSettingActive={
                                                setActiveMutation.isPending && 
                                                // Добавлено .id, так как variables теперь объект
                                                setActiveMutation.variables?.id === year.id 
                                            }
                                        />
                                    ))}
                                </div>
                            ) : (
                                <div className="flex flex-col items-center justify-center py-20 text-black/25">
                                    <BookOpen className="w-12 h-12 mb-3 opacity-30" />
                                    <p className="font-bold text-sm">
                                        {search ? "Годы по запросу не найдены" : "Список годов пуст"}
                                    </p>
                                </div>
                            )}
                        </ScrollArea>

                    </div>
                </div>

                <div className="lg:col-span-1">
                    <div className="sticky top-6">
                        <div className="glass-card rounded-[32px] p-6 backdrop-blur-md">
                            <h2 className="text-base font-black text-[var(--navy)] flex items-center gap-2 mb-5">
                                <Plus className="w-4 h-4 text-[var(--red)]" />
                                Добавить учебный год
                            </h2>
                            
                            <CreateYearForm />
                        </div>
                    </div>
                </div>

            </div>
        </div>
    );
}