import { useMemo, useState } from "react";
import {
    CalendarDays,
    Plus,
    Loader2,
    BookOpen,
    Search,
    CalendarClock,
    AlertTriangle,
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
import { useGetAcademicYears } from "@/hooks/use-academic-year";
import { SelectTrigger, SelectValue, SelectContent, SelectItem, Select } from "@/components/ui/select";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";

export default function AcademicPeriodPage() {
    const [search, setSearch] = useState("");

    const { data: academicYears } = useGetAcademicYears();
    const [selectedAcademicYearId, setSelectedAcademicYearId] = useState<string>("");

    const defaultAcademicYearId = useMemo(() => {
        if (!academicYears?.length) return "";
        return academicYears[0].id.toString();
    }, [academicYears]);

    const resolvedAcademicYearId = selectedAcademicYearId || defaultAcademicYearId;

    const currentAcademicYear = useMemo(() => {
        return academicYears?.find(year => year.id.toString() === resolvedAcademicYearId);
    }, [academicYears, resolvedAcademicYearId]);

    const isYearClosed = currentAcademicYear ? !currentAcademicYear.isActive : false;

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
            {/* ── Header ── */}
            <AdminNavbar />

            {/* ── Controls bar ── */}
            <div className="max-w-350 mx-auto mb-6">
                <div className="glass-card rounded-[24px] p-5 flex flex-col lg:flex-row justify-between lg:items-center gap-5 border-none shadow-lg backdrop-blur-md">

                    <div className="flex items-center gap-4">
                        <div className="hidden sm:flex w-12 h-12 rounded-[18px] bg-(--red-light)/60 items-center justify-center ring-1 ring-(--red)/10">
                            <CalendarDays className="w-6 h-6 text-(--red)" />
                        </div>
                        <div>
                            <h1 className="font-serif font-black text-2xl lg:text-3xl text-(--navy) tracking-tight">
                                Четверти
                            </h1>
                            <p className="text-sm text-black/40 mt-0.5">
                                {isLoading
                                    ? "Загрузка..."
                                    : `${periods.length} четверт${periods.length === 1 ? "ь" : periods.length < 5 ? "и" : "ей"} · ${openCount} открыт${openCount === 1 ? "а" : "о"}`}
                            </p>
                        </div>
                    </div>

                    <div className="flex flex-col lg:flex-row items-start lg:items-center gap-4 w-full lg:w-auto">

                        <Select
                            value={selectedAcademicYearId || academicYears?.[0]?.id.toString() || ""}
                            onValueChange={setSelectedAcademicYearId}
                        >
                            <SelectTrigger className="glass-pill h-10 px-5 text-[12px] font-bold rounded-2xl text-(--navy) border-0 shadow-sm gap-2 min-w-45">
                                <CalendarClock className="w-4 h-4 text-(--red)" />
                                <SelectValue placeholder="Выберите год" />
                            </SelectTrigger>
                            <SelectContent className="rounded-2xl border-none shadow-2xl bg-white/95 backdrop-blur-xl max-h-87.5">
                                {academicYears?.map((academicYear) => (
                                    <SelectItem key={academicYear.id} value={academicYear.id.toString()} className="font-bold text-[13px] py-3 rounded-xl cursor-pointer">
                                        {academicYear.name} {!academicYear.isActive && "(Архив)"}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>

                        <div className="relative w-full lg:w-70 float-end">
                            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-black/30" />
                            <Input
                                placeholder="Поиск четверти..."
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                className="pl-10 h-11 bg-white/40 border-black/10 rounded-2xl text-sm font-semibold placeholder:font-normal focus-visible:ring-(--red)"
                            />
                        </div>

                    </div>
                </div>
            </div>

            {/* Closed year alert */}
            {isYearClosed && (
                <div className="max-w-350 mx-auto mb-6 animate-in fade-in slide-in-from-top-2 duration-300">
                    <Alert variant="destructive" className="rounded-[24px] bg-linear-to-r from-red-50 to-red-50/50 border-red-200/80 shadow-lg backdrop-blur-sm">
                        <div className="flex items-start gap-4">
                            <div className="shrink-0 mt-0.5 w-10 h-10 rounded-[14px] bg-red-100/60 flex items-center justify-center">
                                <AlertTriangle className="h-5 w-5 text-yellow-600" />
                            </div>
                            <div className="flex-1">
                                <AlertTitle className="font-serif font-black tracking-tight text-base text-yellow-900 mb-1">
                                    Учебный год <span className="font-bold text-yellow-900">({currentAcademicYear?.name})</span> закрыт
                                </AlertTitle>
                                <AlertDescription className="text-sm text-yellow-800/85 font-medium leading-relaxed">
                                    Операции удаления и редактирования четвертей запрещены
                                </AlertDescription>
                            </div>
                        </div>
                    </Alert>
                </div>
            )}

            {/* ── Main grid ── */}
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