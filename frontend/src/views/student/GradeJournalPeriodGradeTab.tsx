import Chip from "@/components/student/chip";
import GradeBadge from "@/components/student/grades-page/grade-badge";
import { avgColor } from "@/components/student/grades-page/grades-page-helper";
import StatCard from "@/components/student/grades-page/stat-card";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";
import { TableHead, TableHeader, TableRow, TableBody, TableCell, Table } from "@/components/ui/table";
import {  useGetAcademicPeriodsByAcademicYear } from "@/hooks/use-academic-period";
import { useFinalGradesByStudent } from "@/hooks/use-final-grade";
import { usePeriodGradesByStudent } from "@/hooks/use-period-grade";
import type { FinalGradesStudentResponse } from "@/services/final-grade-service";
import { Loader2, TrendingUp, Star, BookOpen, BarChart2 } from "lucide-react";
import { useMemo } from "react";

export interface GradeJournalGradeTabProps {
    academicYearId: number;
}

export default function GradeJournalPeriodGradeTab({ academicYearId }: GradeJournalGradeTabProps) {
    const { data: periods = [], isLoading: isLoadingPeriods } = useGetAcademicPeriodsByAcademicYear(academicYearId);

    const schoolYear = periods[0]?.academicYear.name ?? "";

    const { data: periodGradesMap = {}, isLoading: isPeriodLoading } =
        usePeriodGradesByStudent(academicYearId);

    const { data: finalGradesMap = {} as FinalGradesStudentResponse, isLoading: isFinalLoading } =
        useFinalGradesByStudent(academicYearId);

    const isLoading = isLoadingPeriods || isPeriodLoading || isFinalLoading;

    const subjects = useMemo(() => Object.keys(periodGradesMap), [periodGradesMap]);

    // Периоды отсортированы по id
    const sortedPeriods = useMemo(
        () => [...periods].sort((a, b) => a.id - b.id),
        [periods]
    );

    const allPeriodGrades = useMemo(
        () => Object.values(periodGradesMap).flat(),
        [periodGradesMap]
    );

    const overallAvg = allPeriodGrades.length
        ? allPeriodGrades.reduce((sum, g) => sum + g.value, 0) / allPeriodGrades.length
        : 0;

    const fivesCount = allPeriodGrades.filter((g) => g.value === 5).length;

    const getFinalGrade = (subjectName: string) =>
        finalGradesMap[subjectName];

    if (isLoading) {
        return (
            <div className="flex h-60 items-center justify-center">
                <div className="glass-card rounded-[28px] p-10 flex flex-col items-center gap-4">
                    <Loader2 className="w-8 h-8 animate-spin text-(--red)" />
                    <p className="text-[11px] font-bold uppercase tracking-widest text-black/30">
                        Загрузка итоговых…
                    </p>
                </div>
            </div>
        );
    }

    if (!Object.keys(periodGradesMap).length) {
        return (
            <div className="flex h-60 items-center justify-center">
                <div className="glass-card rounded-[28px] p-10 text-center">
                    <p className="font-serif text-xl text-(--navy)">Нет данных об итоговых оценках</p>
                </div>
            </div>
        );
    }

    return (
        <>
            {/* Статистика */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
                <StatCard
                    icon={TrendingUp}
                    label="Средний балл"
                    value={overallAvg ? parseFloat(overallAvg.toFixed(2)).toString() : "—"}
                    sub="за все четверти"
                    accent="var(--red)"
                    delay="anim-delay-1"
                />
                <StatCard
                    icon={Star}
                    label="Пятёрок"
                    value={fivesCount.toString()}
                    sub={`из ${allPeriodGrades.length} оценок`}
                    accent="var(--gold)"
                    delay="anim-delay-2"
                />
                <StatCard
                    icon={BookOpen}
                    label="Предметов"
                    value={subjects.length.toString()}
                    sub="в этом году"
                    accent="var(--navy)"
                    delay="anim-delay-3"
                />
                <StatCard
                    icon={BarChart2}
                    label="Четвертей"
                    value={sortedPeriods.length.toString()}
                    sub="всего в году"
                    accent="var(--green)"
                    delay="anim-delay-4"
                />
            </div>

            {/* Таблица */}
            <div className="glass-card rounded-[28px] overflow-hidden anim-in anim-delay-5">
                <div className="flex items-center justify-between px-7 pt-6 pb-4 border-b border-black/5">
                    <Chip className="border-(--navy)/20 text-(--navy) bg-(--navy-light)/30">
                        Итоги по четвертям
                    </Chip>
                    <span className="text-[10px] font-bold text-black/20 uppercase tracking-widest">
                        {schoolYear || "—"}
                    </span>
                </div>

                <ScrollArea className="w-full whitespace-nowrap">
                    <Table className="border-collapse min-w-full">
                        <TableHeader>
                            <TableRow className="hover:bg-transparent border-black/5 bg-black/1.5">
                                <TableHead className="w-50 pl-7 h-12 sticky left-0 bg-white/70 backdrop-blur-sm z-30 border-r border-black/5 font-extrabold text-[9px] uppercase tracking-[0.22em] text-black/30">
                                    Предмет
                                </TableHead>

                                {sortedPeriods.map((p) => (
                                    <TableHead
                                        key={p.id}
                                        className="text-center min-w-27.5 px-2 font-bold text-[10px] text-black/25 border-r border-black/4"
                                    >
                                        <div className="font-extrabold text-[11px] text-(--navy)/60">{p.name}</div>
                                    </TableHead>
                                ))}

                                <TableHead className="w-25 text-center font-extrabold text-[9px] uppercase tracking-[0.18em] text-(--navy)/80 bg-black/2">
                                    Среднее
                                </TableHead>

                                <TableHead className="w-25 text-center font-extrabold text-[9px] uppercase tracking-[0.18em] text-(--navy)/80 bg-black/2">
                                    Годовая
                                </TableHead>
                            </TableRow>
                        </TableHeader>

                        <TableBody>
                            {subjects.map((subjectName) => {
                                // Оценки по предмету — список PeriodGradeStudentResponse
                                const gradesForSubject = periodGradesMap[subjectName];

                                // academicPeriodId → оценка
                                const gradeByPeriodId = new Map(
                                    gradesForSubject.map((g) => [g.academicPeriodId, g])
                                );

                                const subAvg = gradesForSubject.length
                                    ? gradesForSubject.reduce((sum, g) => sum + g.value, 0) / gradesForSubject.length
                                    : 0;

                                return (
                                    <TableRow
                                        key={subjectName}
                                        className="border-black/4 transition-colors group h-14.5 hover:bg-black/1.5"
                                    >
                                        <TableCell className="pl-7 py-0 font-bold text-[13px] text-(--navy) sticky left-0 bg-white/60 backdrop-blur-sm z-20 border-r border-black/5 group-hover:bg-amber-50/50 transition-colors">
                                            {subjectName}
                                        </TableCell>

                                        {sortedPeriods.map((period) => {
                                            const grade = gradeByPeriodId.get(period.id);
                                            return (
                                                <TableCell
                                                    key={period.id}
                                                    className="p-0 text-center border-r border-black/3 min-w-27.5"
                                                >
                                                    <div className="flex items-center justify-center h-14.5 px-1">
                                                        <GradeBadge grade={grade?.value} size="md" />
                                                    </div>
                                                </TableCell>
                                            );
                                        })}

                                        {/* Средний балл по выставленным четвертным */}
                                        <TableCell className="p-0 text-center border-r border-black/3 min-w-27.5">
                                            <div className="flex items-center justify-center h-14.5">
                                                <span className={`font-serif text-[20px] font-black ${avgColor(subAvg)}`}>
                                                    {subAvg ? parseFloat(subAvg.toFixed(1)).toString() : "—"}
                                                </span>
                                            </div>
                                        </TableCell>

                                        {/* Годовая итоговая оценка */}
                                        <TableCell className="p-0 text-center bg-black/1 backdrop-blur-sm group-hover:bg-amber-50/60 transition-colors">
                                            <div className="flex items-center justify-center h-14.5 px-1">
                                                <GradeBadge grade={getFinalGrade(subjectName)?.value} size="md" />
                                            </div>
                                        </TableCell>
                                    </TableRow>
                                );
                            })}
                        </TableBody>
                    </Table>
                    <ScrollBar orientation="horizontal" className="h-2 mx-2 mb-2 rounded-full bg-black/3" />
                </ScrollArea>
            </div>
        </>
    );
}