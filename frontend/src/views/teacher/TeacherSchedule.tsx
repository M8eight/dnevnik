import { useMemo, useState } from "react";
import { CalendarDays, ChevronLeft, ChevronRight, GraduationCap, LayoutGrid, List } from "lucide-react";
import TeacherNavbar from "@/components/layout/navbars/TeacherNavbar";
import { useTeacherScheduleDate, useTeacherSchedulePeriod } from "@/hooks/use-schedule";
import { addDays, formatWeekRangeLabel, getWeekRange, toISODate } from "@/components/teacher/teacher-schedule/schedule-date";
import ScheduleWeekView from "@/components/teacher/teacher-schedule/schedule-week-view";
import ScheduleDayView from "@/components/teacher/teacher-schedule/schedule-day-view";
import { useByTeacherId } from "@/hooks/use-school-class";
import { useNavigate } from "react-router-dom";

function Skeleton({ className }: { className?: string }) {
    return <div className={`animate-pulse rounded-xl bg-black/8 ${className}`} />;
}

function ClassCardSkeleton() {
    return (
        <div className="glass-card rounded-[24px] p-5 border-none shadow-lg backdrop-blur-md flex items-center gap-4">
            <Skeleton className="w-14 h-14 rounded-[16px] shrink-0" />
            <div className="flex-1 space-y-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-3 w-32" />
            </div>
        </div>
    );
}

function ClassCard({ schoolClass }: { schoolClass: { id: number; name: string; academicYear: { name: string; closed: boolean } } }) {
    const navigate = useNavigate();
    const { id, name, academicYear } = schoolClass;

    return (
        <button
            onClick={() => navigate(`/teacher/school-class/${id}`)}
            className="glass-card rounded-[24px] p-5 border-none shadow-lg backdrop-blur-md flex items-center gap-4 text-left w-full transition-transform hover:-translate-y-0.5"
        >
            <div className="w-14 h-14 rounded-[16px] bg-(--red-light)/50 flex items-center justify-center shrink-0">
                <span className="font-serif font-black text-lg text-(--red)">{name}</span>
            </div>
            <div className="flex-1 min-w-0">
                <p className="font-bold text-sm text-(--navy) leading-tight truncate">Класс {name}</p>
                <div className="flex items-center gap-1.5 mt-1">
                    {academicYear.closed && <span className="w-1.5 h-1.5 rounded-full bg-green-500" />}
                    <p className="text-xs text-black/40 font-medium">{academicYear.name}</p>
                </div>
            </div>
            <ChevronRight className="w-4 h-4 text-black/25 shrink-0" />
        </button>
    );
}

type ViewMode = "week" | "day";

export default function TeacherSchedule() {

    const { data: teacherData, isLoading } = useByTeacherId();

    const [viewMode, setViewMode] = useState<ViewMode>("week");
    const [anchorDate, setAnchorDate] = useState<Date>(() => new Date());

    const { start: weekStart, end: weekEnd } = useMemo(() => getWeekRange(anchorDate), [anchorDate]);

    const { data: periodData, isLoading: periodLoading } = useTeacherSchedulePeriod(
        toISODate(weekStart),
        toISODate(weekEnd)
    );

    const { data: dayData, isLoading: dayLoading } = useTeacherScheduleDate(toISODate(anchorDate));

    const handlePrev = () => setAnchorDate((prev) => addDays(prev, viewMode === "week" ? -7 : -1));
    const handleNext = () => setAnchorDate((prev) => addDays(prev, viewMode === "week" ? 7 : 1));
    const handleToday = () => setAnchorDate(new Date());

    const rangeLabel =
        viewMode === "week"
            ? formatWeekRangeLabel(weekStart, weekEnd)
            : anchorDate.toLocaleDateString("ru-RU", { day: "numeric", month: "long", year: "numeric" });

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">

            <TeacherNavbar />

            <div className="max-w-350 mx-auto mb-6">

                <div className="max-w-350 mx-auto mb-6">
                    {isLoading ? (
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                            {Array.from({ length: 6 }).map((_, i) => (
                                <ClassCardSkeleton key={i} />
                            ))}
                        </div>
                    ) : !teacherData || teacherData.length === 0 ? (
                        <div className="glass-card rounded-[28px] p-10 border-none shadow-lg backdrop-blur-md flex flex-col items-center text-center">
                            <div className="w-14 h-14 rounded-[16px] bg-(--red-light)/50 flex items-center justify-center mb-4">
                                <GraduationCap className="w-6 h-6 text-(--red)" />
                            </div>
                            <p className="font-bold text-sm text-(--navy) mb-1">Нет привязанных классов</p>
                            <p className="text-xs text-black/40 font-medium">
                                Классы появятся здесь
                            </p>
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                            {teacherData.map((sc) => (
                                <ClassCard key={sc.id} schoolClass={sc} />
                            ))}
                        </div>
                    )}
                </div>

                <div className="glass-card rounded-[24px] p-5 flex flex-col lg:flex-row lg:items-center justify-between gap-5 border-none shadow-lg backdrop-blur-md">
                    <div className="flex items-center gap-4">
                        <div className="hidden sm:flex w-12 h-12 rounded-[18px] bg-(--red-light)/60 items-center justify-center ring-1 ring-(--red)/10">
                            <CalendarDays className="w-6 h-6 text-(--red)" />
                        </div>
                        <div>
                            <h1 className="font-serif font-black text-2xl lg:text-3xl text-(--navy) tracking-tight">
                                Расписание
                            </h1>
                            <p className="text-sm text-black/40 mt-0.5 capitalize">{rangeLabel}</p>
                        </div>
                    </div>

                    <div className="flex flex-wrap items-center gap-3">
                        <div className="glass-pill rounded-2xl p-1 flex items-center gap-1">
                            <button
                                type="button"
                                onClick={() => setViewMode("week")}
                                className={`flex items-center gap-1.5 px-4 h-9 rounded-xl text-[13px] font-bold transition-colors ${viewMode === "week"
                                        ? "bg-(--red) text-white shadow-sm"
                                        : "text-(--navy)/60 hover:text-(--navy)"
                                    }`}
                            >
                                <LayoutGrid className="w-3.5 h-3.5" />
                                Неделя
                            </button>
                            <button
                                type="button"
                                onClick={() => setViewMode("day")}
                                className={`flex items-center gap-1.5 px-4 h-9 rounded-xl text-[13px] font-bold transition-colors ${viewMode === "day"
                                        ? "bg-(--red) text-white shadow-sm"
                                        : "text-(--navy)/60 hover:text-(--navy)"
                                    }`}
                            >
                                <List className="w-3.5 h-3.5" />
                                День
                            </button>
                        </div>

                        <div className="flex items-center gap-1.5">
                            <button
                                type="button"
                                onClick={handlePrev}
                                aria-label="Предыдущий период"
                                className="glass-pill w-9 h-9 rounded-xl flex items-center justify-center text-(--navy) hover:text-(--red) transition-colors"
                            >
                                <ChevronLeft className="w-4 h-4" />
                            </button>
                            <button
                                type="button"
                                onClick={handleToday}
                                className="glass-pill h-9 px-4 rounded-xl text-[13px] font-bold text-(--navy) hover:text-(--red) transition-colors"
                            >
                                Сегодня
                            </button>
                            <button
                                type="button"
                                onClick={handleNext}
                                aria-label="Следующий период"
                                className="glass-pill w-9 h-9 rounded-xl flex items-center justify-center text-(--navy) hover:text-(--red) transition-colors"
                            >
                                <ChevronRight className="w-4 h-4" />
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div className="max-w-350 mx-auto">
                {viewMode === "week" ? (
                    <ScheduleWeekView
                        weekStart={weekStart}
                        period={periodData}
                        isLoading={periodLoading}
                        selectedDate={anchorDate}
                        onSelectDate={(date) => {
                            setAnchorDate(date);
                            setViewMode("day");
                        }}
                    />
                ) : (
                    <ScheduleDayView date={anchorDate} lessons={dayData} isLoading={dayLoading} />
                )}
            </div>
        </div>
    );
}