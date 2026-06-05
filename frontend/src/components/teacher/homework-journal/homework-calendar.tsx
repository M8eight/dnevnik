import { WEEKDAYS } from "@/constants/component-constants";
import { toDateKey } from "@/helpers/page-helpers";
import { cn } from "@/lib/utils";
import type { HomeworkResponse } from "@/services/homework-service";
import type { lessonInstance } from "@/services/lesson-instance-service";
import { startOfMonth, endOfMonth, eachDayOfInterval, getDay, format, isSameDay, isToday } from "date-fns";
import { ru } from "date-fns/locale";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useMemo } from "react";

export default function HomeworkCalendar({
    homeworks,
    lessonInstances,
    selectedDate,
    onSelectDate,
    currentMonth,
    onMonthChange,
}: {
    homeworks: HomeworkResponse[];
    lessonInstances: lessonInstance[];
    selectedDate: Date | null;
    onSelectDate: (d: Date) => void;
    currentMonth: Date;
    onMonthChange: (d: Date) => void;
}) {
    const hwByDay = useMemo(() => {
        const map = new Map<string, HomeworkResponse[]>();
        homeworks.forEach((hw) => {
            if (!hw.lessonInstance?.lessonDate) return;
            const key = hw.lessonInstance.lessonDate.slice(0, 10);
            if (!map.has(key)) map.set(key, []);
            map.get(key)!.push(hw);
        });
        return map;
    }, [homeworks]);

    const lessonDays = useMemo(
        () => new Set(lessonInstances.map((i) => i.lessonDate.slice(0, 10))),
        [lessonInstances]
    );

    const days = useMemo(() => {
        const start = startOfMonth(currentMonth);
        const end = endOfMonth(currentMonth);
        const monthDays = eachDayOfInterval({ start, end });

        // Monday-based offset
        const startDow = getDay(start);
        const offset = startDow === 0 ? 6 : startDow - 1;
        const prefixDays: (Date | null)[] = Array(offset).fill(null);

        const allCells = [...prefixDays, ...monthDays];
        // pad to full weeks
        while (allCells.length % 7 !== 0) allCells.push(null);
        return allCells;
    }, [currentMonth]);

    const prevMonth = () => {
        const d = new Date(currentMonth);
        d.setMonth(d.getMonth() - 1);
        onMonthChange(d);
    };
    const nextMonth = () => {
        const d = new Date(currentMonth);
        d.setMonth(d.getMonth() + 1);
        onMonthChange(d);
    };

    return (
        <div className="glass-card rounded-[32px] p-6 h-full flex flex-col">
            {/* Month nav */}
            <div className="flex items-center justify-between mb-6">
                <button
                    onClick={prevMonth}
                    className="w-9 h-9 rounded-2xl bg-white/40 hover:bg-white/60 border border-black/5 flex items-center justify-center transition-all active:scale-95"
                >
                    <ChevronLeft className="w-4 h-4 text-[var(--navy)]" />
                </button>
                <h2 className="font-serif font-black text-lg text-[var(--navy)] tracking-tight capitalize">
                    {format(currentMonth, "LLLL yyyy", { locale: ru })}
                </h2>
                <button
                    onClick={nextMonth}
                    className="w-9 h-9 rounded-2xl bg-white/40 hover:bg-white/60 border border-black/5 flex items-center justify-center transition-all active:scale-95"
                >
                    <ChevronRight className="w-4 h-4 text-[var(--navy)]" />
                </button>
            </div>

            {/* Weekday headers */}
            <div className="grid grid-cols-7 mb-2">
                {WEEKDAYS.map((w) => (
                    <div
                        key={w}
                        className="text-center text-[10px] font-bold tracking-widest uppercase text-black/25 pb-2"
                    >
                        {w}
                    </div>
                ))}
            </div>

            {/* Day cells */}
            <div className="grid grid-cols-7 gap-1.5 flex-1">
                {days.map((day, idx) => {
                    if (!day) {
                        return <div key={`empty-${idx}`} />;
                    }
                    const key = toDateKey(day);
                    const hws = hwByDay.get(key) ?? [];
                    const hasLesson = lessonDays.has(key);
                    const isSelected =
                        selectedDate && isSameDay(day, selectedDate);
                    const isTodayCell = isToday(day);

                    return (
                        <button
                            key={key}
                            onClick={() => onSelectDate(day)}
                            className={cn(
                                "relative flex flex-col items-start rounded-2xl p-2 min-h-[72px] transition-all text-left border",
                                isSelected
                                    ? "bg-[var(--ink-dim)] border-transparent shadow-lg shadow-[var(--navy)]/20"
                                    : hws.length > 0
                                    ? "bg-white/50 border-black/5 hover:bg-white/70 hover:border-black/10"
                                    : "bg-white/20 border-transparent hover:bg-white/35 hover:border-black/5"
                            )}
                        >
                            {/* Day number */}
                            <span
                                className={cn(
                                    "text-xs font-bold mb-1.5 w-5 h-5 flex items-center justify-center rounded-full",
                                    isSelected
                                        ? "bg-white/20 text-white"
                                        : isTodayCell
                                        ? "bg-[var(--red)] text-white"
                                        : "text-[var(--navy)]"
                                )}
                            >
                                {format(day, "d")}
                            </span>

                            {/* Lesson dot */}
                            {hasLesson && hws.length === 0 && (
                                <div
                                    className={cn(
                                        "w-1.5 h-1.5 rounded-full mt-auto",
                                        isSelected ? "bg-white/40" : "bg-black/15"
                                    )}
                                />
                            )}

                            {/* HW chips */}
                            {hws.slice(0, 2).map((hw, i) => (
                                <div
                                    key={hw.id}
                                    className={cn(
                                        "w-full text-[9px] font-bold rounded-lg px-1.5 py-0.5 truncate leading-tight",
                                        isSelected
                                            ? i === 0
                                                ? "bg-white/25 text-white"
                                                : "bg-white/15 text-white/70"
                                            : i === 0
                                            ? "bg-[var(--red-light)] text-[var(--red)]"
                                            : "bg-blue-50 text-blue-600"
                                    )}
                                >
                                    Задание #{hw.id}
                                </div>
                            ))}
                            {hws.length > 2 && (
                                <div
                                    className={cn(
                                        "text-[9px] font-bold px-1",
                                        isSelected ? "text-white/50" : "text-black/30"
                                    )}
                                >
                                    +{hws.length - 2}
                                </div>
                            )}
                        </button>
                    );
                })}
            </div>
        </div>
    );
}