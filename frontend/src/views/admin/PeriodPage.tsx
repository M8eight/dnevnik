import { useMemo, useState } from "react";
import {
    CalendarDays,
    Plus,
    Loader2,
    BookOpen,
    Search,
} from "lucide-react";
import {
    useCloseAcademicPeriod,
    useDeleteAcademicPeriod,
    useGetAcademicPeriodsByAcademicYear,
    useOpenAcademicPeriod,
    useUpdateAcademicPeriod,
} from "@/hooks/use-academic-period";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { AcademicPeriodResponse } from "@/services/academic-period-service";
import AdminNavbar from "@/components/layout/navbars/AdminNavbar";
import PeriodCard from "@/components/admin/academic-period-page/period-card";
import CreatePeriodForm from "@/components/admin/academic-period-page/create-period-form";
import { useAcademicYearSelection } from "@/hooks/use-academic-year-selection";
import PageHeader from "@/components/admin/page-header";
import AcademicYearSelect from "@/components/admin/academic-year-select";
import ClosedYearAlert from "@/components/admin/closed-year-alert";

export default function AcademicPeriodPage() {
    const [search, setSearch] = useState("");

    const {
        resolvedAcademicYearId,
        setSelectedAcademicYearId,
        currentAcademicYear,
        isYearClosed,
    } = useAcademicYearSelection();

    const { data: periods = [], isLoading } = useGetAcademicPeriodsByAcademicYear(parseInt(resolvedAcademicYearId, 10));

    const openMutation = useOpenAcademicPeriod();
    const closeMutation = useCloseAcademicPeriod();
    const deleteMutation = useDeleteAcademicPeriod();
    const updateMutation = useUpdateAcademicPeriod();

    const handleToggle = (period: AcademicPeriodResponse) => {
        if (period.isClosed) {
            openMutation.mutate(period.id);
        } else {
            closeMutation.mutate(period.id);
        }
    };

    const filteredPeriods = useMemo(() => {
        return periods.filter((p) =>
            p.name.toLowerCase().includes(search.toLowerCase())
        );
    }, [periods, search]);

    const openCount = periods.filter((p) => !p.isClosed).length;

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">

            <AdminNavbar />

            <PageHeader
                icon={CalendarDays}
                title="Четверти"
                subtitle={
                    isLoading
                        ? "Загрузка..."
                        : `${periods.length} четверт${periods.length === 1 ? "ь" : periods.length < 5 ? "и" : "ей"} · ${openCount} открыт${openCount === 1 ? "а" : "о"}`
                }
            >
                <AcademicYearSelect
                    value={resolvedAcademicYearId}
                    onChange={setSelectedAcademicYearId}
                />

                <div className="relative w-full lg:w-70 float-end">
                    <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-black/30" />
                    <Input
                        placeholder="Поиск четверти..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="pl-10 h-11 bg-white/40 border-black/10 rounded-2xl text-sm font-semibold placeholder:font-normal focus-visible:ring-(--red)"
                    />
                </div>
            </PageHeader>

            {isYearClosed && (
                <ClosedYearAlert
                    yearName={currentAcademicYear?.name}
                    description="Операции удаления и редактирования четвертей запрещены"
                />
            )}

            <div className="max-w-350 mx-auto grid grid-cols-1 lg:grid-cols-3 gap-6">

                <div className="lg:col-span-2">
                    <div className="glass-card rounded-[32px] p-6 backdrop-blur-md min-h-125 flex flex-col">

                        <h2 className="font-serif font-black text-lg text-(--navy) tracking-tight flex items-center gap-2 mb-5">
                            <CalendarDays className="w-5 h-5 text-(--red)" />
                            Все четверти
                        </h2>

                        <ScrollArea className="flex-1 pr-2">
                            {isLoading ? (
                                <div className="flex flex-col items-center justify-center py-20 text-black/30">
                                    <Loader2 className="w-8 h-8 animate-spin" />
                                </div>
                            ) : filteredPeriods.length > 0 ? (
                                <div className="flex flex-col gap-3">
                                    {filteredPeriods.map((period, idx) => (
                                        <PeriodCard
                                            key={period.id}
                                            period={period}
                                            index={idx}
                                            onDelete={isYearClosed ? undefined : (id) => deleteMutation.mutate(id)}
                                            onToggle={isYearClosed ? undefined : handleToggle}
                                            onUpdate={isYearClosed ? undefined : (id, data) =>
                                                updateMutation.mutate({ id, request: data })
                                            }
                                            isDeleting={
                                                deleteMutation.isPending &&
                                                deleteMutation.variables === period.id
                                            }
                                            isToggling={
                                                (openMutation.isPending && openMutation.variables === period.id) ||
                                                (closeMutation.isPending && closeMutation.variables === period.id)
                                            }
                                            isUpdating={
                                                updateMutation.isPending &&
                                                updateMutation.variables?.id === period.id
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

                <div className="lg:col-span-1">
                    <div className="sticky top-6">
                        <div className="glass-card rounded-[32px] p-6 backdrop-blur-md">
                            <h2 className="text-base font-black text-(--navy) flex items-center gap-2 mb-5">
                                <Plus className="w-4 h-4 text-(--red)" />
                                Создать четверть
                            </h2>
                            {isYearClosed ? (
                                <div className="text-center py-8 px-4 border border-dashed border-black/10 rounded-2xl bg-black/2">
                                    <p className="text-sm font-semibold text-black/40">
                                        Создание четвертей в закрытом году недоступно
                                    </p>
                                </div>
                            ) : (
                                <CreatePeriodForm academicYearId={parseInt(resolvedAcademicYearId, 10)} />
                            )}
                        </div>
                    </div>
                </div>

            </div>
        </div>
    );
}