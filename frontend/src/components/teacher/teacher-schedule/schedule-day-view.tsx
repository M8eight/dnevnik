import { CalendarX2, MapPin, Users } from "lucide-react";
import type { TeacherScheduleItem } from "@/services/schedule-service";
import { getClassColor } from "./schedule-color";

interface ScheduleDayViewProps {
    date: Date;
    lessons: TeacherScheduleItem[] | undefined;
    isLoading: boolean;
}
 
export default function ScheduleDayView({ date, lessons, isLoading }: ScheduleDayViewProps) {
    const sorted = (lessons ?? [])
        .slice()
        .sort((a, b) => a.scheduleLesson.lessonNumber - b.scheduleLesson.lessonNumber);
 
    const dateLabel = date.toLocaleDateString("ru-RU", {
        weekday: "long",
        day: "numeric",
        month: "long",
    });
 
    return (
        <div className="glass-card rounded-[32px] p-6 backdrop-blur-md min-h-125">
            <div className="flex items-center justify-between mb-5">
                <p className="font-serif font-black text-lg text-(--navy) capitalize">{dateLabel}</p>
                {sorted.length > 0 && (
                    <span className="text-[12px] font-bold text-black/35">
                        {sorted.length} {pluralizeLessons(sorted.length)}
                    </span>
                )}
            </div>
 
            {isLoading ? (
                <div className="flex items-center justify-center py-16">
                    <div className="animate-pulse text-black/30 font-medium">Загрузка...</div>
                </div>
            ) : sorted.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-16 gap-3 text-black/30">
                    <CalendarX2 className="w-8 h-8" />
                    <p className="text-sm font-medium">В этот день уроков нет</p>
                </div>
            ) : (
                <div className="flex flex-col gap-2.5">
                    {sorted.map((lesson) => {
                        const color = getClassColor(lesson.schoolClass.id);
                        return (
                            <div
                                key={lesson.lessonInstance.id}
                                className={`flex items-center gap-4 rounded-2xl px-4 py-3 border ${color.bg} ${color.border}`}
                            >
                                <div
                                    className={`shrink-0 w-10 h-10 rounded-2xl bg-white/70 ${color.text} font-black flex items-center justify-center`}
                                >
                                    {lesson.scheduleLesson.lessonNumber}
                                </div>
                                <div className="min-w-0 flex-1">
                                    <p className="font-bold text-(--navy) truncate">{lesson.subject.name}</p>
                                    <div className="flex items-center gap-3 mt-0.5">
                                        <span className="flex items-center gap-1 text-[12.5px] text-black/45">
                                            <Users className="w-3 h-3" />
                                            {lesson.schoolClass.name}
                                        </span>
                                        <span className="flex items-center gap-1 text-[12.5px] text-black/45">
                                            <MapPin className="w-3 h-3" />
                                            {lesson.scheduleLesson.classRoom}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}
 
function pluralizeLessons(count: number): string {
    const mod10 = count % 10;
    const mod100 = count % 100;
    if (mod10 === 1 && mod100 !== 11) return "урок";
    if ([2, 3, 4].includes(mod10) && ![12, 13, 14].includes(mod100)) return "урока";
    return "уроков";
}
 