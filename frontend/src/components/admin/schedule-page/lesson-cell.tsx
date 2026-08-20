import type { ScheduleLessonDto } from "@/services/schedule-service";
import { CalendarX2, MapPin, Trash2, Users } from "lucide-react";

interface LessonCellProps {
    lessons: ScheduleLessonDto[];
    dayKey: string;
    slotNum: number;
    onAddClick: (day: string, slot: number) => void;
    onLessonClick: (lesson: ScheduleLessonDto) => void;
    onCloseClick: (lesson: ScheduleLessonDto) => void;
    onDeleteClick: (lesson: ScheduleLessonDto) => void;
    isDeleting?: boolean;
}

function LessonCard({
    lesson,
    compact,
    onLessonClick,
    onDeleteClick,
    onCloseClick,
    isDeleting,
}: {
    lesson: ScheduleLessonDto;
    compact?: boolean;
    onLessonClick: (lesson: ScheduleLessonDto) => void;
    onDeleteClick: (lesson: ScheduleLessonDto) => void;
    onCloseClick: (lesson: ScheduleLessonDto) => void;
    isDeleting?: boolean;
}) {
    return (
        <div
            onClick={() => onLessonClick(lesson)}
            className={`w-full ${compact ? "p-2" : "p-2.5"} flex flex-col justify-between items-start text-left rounded-[16px] bg-white/50 border border-white/60 hover:border-red-200 hover:bg-red-50/30 transition-all duration-150 cursor-pointer`}
        >
            <div className="w-full">
                <div className="flex items-start justify-between gap-1">
                    <p className={`font-black text-(--navy) leading-snug line-clamp-1 ${compact ? "text-[11px]" : "text-[13px]"}`}>
                        {lesson.subject.name}
                    </p>
                    {lesson.classGroup && (
                        <span className="shrink-0 flex items-center gap-0.5 text-[9px] font-black text-blue-500 bg-blue-50 border border-blue-200 px-1.5 py-0.5 rounded-md leading-none mt-0.5">
                            <Users className="w-2 h-2" />
                            {lesson.classGroup.name}
                        </span>
                    )}
                </div>
                <p className={`${compact ? "text-[10px]" : "text-[11px]"} text-black/40 font-semibold mt-0.5 line-clamp-1`}>
                    {lesson.teacher.lastName} {lesson.teacher.firstName.charAt(0)}.
                </p>
            </div>
            <div className="w-full flex items-center justify-between mt-2">
                <span className="flex items-center gap-1 text-[10px] font-bold text-black/30 bg-black/5 px-2 py-0.5 rounded-lg cursor-default">
                    <MapPin className="w-2.5 h-2.5 text-(--red)" />
                    {lesson.classRoom}
                </span>
                <div className="flex items-center gap-0.5">
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            onDeleteClick(lesson);
                        }}
                        disabled={isDeleting}
                        className="p-1 rounded-md hover:bg-red-100 disabled:opacity-40 disabled:cursor-not-allowed"
                        title="Удалить урок"
                    >
                        <Trash2 className="w-3.5 h-3.5 text-(--red) transition-colors" />
                    </button>
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            onCloseClick(lesson);
                        }}
                        className="p-1 rounded-md hover:bg-red-100"
                        title="Закрыть урок"
                    >
                        <CalendarX2 className="w-3.5 h-3.5 text-(--red) transition-colors" />
                    </button>
                </div>
            </div>
        </div>
    );
}

export default function LessonCell({
    lessons,
    dayKey,
    slotNum,
    onAddClick,
    onLessonClick,
    onCloseClick,
    onDeleteClick,
    isDeleting,
}: LessonCellProps) {
    if (lessons.length === 0) {
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

    const isSplit = lessons.length > 1;

    return (
        <div className={`group/cell w-full h-full flex flex-col gap-1 ${isSplit ? "justify-center" : "justify-center"}`}>
            {lessons.map((lesson) => (
                <LessonCard
                    key={lesson.id}
                    lesson={lesson}
                    compact={isSplit}
                    onLessonClick={onLessonClick}
                    onDeleteClick={onDeleteClick}
                    onCloseClick={onCloseClick}
                    isDeleting={isDeleting}
                />
            ))}
        </div>
    );
}
