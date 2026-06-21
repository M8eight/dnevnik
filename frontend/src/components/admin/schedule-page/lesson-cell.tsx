import type { ScheduleLessonDto } from "@/services/schedule-service";
import { MapPin, Trash2 } from "lucide-react";

interface LessonCellProps {
    dayKey: string;
    slotNum: number;
    schedule: ScheduleLessonDto[];
    onAddClick: (day: string, slot: number) => void;
    onCloseClick: (lesson: ScheduleLessonDto) => void;
}

export default function LessonCell({ dayKey, slotNum, schedule, onAddClick, onCloseClick }: LessonCellProps) {
    const activeLesson = schedule.find(
        (s) => s.dayOfWeek === dayKey && s.lessonNumber === slotNum
    );

    if (activeLesson) {
        const validTo = activeLesson.validTo
            ? new Date(activeLesson.validTo).toLocaleDateString("ru-RU", { day: "numeric", month: "short" })
            : null;

        return (
            <div className="group/cell w-full h-full p-2.5 flex flex-col justify-between items-start text-left rounded-[16px] bg-white/50 border border-white/60 hover:border-red-200 hover:bg-red-50/30 transition-all duration-150">
                <div className="w-full cursor-default">
                    <div className="flex items-start justify-between gap-1">
                        <p className="font-black text-(--navy) text-[13px] leading-snug line-clamp-1">
                            {activeLesson.subject.name}
                        </p>
                        {validTo && (
                            <span className="shrink-0 text-[9px] font-black text-orange-400 bg-orange-50 border border-orange-200 px-1.5 py-0.5 rounded-md leading-none mt-0.5">
                                до {validTo}
                            </span>
                        )}
                    </div>
                    <p className="text-[11px] text-black/40 font-semibold mt-0.5 line-clamp-1">
                        {activeLesson.teacher.lastName} {activeLesson.teacher.firstName.charAt(0)}.
                    </p>
                </div>
                <div className="w-full flex items-center justify-between mt-2">
                    <span className="flex items-center gap-1 text-[10px] font-bold text-black/30 bg-black/5 px-2 py-0.5 rounded-lg cursor-default">
                        <MapPin className="w-2.5 h-2.5 text-(--red)" />
                        {activeLesson.classRoom}
                    </span>
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            onCloseClick(activeLesson);
                        }}
                        className="p-1 rounded-md hover:bg-red-100"
                        title="Закрыть урок"
                    >
                        <Trash2 className="w-3.5 h-3.5 text-black/20 group-hover/cell:text-(--red) transition-colors" />
                    </button>
                </div>
            </div>
        );
    }

    return (
        <button
            onClick={() => onAddClick(dayKey, slotNum)}
            className="group/cell w-full h-full min-h-[80px] flex items-center justify-center rounded-[16px] bg-transparent hover:bg-white/40 border border-dashed border-black/10 hover:border-(--red)/30 transition-all duration-150"
        >
            <span className="text-[11px] font-bold text-black/20 group-hover/cell:text-(--red) transition-colors opacity-0 group-hover/cell:opacity-100">
                + Добавить
            </span>
        </button>
    );
}