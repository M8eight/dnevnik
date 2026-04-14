import React from "react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useDiaryLessonsByStudentIdAndDateRange } from "@/hooks/use-schedule";
import type { RootState } from "@/store";
import { useDispatch, useSelector } from "react-redux";
import { addDays } from "date-fns/addDays";
import { format } from "date-fns/format";
import { ru } from 'date-fns/locale';
import { nextWeek, prevWeek } from "@/store/slices/scheduleSlice";


const RUSSIAN_DAYS: Record<string, string> = {
    MONDAY: "Понедельник",
    TUESDAY: "Вторник",
    WEDNESDAY: "Среда",
    THURSDAY: "Четверг",
    FRIDAY: "Пятница",
    SATURDAY: "Суббота",
    SUNDAY: "Воскресенье",
};

// Превращает "2026-04-06" в "6 апреля"
const formatDateLabel = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString("ru-RU", { day: "numeric", month: "long" });
};

// Маппинг статусов посещаемости с сервера на твои "чипы"
const mapAttendanceStatus = (status?: string) => {
    if (status === "ABSENT") return "Н";
    if (status === "EXCUSED") return "ОП";
    if (status === "SICK") return "Б";
    return "";
};

function Chip({ children, className = "" }: { children: React.ReactNode, className?: string }) {
    return (
        <Badge
            variant="outline"
            className={`text-[11px] p-2 font-extrabold tracking-[0.18em] uppercase mb-3 rounded-md ${className}`}
        >
            {children}
        </Badge>
    );
}

function AttendanceBadge({ status }: { status: string }) {
    if (!status) return null;

    const styles: Record<string, string> = {
        "Н": "bg-[var(--red-light)] text-[var(--red)] border-[var(--red)]",
        "ОП": "bg-[var(--gold-light)] text-[var(--gold)] border-[var(--gold)]",
        "Б": "bg-[var(--green-light)] text-[var(--green)] border-[var(--green)]",
    }

    return (
        <span className={`w-[38px] h-[38px] rounded-[10px] flex items-center justify-center font-serif text-xl font-bold flex-shrink-0 border ${styles[status] ?? "bg-gray-100 text-gray-600"}`}>
            {status}
        </span>
    )
}

function GradeBadge({ grade }: { grade: number | null }) {
    if (!grade) return null;

    const styles: Record<number, string> = {
        5: "bg-[var(--green-light)] text-[var(--green)]",
        4: "bg-[var(--gold-light)]   text-[var(--gold)]",
        3: "bg-[var(--red-light)]    text-[var(--red)]",
        2: "bg-[var(--red-light)]    text-[var(--red)]",
    };
    return (
        <span className={`w-[38px] h-[38px] rounded-[10px] flex items-center justify-center font-serif text-xl font-bold flex-shrink-0 ${styles[grade] ?? "bg-gray-100 text-gray-600"}`}>
            {grade}
        </span>
    );
}

// --- Основной компонент ---

export default function Diary() {
    const dispatch = useDispatch();
    const currentWeekStartISO = useSelector((state: RootState) => state.schedule.currentWeekStart);
    const currentWeekStart = new Date(currentWeekStartISO);
    const startDate = format(currentWeekStart, 'yyyy-MM-dd');
    const endDate = format(addDays(currentWeekStart, 6), 'yyyy-MM-dd');
    const { data, isLoading } = useDiaryLessonsByStudentIdAndDateRange(1, startDate, endDate);
    const weekEnd = addDays(currentWeekStart, 6);
    const startDay = format(currentWeekStart, 'dd');
    const endDayWithMonth = format(weekEnd, 'dd MMM', { locale: ru });
    const fullMonthYear = format(currentWeekStart, 'LLLL yyyy', { locale: ru });
    const capitalizedMonth = fullMonthYear.charAt(0).toUpperCase() + fullMonthYear.slice(1);

    // Сортируем ключи (даты), чтобы дни шли по порядку
    const sortedDays = data ? Object.entries(data).sort((a, b) => a[0].localeCompare(b[0])) : [];

    return (
        <div className="relative z-10 min-h-screen px-8 pt-24 pb-10">
            <header className="flex items-end justify-between mb-10 pb-6 border-b border-black/10 max-w-7xl mx-auto">
                <div className="border-l-4 border-[var(--red)] pl-5">
                    <p className="text-[10px] font-extrabold tracking-[0.22em] text-[var(--red)] uppercase mb-1">
                        ✦ Академический год 25/26
                    </p>
                    <h1 className="font-serif font-black text-[clamp(2rem,4.5vw,3.2rem)] text-[var(--navy)] leading-none">
                        Учебный <em className="not-italic text-[var(--red)]">дневник</em>
                    </h1>
                </div>

                <div className="flex items-center gap-4">
                    <Button onClick={() => dispatch(prevWeek())} variant="outline" size="icon" className="rounded-md border-black/10 hover:bg-[var(--red-light)] hover:text-[var(--red)]">
                        <ChevronLeft className="h-4 w-4" />
                    </Button>

                    <div className="text-right mr-4">
                        {/* Верхняя строка: Месяц и Год (например, "Апрель 2026") */}
                        <p className="text-[10px] font-bold uppercase text-[var(--ink-faint)] tracking-widest">
                            {capitalizedMonth}
                        </p>

                        {/* Нижняя строка: Диапазон (например, "06 — 12 апр") */}
                        <p className="font-serif text-lg font-bold text-[var(--ink)]">
                            {startDay} — {endDayWithMonth}
                        </p>
                    </div>

                    <Button onClick={() => dispatch(nextWeek())} variant="outline" size="icon" className="rounded-md border-black/10 hover:bg-[var(--red-light)] hover:text-[var(--red)]">
                        <ChevronRight className="h-4 w-4" />
                    </Button>
                </div>
            </header>

            <main className="grid grid-cols-12 gap-6 max-w-7xl mx-auto">
                {isLoading ? (
                    Array.from({ length: 6 }).map((_, i) => (
                        <Card key={i} className="col-span-12 md:col-span-6 xl:col-span-6 bg-[var(--bg-card)] border-black/10">
                            <CardContent className="p-7"><Skeleton className="h-40 w-full" /></CardContent>
                        </Card>
                    ))
                ) : (
                    sortedDays.map(([dateKey, lessons]) => (
                        <Card
                            key={dateKey}
                            className="col-span-12 md:col-span-6 xl:col-span-6 bg-[var(--bg-card)] border-black/10 hover:-translate-y-1 hover:shadow-xl transition-all duration-200"
                        >
                            <CardContent className="p-6">
                                <div className="flex justify-between items-start mb-6">
                                    <Chip className="border-[var(--red)] text-[var(--red)] bg-[var(--red-light)]">
                                        {/* Берем день недели из первого урока дня */}
                                        {RUSSIAN_DAYS[lessons[0]?.dayOfWeek] || "День"}
                                    </Chip>
                                    <p className="text-[10px] font-bold text-[var(--ink-faint)] uppercase tracking-widest mt-2">
                                        {formatDateLabel(dateKey)}
                                    </p>
                                </div>

                                <div className="divide-y divide-black/[0.07]">
                                    {lessons.map((lesson, idx) => (
                                        <div key={idx} className="py-4 first:pt-0 last:pb-0">
                                            <div className="flex items-start gap-4">
                                                <span className="font-serif text-[1.6rem] font-bold text-[var(--red-light)] leading-none min-w-[28px]">
                                                    {lesson.lessonNumber}
                                                </span>

                                                <div className="flex-1 min-w-0">
                                                    <div className="flex justify-between items-start gap-2">
                                                        <div>
                                                            <p className="font-bold text-sm text-[var(--ink)] leading-tight">
                                                                {lesson.subjectName}
                                                            </p>
                                                            {lesson.homeworks && lesson.homeworks.length > 0 && (
                                                                <p className="text-[14px] text-[var(--ink-dim)] mt-1 italic leading-snug line-clamp-2">
                                                                    {lesson.homeworks[0].text}
                                                                </p>
                                                            )}
                                                        </div>

                                                        <div className="flex items-center gap-3">
                                                            <AttendanceBadge
                                                                status={mapAttendanceStatus(lesson.attendance?.status)}
                                                            />
                                                            {/* Если оценок несколько, можно мапить все, пока берем первую */}
                                                            <GradeBadge
                                                                grade={lesson.grades[0]?.value || null}
                                                            />
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    ))}

                                    {lessons.length === 0 && (
                                        <p className="py-4 text-sm text-[var(--ink-faint)] italic">Занятий не запланировано</p>
                                    )}
                                </div>
                            </CardContent>
                        </Card>
                    ))
                )}
            </main>
        </div>
    );
}
