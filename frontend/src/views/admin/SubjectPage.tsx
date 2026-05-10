import { useState } from "react";
import { cn } from "@/lib/utils";
import {
    BookOpen,
    Plus,
    Search,
    Layers,
    Loader2,
    ChevronRight,
    ChevronLeft,
} from "lucide-react";
import { useGetAllSubjects, useDeleteSubject } from "@/hooks/use-subject";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { SubjectResponse } from "@/services/subject-service";
import AdminNavbar from "@/templates/navbars/AdminNavbar";
import CreateSubjectForm from "@/components/admin/subject-page/create-subject-form";
import SubjectCard from "@/components/admin/subject-page/subject-card";


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
            <AdminNavbar />

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