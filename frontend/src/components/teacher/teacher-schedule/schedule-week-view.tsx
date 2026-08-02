import type { TeacherScheduleItemPeriod } from "@/services/schedule-service";
import { addDays, getDayOfWeekKey, isSameDay, WEEK_DAY_LABELS } from "./schedule-date";
import { getClassColor } from "./schedule-color";
import { LESSON_TIMES } from "@/constants/component-constants";

interface ScheduleWeekViewProps {
    weekStart: Date;
    period: TeacherScheduleItemPeriod | undefined;
    isLoading: boolean;
    selectedDate: Date;
    onSelectDate: (date: Date) => void;
}

const MIN_ROWS = 6;

export default function ScheduleWeekView({
    weekStart,
    period,
    isLoading,
    selectedDate,
    onSelectDate,
}: ScheduleWeekViewProps) {
    const today = new Date();

    const days = WEEK_DAY_LABELS.map((label, index) => {
        const date = addDays(weekStart, index);
        const lessons = period?.[getDayOfWeekKey(date)] ?? [];
        return { label, date, lessons };
    });

    const maxLessonNumber = Math.max(
        MIN_ROWS,
        ...days.flatMap((d) => d.lessons.map((l) => l.scheduleLesson.lessonNumber))
    );
    const rowNumbers = Array.from({ length: maxLessonNumber }, (_, i) => i + 1);

    if (isLoading) {
        return (
            <div className="glass-card rounded-[32px] p-6 backdrop-blur-md min-h-125 flex items-center justify-center">
                <div className="animate-pulse text-black/30 font-medium">Загрузка расписания...</div>
            </div>
        );
    }

    return (
        <div className="glass-card rounded-[32px] p-4 sm:p-6 backdrop-blur-md overflow-x-auto">
            <div className="grid min-w-[760px]" style={{ gridTemplateColumns: "56px repeat(6, minmax(0, 1fr))" }}>
                {/* header row */}
                <div className="contents">
                    <div className="pb-3" />
                    {days.map(({ label, date }) => {
                        const isToday = isSameDay(date, today);
                        const isSelected = isSameDay(date, selectedDate);

                        return (
                            <button
                                key={label}
                                type="button"
                                onClick={() => onSelectDate(date)}
                                className={`pb-3 px-2 flex flex-col items-center gap-1 rounded-t-2xl transition-colors ${isSelected ? "bg-(--red-light)/25" : "hover:bg-black/[0.02]"
                                    }`}
                            >
                                <span
                                    className={`text-[11px] font-bold uppercase tracking-wide ${isToday ? "text-(--red)" : "text-black/35"
                                        }`}
                                >
                                    {label}
                                </span>
                                <span
                                    className={`w-7 h-7 flex items-center justify-center rounded-full font-serif font-black text-[15px] ${isToday ? "bg-(--red) text-white" : "text-(--navy)"
                                        }`}
                                >
                                    {date.getDate()}
                                </span>
                            </button>
                        );
                    })}
                </div>

                {/* lesson rows */}
                {rowNumbers.map((num) => (
                    <div key={num} className="contents">
                        <div className="flex flex-col items-center justify-start pt-2.5 gap-1 w-14">
                            <span className="w-7 h-7 flex items-center justify-center rounded-full bg-black/5 text-black/60 text-[13px] font-bold">
                                {num}
                            </span>
                            <span className="w-full text-center text-[10.5px] text-black/40 font-medium tabular-nums whitespace-nowrap">
                                {LESSON_TIMES[num]}
                            </span>
                        </div>

                        {days.map(({ label, date, lessons }) => {
                            const isToday = isSameDay(date, today);
                            const cellLessons = lessons
                                .filter((l) => l.scheduleLesson.lessonNumber === num)
                                .sort((a, b) => a.schoolClass.name.localeCompare(b.schoolClass.name));

                            return (
                                <div
                                    key={`${label}-${num}`}
                                    className={`min-h-14 px-1 py-1 border-t border-black/[0.05] ${isToday ? "bg-(--red-light)/10" : ""
                                        }`}
                                >
                                    {cellLessons.length === 0 ? (
                                        <div className="w-full h-full min-h-10 rounded-xl border border-dashed border-black/[0.06]" />
                                    ) : (
                                        <div className="flex flex-col gap-1">
                                            {cellLessons.map((lesson) => {
                                                const color = getClassColor(lesson.schoolClass.id);

                                                return (
                                                    <div
                                                        key={lesson.lessonInstance.id}
                                                        className={`rounded-xl border px-2.5 py-1.5 ${color.bg} ${color.border}`}
                                                    >
                                                        <p className={`text-[12px] font-bold truncate ${color.text}`}>
                                                            {lesson.subject.name}
                                                        </p>
                                                        <p className="text-[10.5px] text-black/45 truncate">
                                                            {lesson.schoolClass.name} · каб. {lesson.scheduleLesson.classRoom}
                                                        </p>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                ))}
            </div>
        </div>
    );
}