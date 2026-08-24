import type { ScheduleLessonDto } from "@/services/schedule-service";
import { CalendarX2, MapPin, Plus, Trash2, Users } from "lucide-react";

const SUBJECT_COLORS = [
    { bg: "bg-red-50", border: "border-red-200", text: "text-red-700", accent: "bg-red-100" },
    { bg: "bg-blue-50", border: "border-blue-200", text: "text-blue-700", accent: "bg-blue-100" },
    { bg: "bg-green-50", border: "border-green-200", text: "text-green-700", accent: "bg-green-100" },
    { bg: "bg-purple-50", border: "border-purple-200", text: "text-purple-700", accent: "bg-purple-100" },
    { bg: "bg-orange-50", border: "border-orange-200", text: "text-orange-700", accent: "bg-orange-100" },
    { bg: "bg-cyan-50", border: "border-cyan-200", text: "text-cyan-700", accent: "bg-cyan-100" },
    { bg: "bg-pink-50", border: "border-pink-200", text: "text-pink-700", accent: "bg-pink-100" },
    { bg: "bg-amber-50", border: "border-amber-200", text: "text-amber-700", accent: "bg-amber-100" },
    { bg: "bg-teal-50", border: "border-teal-200", text: "text-teal-700", accent: "bg-teal-100" },
    { bg: "bg-indigo-50", border: "border-indigo-200", text: "text-indigo-700", accent: "bg-indigo-100" },
    { bg: "bg-rose-50", border: "border-rose-200", text: "text-rose-700", accent: "bg-rose-100" },
    { bg: "bg-lime-50", border: "border-lime-200", text: "text-lime-700", accent: "bg-lime-100" },
];

function hashSubject(name: string): number {
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
        hash = ((hash << 5) - hash + name.charCodeAt(i)) | 0;
    }
    return Math.abs(hash);
}

function getSubjectColor(name: string) {
    return SUBJECT_COLORS[hashSubject(name) % SUBJECT_COLORS.length];
}

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
    const color = getSubjectColor(lesson.subject.name);

    return (
        <div
            onClick={() => onLessonClick(lesson)}
            className={`w-full ${compact ? "p-2" : "p-2.5"} flex flex-col justify-between items-start text-left rounded-[16px] ${color.bg} border ${color.border} hover:brightness-95 transition-all duration-150 cursor-pointer`}
        >
            <div className="w-full">
                <div className="flex items-start justify-between gap-1">
                    <p className={`font-black ${color.text} leading-snug line-clamp-1 ${compact ? "text-[11px]" : "text-[13px]"}`}>
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
                <span className={`flex items-center gap-1 text-[10px] font-bold ${color.text}/60 ${color.accent} px-2 py-0.5 rounded-lg cursor-default`}>
                    <MapPin className="w-2.5 h-2.5" />
                    {lesson.classRoom}
                </span>
                <div className="flex items-center gap-0.5">
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            onDeleteClick(lesson);
                        }}
                        disabled={isDeleting}
                        className="p-1 rounded-md hover:bg-black/10 disabled:opacity-40 disabled:cursor-not-allowed"
                        title="Удалить урок"
                    >
                        <Trash2 className="w-3.5 h-3.5 text-red-400 transition-colors" />
                    </button>
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            onCloseClick(lesson);
                        }}
                        className="p-1 rounded-md hover:bg-black/10"
                        title="Закрыть урок"
                    >
                        <CalendarX2 className="w-3.5 h-3.5 text-red-400 transition-colors" />
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
        <div className="group/cell relative w-full h-full flex flex-col gap-1 justify-center">
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
            <button
                onClick={(e) => {
                    e.stopPropagation();
                    onAddClick(dayKey, slotNum);
                }}
                className="absolute -right-2 top-1/2 -translate-y-1/2 z-10 w-6 h-6 rounded-full bg-(--red) text-white flex items-center justify-center shadow-md opacity-0 group-hover/cell:opacity-100 scale-75 group-hover/cell:scale-100 transition-all duration-200 cursor-pointer "
            >
                <Plus className="w-3.5 h-3.5" />
            </button>
        </div>
    );
}
