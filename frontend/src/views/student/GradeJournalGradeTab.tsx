import Chip from "@/components/student/chip";
import { calculateWeightedAvg, avgColor, formatAvg, formatDate } from "@/components/student/grades-page/grades-page-helper";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";
import { TableHeader, TableRow, TableHead, TableBody, TableCell, Table } from "@/components/ui/table";
import React from "react";
import GradeBadge from "@/components/student/grades-page/grade-badge";
import StatCard from "@/components/student/grades-page/stat-card";
import { Award, BookOpen, Star, TrendingUp } from "lucide-react";
import { useGradesLessonsByStudentId } from "@/hooks/use-grade";

export interface GradeJournalGradeTabProps {
    studentId: number;
    academicPeriodId: number;
}

// Отдельный компонент для таблицы — принимает уже готовые данные
function GradeTable({ response, filteredDates }: {
    response: NonNullable<ReturnType<typeof useGradesLessonsByStudentId>['data']>;
    filteredDates: string[]
}) {
    return (
        <div className="glass-card rounded-[28px] overflow-hidden anim-in anim-delay-5">
            <div className="flex items-center justify-between px-7 pt-6 pb-4 border-b border-black/[0.05]">
                <Chip className="border-[var(--navy)]/20 text-[var(--navy)] bg-[var(--navy-light)]/30">
                    Электронный журнал
                </Chip>
                <span className="text-[10px] font-bold text-black/20 uppercase tracking-widest">
                    {response.academicPeriod.academicYear.name}
                </span>
            </div>

            <ScrollArea className="w-full whitespace-nowrap">
                <Table className="border-collapse min-w-full">
                    <TableHeader>
                        <TableRow className="hover:bg-transparent border-black/[0.05] bg-black/[0.015]">
                            <TableHead className="w-[200px] pl-7 h-12 sticky left-0 bg-white/70 backdrop-blur-sm z-30 border-r border-black/[0.05] font-extrabold text-[9px] uppercase tracking-[0.22em] text-black/30">
                                Предмет
                            </TableHead>
                            {filteredDates.map((date) => (
                                <TableHead
                                    key={date}
                                    className="text-center min-w-[76px] px-1 font-bold text-[10px] text-black/25 border-r border-black/[0.04]"
                                >
                                    {formatDate(date)}
                                </TableHead>
                            ))}
                            <TableHead className="w-[88px] sticky right-0 z-30 bg-white/80 backdrop-blur-sm border-l border-black/[0.07] text-center font-extrabold text-[9px] uppercase tracking-[0.18em] text-[var(--red)]">
                                Средний
                            </TableHead>
                        </TableRow>
                    </TableHeader>

                    <TableBody>
                        {response.subjects.map((sub) => {
                            const weightedAvg = calculateWeightedAvg(sub.grades);

                            return (
                                <TableRow
                                    key={sub.subject}
                                    className="border-black/[0.04] transition-colors group h-[58px] hover:bg-black/[0.015]"
                                >
                                    <TableCell className="pl-7 py-0 font-bold text-[13px] text-[var(--navy)] sticky left-0 bg-white/60 backdrop-blur-sm z-20 border-r border-black/[0.05] group-hover:bg-amber-50/50 transition-colors">
                                        {sub.subject}
                                    </TableCell>

                                    {filteredDates.map((date) => {
                                        const dayGrades = sub.grades.filter((g) => g.date === date);
                                        return (
                                            <TableCell
                                                key={date}
                                                className="p-0 text-center border-r border-black/[0.03] min-w-[76px]"
                                            >
                                                <div className="flex items-center justify-center h-[58px] px-1">
                                                    {dayGrades.length > 0 ? (
                                                        <div className={`flex items-center justify-center ${dayGrades.length > 1 ? "gap-0.5" : ""}`}>
                                                            {dayGrades.map((g, idx) => (
                                                                <React.Fragment key={g.gradeId}>
                                                                    <GradeBadge
                                                                        grade={g.value}
                                                                        size={dayGrades.length > 1 ? "sm" : "md"}
                                                                    />
                                                                    {dayGrades.length > 1 && idx === 0 && (
                                                                        <span className="text-black/15 font-light mx-[-1px] select-none">
                                                                            /
                                                                        </span>
                                                                    )}
                                                                </React.Fragment>
                                                            ))}
                                                        </div>
                                                    ) : (
                                                        <GradeBadge />
                                                    )}
                                                </div>
                                            </TableCell>
                                        );
                                    })}

                                    <TableCell className="sticky right-0 z-20 border-l border-black/[0.07] p-0 bg-white/70 backdrop-blur-sm group-hover:bg-amber-50/60 transition-colors">
                                        <div className="flex items-center justify-center h-[58px]">
                                            <span className={`font-serif text-[20px] font-black ${avgColor(weightedAvg)}`}>
                                                {formatAvg(weightedAvg)}
                                            </span>
                                        </div>
                                    </TableCell>
                                </TableRow>
                            );
                        })}
                    </TableBody>
                </Table>
                <ScrollBar orientation="horizontal" className="h-2 mx-2 mb-2 rounded-full bg-black/[0.03]" />
            </ScrollArea>
        </div>
    );
}

// Основной компонент с логикой загрузки
export default function GradeJournalGradeTab({ studentId, academicPeriodId }: GradeJournalGradeTabProps) {
    const { data: response, isLoading, isError } = useGradesLessonsByStudentId(studentId, academicPeriodId);

    const today = new Date();
    const localTodayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;

    const filteredDates = response?.dates.filter((date) => {
        if (date <= localTodayStr) return true;
        return response.subjects.some((sub) => sub.grades.some((g) => g.date === date));
    }) ?? [];

    const allCurrentGrades = response?.subjects.flatMap((s) => s.grades) ?? [];
    const overallAvg = allCurrentGrades.length
        ? allCurrentGrades.reduce((a, g) => a + g.value, 0) / allCurrentGrades.length
        : 0;
    const excellentCount = allCurrentGrades.filter((g) => g.value === 5).length;

    if (isLoading) {
        return (
            <div className="glass-card rounded-[28px] overflow-hidden anim-in anim-delay-5 p-8 text-center">
                <p className="text-black/40">Загружаем данные...</p>
            </div>
        );
    }

    if (isError || !response) {
        return (
            <div className="glass-card rounded-[28px] overflow-hidden anim-in anim-delay-5 p-8 text-center">
                <p className="text-red-500">Ошибка при загрузке данных</p>
            </div>
        );
    }
    
    return (
        <>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
                <StatCard
                    icon={TrendingUp}
                    label="Средний балл"
                    value={overallAvg ? parseFloat(overallAvg.toFixed(2)).toString() : "—"}
                    sub="за четверть"
                    accent="var(--red)"
                    delay="anim-delay-1"
                />
                <StatCard
                    icon={Star}
                    label="Пятёрок"
                    value={excellentCount.toString()}
                    sub={`из ${allCurrentGrades.length} оценок`}
                    accent="var(--gold)"
                    delay="anim-delay-2"
                />
                <StatCard
                    icon={BookOpen}
                    label="Предметов"
                    value={response.subjects.length.toString()}
                    sub="в этой четверти"
                    accent="var(--navy)"
                    delay="anim-delay-3"
                />
                <StatCard
                    icon={Award}
                    label="Дней занятий"
                    value={filteredDates.length.toString()}
                    sub="в журнале"
                    accent="var(--green)"
                    delay="anim-delay-4"
                />
            </div>

            <GradeTable response={response} filteredDates={filteredDates} />
        </>
    );
}