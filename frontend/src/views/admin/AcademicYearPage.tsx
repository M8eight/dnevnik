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
    useOpenAcademicYear,
    useCloseAcademicYear
} from "@/hooks/use-academic-year";
import type { AcademicYearResponse } from "@/services/academic-year-service";
import PageHeader from "@/components/admin/page-header";

export default function AcademicYearPage() {
    const [search, setSearch] = useState("");

    const { data: academicYears = [], isLoading } = useGetAcademicYears();

    const openMutation = useOpenAcademicYear();
    const closeMutation = useCloseAcademicYear();
    const deleteMutation = useDeleteAcademicYear();

    const handleToggleActive = (year: AcademicYearResponse) => {
        if (year.closed) {
            openMutation.mutate(year.id);
        } else {
            closeMutation.mutate(year.id);
        }
    };

    const filteredYears = academicYears.filter((year) => 
        year.name.toLowerCase().includes(search.toLowerCase())
    );

    const activeCount = academicYears.filter((y) => !y.closed).length;

    const getYearWord = (count: number) => {
        const rules = new Intl.PluralRules("ru-RU");
        const form = rules.select(count);
        if (form === "one") return "год";
        if (form === "few") return "года";
        return "лет";
    };

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">

            <AdminNavbar />

            <PageHeader
                icon={CalendarDays}
                title="Учебные годы"
                subtitle={
                    isLoading
                        ? "Загрузка..."
                        : `${academicYears.length} ${getYearWord(academicYears.length)} · ${activeCount} активен`
                }
            >
                <div className="relative w-full lg:w-70 float-end">
                    <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-black/30" />
                    <Input
                        placeholder="Поиск года..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="pl-10 h-11 bg-white/40 border-black/10 rounded-2xl text-sm font-semibold placeholder:font-normal focus-visible:ring-(--red)"
                    />
                </div>
            </PageHeader>

            <div className="max-w-350 mx-auto grid grid-cols-1 lg:grid-cols-3 gap-6">

                <div className="lg:col-span-2">
                    <div className="glass-card rounded-[32px] p-6 backdrop-blur-md min-h-125 flex flex-col">

                        <h2 className="font-serif font-black text-lg text-(--navy) tracking-tight flex items-center gap-2 mb-5">
                            <CalendarDays className="w-5 h-5 text-(--red)" />
                            Все учебные годы
                        </h2>

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
                                                openMutation.isPending && 
                                                openMutation.variables === year.id 
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
                            <h2 className="text-base font-black text-(--navy) flex items-center gap-2 mb-5">
                                <Plus className="w-4 h-4 text-(--red)" />
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