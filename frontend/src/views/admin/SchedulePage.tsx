import { useState, useMemo, useEffect } from "react";
import {
    CalendarDays,
    Users,
    MapPin,
    Trash2,
    GraduationCap,
    CheckCircle2,
    X,
    Calendar,
    Send,
    Loader2,
    AlertTriangle,
    RefreshCw,
} from "lucide-react";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import AdminNavbar from "@/components/layout/navbars/AdminNavbar";
import { useFindUsersByFilter } from "@/hooks/use-teacher-subject";
import { useScheduleByClassId, useCreateSchedule, useCloseSchedule, useLoadLessonInstance } from "@/hooks/use-schedule";
import type { ScheduleLessonDto } from "@/services/schedule-service";
import { useGetAllClasses } from "@/hooks/use-school-class";

// ==========================================
// CONSTANTS
// ==========================================

const WEEKDAYS = [
    { key: "MONDAY", label: "Пн", full: "Понедельник" },
    { key: "TUESDAY", label: "Вт", full: "Вторник" },
    { key: "WEDNESDAY", label: "Ср", full: "Среда" },
    { key: "THURSDAY", label: "Чт", full: "Четверг" },
    { key: "FRIDAY", label: "Пт", full: "Пятница" },
];

const LESSON_SLOTS = [
    { num: 1, time: "08:30 – 09:15" },
    { num: 2, time: "09:25 – 10:10" },
    { num: 3, time: "10:25 – 11:10" },
    { num: 4, time: "11:20 – 12:05" },
    { num: 5, time: "12:35 – 13:20" },
    { num: 6, time: "13:30 – 14:15" },
    { num: 7, time: "14:25 – 15:10" },
];

// ==========================================
// LESSON CELL COMPONENT
// ==========================================

interface LessonCellProps {
    dayKey: string;
    slotNum: number;
    schedule: ScheduleLessonDto[];
    onAddClick: (day: string, slot: number) => void;
    onCloseClick: (lesson: ScheduleLessonDto) => void;
}

function LessonCell({ dayKey, slotNum, schedule, onAddClick, onCloseClick }: LessonCellProps) {
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
                        <p className="font-black text-[var(--navy)] text-[13px] leading-snug line-clamp-1">
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
                        <MapPin className="w-2.5 h-2.5 text-[var(--red)]" />
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
                        <Trash2 className="w-3.5 h-3.5 text-black/20 group-hover/cell:text-[var(--red)] transition-colors" />
                    </button>
                </div>
            </div>
        );
    }

    return (
        <button
            onClick={() => onAddClick(dayKey, slotNum)}
            className="group/cell w-full h-full min-h-[80px] flex items-center justify-center rounded-[16px] bg-transparent hover:bg-white/40 border border-dashed border-black/10 hover:border-[var(--red)]/30 transition-all duration-150"
        >
            <span className="text-[11px] font-bold text-black/20 group-hover/cell:text-[var(--red)] transition-colors opacity-0 group-hover/cell:opacity-100">
                + Добавить
            </span>
        </button>
    );
}

// ==========================================
// CONFIRM CLOSE MODAL
// ==========================================

interface ConfirmCloseModalProps {
    lesson: ScheduleLessonDto;
    isClosing: boolean;
    closeDate: string;
    onCloseDateChange: (date: string) => void;
    onConfirm: () => void;
    onCancel: () => void;
}

function ConfirmCloseModal({ lesson, isClosing, closeDate, onCloseDateChange, onConfirm, onCancel }: ConfirmCloseModalProps) {
    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-in fade-in duration-200"
            style={{ background: "rgba(15,20,40,0.25)", backdropFilter: "blur(8px)" }}
            onClick={onCancel}
        >
            <div
                className="glass-card w-full max-w-sm rounded-[28px] p-6 shadow-2xl backdrop-blur-xl animate-in slide-in-from-bottom-4 fade-in duration-200"
                style={{ boxShadow: "0 32px 80px rgba(15,20,60,0.12), 0 0 0 1px rgba(255,255,255,0.5)" }}
                onClick={(e) => e.stopPropagation()}
            >
                <div className="flex items-start gap-3 mb-5">
                    <div className="w-9 h-9 rounded-[12px] bg-orange-50 flex items-center justify-center ring-1 ring-orange-200 shrink-0 mt-0.5">
                        <AlertTriangle className="w-4 h-4 text-orange-500" />
                    </div>
                    <div>
                        <p className="font-black text-[var(--navy)] text-base leading-none">
                            Закрыть урок?
                        </p>
                        <p className="text-xs text-black/40 font-semibold mt-1.5 leading-relaxed">
                            <span className="text-[var(--navy)]">{lesson.subject.name}</span> перестанет проводиться
                            с выбранной даты. Прошлые оценки и посещаемость сохранятся.
                        </p>
                    </div>
                </div>

                {/* Дата закрытия */}
                <div className="space-y-1.5 mb-5">
                    <label className="text-[11px] font-black uppercase tracking-widest text-black/30">
                        Последний день урока
                    </label>
                    <div className="flex items-center gap-2 bg-white/40 border border-white/60 rounded-2xl px-4 h-11">
                        <Calendar className="w-3.5 h-3.5 text-[var(--red)] shrink-0" />
                        <input
                            type="date"
                            value={closeDate}
                            onChange={(e) => onCloseDateChange(e.target.value)}
                            className="bg-transparent text-[var(--navy)] font-bold text-sm focus:outline-none cursor-pointer w-full"
                        />
                    </div>
                    <p className="text-[10px] text-black/30 font-semibold px-1">
                        Урок включительно в этот день — последний
                    </p>
                </div>

                <div className="flex gap-2">
                    <button
                        onClick={onCancel}
                        className="flex-1 h-10 rounded-2xl bg-black/5 hover:bg-black/10 text-black/50 font-bold text-sm transition-all"
                    >
                        Отмена
                    </button>
                    <button
                        onClick={onConfirm}
                        disabled={isClosing || !closeDate}
                        className="flex-1 h-10 rounded-2xl bg-[var(--red)] hover:bg-[var(--red-dark)] text-white font-bold text-sm shadow-lg shadow-[var(--red)]/20 transition-all disabled:opacity-50 flex items-center justify-center gap-2"
                    >
                        {isClosing ? (
                            <Loader2 className="w-3.5 h-3.5 animate-spin" />
                        ) : (
                            "Закрыть"
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
}

// ==========================================
// GENERATE INSTANCES MODAL
// ==========================================

interface GenerateModalProps {
    classId: string;
    isGenerating: boolean;
    onConfirm: (from: string, to: string) => void;
    onCancel: () => void;
}

function GenerateModal({ classId, isGenerating, onConfirm, onCancel }: GenerateModalProps) {
    const todayStr = new Date().toISOString().split("T")[0];
    const nextMonthStr = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split("T")[0];
    const [fromDate, setFromDate] = useState(todayStr);
    const [toDate, setToDate] = useState(nextMonthStr);

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-in fade-in duration-200"
            style={{ background: "rgba(15,20,40,0.25)", backdropFilter: "blur(8px)" }}
            onClick={onCancel}
        >
            <div
                className="glass-card w-full max-w-sm rounded-[28px] p-6 shadow-2xl backdrop-blur-xl animate-in slide-in-from-bottom-4 fade-in duration-200"
                style={{ boxShadow: "0 32px 80px rgba(15,20,60,0.12), 0 0 0 1px rgba(255,255,255,0.5)" }}
                onClick={(e) => e.stopPropagation()}
            >
                <div className="flex items-start gap-3 mb-5">
                    <div className="w-9 h-9 rounded-[12px] bg-blue-50 flex items-center justify-center ring-1 ring-blue-200 shrink-0 mt-0.5">
                        <RefreshCw className="w-4 h-4 text-blue-500" />
                    </div>
                    <div>
                        <p className="font-black text-[var(--navy)] text-base leading-none">
                            Загрузить уроки
                        </p>
                        <p className="text-xs text-black/40 font-semibold mt-1.5 leading-relaxed">
                            Сгенерирует lesson instances по активным шаблонам за выбранный период
                        </p>
                    </div>
                </div>

                <div className="space-y-3 mb-5">
                    <div className="space-y-1.5">
                        <label className="text-[11px] font-black uppercase tracking-widest text-black/30">
                            С даты
                        </label>
                        <div className="flex items-center gap-2 bg-white/40 border border-white/60 rounded-2xl px-4 h-11">
                            <Calendar className="w-3.5 h-3.5 text-[var(--red)] shrink-0" />
                            <input
                                type="date"
                                value={fromDate}
                                onChange={(e) => setFromDate(e.target.value)}
                                className="bg-transparent text-[var(--navy)] font-bold text-sm focus:outline-none cursor-pointer w-full"
                            />
                        </div>
                    </div>
                    <div className="space-y-1.5">
                        <label className="text-[11px] font-black uppercase tracking-widest text-black/30">
                            По дату
                        </label>
                        <div className="flex items-center gap-2 bg-white/40 border border-white/60 rounded-2xl px-4 h-11">
                            <Calendar className="w-3.5 h-3.5 text-[var(--red)] shrink-0" />
                            <input
                                type="date"
                                value={toDate}
                                onChange={(e) => setToDate(e.target.value)}
                                className="bg-transparent text-[var(--navy)] font-bold text-sm focus:outline-none cursor-pointer w-full"
                            />
                        </div>
                    </div>
                </div>

                <div className="flex gap-2">
                    <button
                        onClick={onCancel}
                        className="flex-1 h-10 rounded-2xl bg-black/5 hover:bg-black/10 text-black/50 font-bold text-sm transition-all"
                    >
                        Отмена
                    </button>
                    <button
                        onClick={() => onConfirm(fromDate, toDate)}
                        disabled={isGenerating || !fromDate || !toDate}
                        className="flex-1 h-10 rounded-2xl bg-[var(--navy)] hover:bg-[var(--navy)]/80 text-white font-bold text-sm transition-all disabled:opacity-50 flex items-center justify-center gap-2"
                    >
                        {isGenerating ? (
                            <Loader2 className="w-3.5 h-3.5 animate-spin" />
                        ) : (
                            <>
                                <RefreshCw className="w-3.5 h-3.5" />
                                Загрузить
                            </>
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
}

// ==========================================
// MAIN PAGE
// ==========================================

export default function SchedulePage() {
    // Сегодняшняя дата в формате yyyy-MM-dd
    const todayStr = useMemo(() => {
        const d = new Date();
        return d.toISOString().split("T")[0];
    }, []);

    const [date, setDate] = useState<string>(todayStr);
    const [viewClassId, setViewClassId] = useState<string>("");

    const { data: classes = [], isLoading: isClassesLoading } = useGetAllClasses();

    // Автовыбор первого класса когда загрузились
    useEffect(() => {
        if (classes.length > 0 && !viewClassId) {
            setViewClassId(String(classes[0].id));
        }
    }, [classes, viewClassId]);

    // Расписание
    const {
        data: scheduleRecord,
        isLoading: isScheduleLoading,
        refetch: refetchSchedule,
    } = useScheduleByClassId(Number(viewClassId), date);

    const flatSchedule = useMemo<ScheduleLessonDto[]>(() => {
        if (!scheduleRecord) return [];
        return Object.values(scheduleRecord).flat();
    }, [scheduleRecord]);

    // Учителя с предметами
    const { data: teacherSubjects = [], isLoading: isTeachersLoading } = useFindUsersByFilter();

    // Мутации
    const { mutate: createSchedule, isPending: isCreating } = useCreateSchedule();
    const { mutate: closeSchedule, isPending: isClosing } = useCloseSchedule();
    const { mutate: loadInstances, isPending: isGenerating } = useLoadLessonInstance();

    // Состояние модалки генерации
    const [isGenerateModalOpen, setIsGenerateModalOpen] = useState(false);

    // Состояние модалки создания
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [targetSlot, setTargetSlot] = useState<{ dayOfWeek: string; lessonNumber: number } | null>(null);
    const [formTeacherId, setFormTeacherId] = useState("");
    const [formSubjectId, setFormSubjectId] = useState("");
    const [roomInput, setRoomInput] = useState("");
    const [saveSuccess, setSaveSuccess] = useState(false);

    // Состояние модалки закрытия
    const [lessonToClose, setLessonToClose] = useState<ScheduleLessonDto | null>(null);
    const [closeDate, setCloseDate] = useState<string>(todayStr);

    const isLoading = isScheduleLoading || isTeachersLoading || isClassesLoading;

    // Уникальные учителя
    const uniqueTeachers = useMemo(() => {
        const seen = new Set<number>();
        return teacherSubjects
            .filter((ts) => {
                if (seen.has(ts.teacher.id)) return false;
                seen.add(ts.teacher.id);
                return true;
            })
            .map((ts) => ts.teacher);
    }, [teacherSubjects]);

    // Предметы выбранного учителя
    const availableSubjectsForTeacher = useMemo(() => {
        if (!formTeacherId) return [];
        return teacherSubjects
            .filter((ts) => ts.teacher.id === Number(formTeacherId))
            .map((ts) => ts.subject);
    }, [formTeacherId, teacherSubjects]);

    // Сброс предмета при смене учителя
    useEffect(() => {
        setFormSubjectId("");
    }, [formTeacherId]);

    const handleGenerateConfirm = (from: string, to: string) => {
        if (!viewClassId) return;
        loadInstances(
            { classId: Number(viewClassId), fromDate: from, toDate: to },
            {
                onSuccess: () => {
                    setIsGenerateModalOpen(false);
                    refetchSchedule();
                },
            }
        );
    };

    const handleAddClick = (dayOfWeek: string, lessonNumber: number) => {
        setTargetSlot({ dayOfWeek, lessonNumber });
        setFormTeacherId("");
        setFormSubjectId("");
        setRoomInput("");
        setSaveSuccess(false);
        setIsModalOpen(true);
    };

    const handleCloseClick = (lesson: ScheduleLessonDto) => {
        setLessonToClose(lesson);
        setCloseDate(todayStr);
    };

    const handleConfirmClose = () => {
        if (!lessonToClose || !closeDate) return;
        closeSchedule({ scheduleId: lessonToClose.id, closeDate }, {
            onSuccess: () => {
                setLessonToClose(null);
                refetchSchedule();
            },
        });
    };

    const handleSaveLesson = () => {
        if (!targetSlot || !formTeacherId || !formSubjectId || !viewClassId) return;

        createSchedule(
            {
                classId: Number(viewClassId),
                teacherId: Number(formTeacherId),
                subjectId: Number(formSubjectId),
                dayOfWeek: targetSlot.dayOfWeek,
                lessonNumber: targetSlot.lessonNumber,
                classRoom: roomInput.trim() || "Не указ.",
                validFrom: date,
            },
            {
                onSuccess: () => {
                    setSaveSuccess(true);
                    refetchSchedule();
                    setTimeout(() => {
                        setIsModalOpen(false);
                        setSaveSuccess(false);
                    }, 1200);
                },
            }
        );
    };

    const currentClass = classes.find((c) => c.id === Number(viewClassId));
    const lessonsCount = flatSchedule.length;

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
            <AdminNavbar />

            {/* Header */}
            <div className="max-w-[1400px] mx-auto mb-6">
                <div className="glass-card rounded-[24px] p-5 flex flex-col lg:flex-row lg:items-center justify-between gap-5 border-none shadow-lg backdrop-blur-md">
                    <div className="flex items-center gap-4">
                        <div className="hidden sm:flex w-12 h-12 rounded-[18px] bg-[var(--red-light)]/60 items-center justify-center ring-1 ring-[var(--red)]/10">
                            <CalendarDays className="w-6 h-6 text-[var(--red)]" />
                        </div>
                        <div>
                            <h1 className="font-serif font-black text-2xl lg:text-3xl text-[var(--navy)] tracking-tight">
                                Расписание
                            </h1>
                            <p className="text-sm text-black/40 mt-0.5">
                                {isLoading
                                    ? "Загрузка данных..."
                                    : `${currentClass?.name ?? "—"} · ${lessonsCount} уро${lessonsCount === 1 ? "к" : lessonsCount < 5 ? "ка" : "ков"} на неделе`}
                            </p>
                        </div>
                    </div>

                    <div className="flex flex-wrap items-center gap-3">
                        <div className="flex items-center gap-2 bg-white/40 border border-white/60 rounded-2xl px-4 py-2 text-xs font-semibold text-black/50">
                            <Calendar className="w-3.5 h-3.5 text-[var(--red)]" />
                            <span>Дата:</span>
                            <input
                                type="date"
                                value={date}
                                onChange={(e) => setDate(e.target.value)}
                                className="bg-transparent text-[var(--navy)] font-bold focus:outline-none cursor-pointer"
                            />
                        </div>

                        <button
                            onClick={() => setIsGenerateModalOpen(true)}
                            disabled={!viewClassId}
                            className="flex items-center gap-2 h-10 px-4 rounded-2xl bg-white/40 border border-white/60 text-xs font-bold text-[var(--navy)] hover:bg-white/60 transition-all disabled:opacity-40"
                        >
                            <RefreshCw className="w-3.5 h-3.5 text-[var(--red)]" />
                            Загрузить уроки
                        </button>

                        <Select value={viewClassId} onValueChange={setViewClassId}>
                            <SelectTrigger className="w-[140px] h-10 text-xs font-bold rounded-2xl bg-white/40 border-white/60 text-[var(--navy)]">
                                <div className="flex items-center gap-2">
                                    <Users className="w-3.5 h-3.5 text-[var(--red)]" />
                                    <SelectValue placeholder="Класс" />
                                </div>
                            </SelectTrigger>
                            <SelectContent className="rounded-2xl">
                                {classes.map((c) => (
                                    <SelectItem key={c.id} value={c.id.toString()} className="text-xs font-bold">
                                        {c.name}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>
                </div>
            </div>

            {/* Grid */}
            <div className="max-w-[1400px] mx-auto">
                <div className="glass-card rounded-[32px] p-5 backdrop-blur-md overflow-x-auto">
                    {isLoading ? (
                        <div className="flex flex-col items-center justify-center py-32 text-[var(--navy)]/50 gap-4">
                            <Loader2 className="w-8 h-8 animate-spin text-[var(--red)]" />
                            <p className="text-sm font-bold animate-pulse">Загрузка расписания...</p>
                        </div>
                    ) : (
                        <div className="min-w-[700px]">
                            <div className="grid grid-cols-6 mb-2">
                                <div className="pr-3" />
                                {WEEKDAYS.map((day) => (
                                    <div
                                        key={day.key}
                                        className="px-2 py-2 text-center text-[11px] font-black uppercase tracking-widest text-black/30"
                                    >
                                        <span className="hidden md:block">{day.full}</span>
                                        <span className="md:hidden">{day.label}</span>
                                    </div>
                                ))}
                            </div>

                            <div className="space-y-1.5">
                                {LESSON_SLOTS.map((slot) => (
                                    <div key={slot.num} className="grid grid-cols-6 gap-1.5 items-stretch">
                                        <div className="flex flex-col items-center justify-center bg-white/30 rounded-[14px] py-3 px-2 text-center">
                                            <span className="text-xs font-black text-[var(--navy)]">{slot.num}</span>
                                            <span className="text-[9px] font-semibold text-black/30 mt-0.5 leading-tight">
                                                {slot.time}
                                            </span>
                                        </div>
                                        {WEEKDAYS.map((day) => (
                                            <div key={day.key} className="min-h-[80px]">
                                                <LessonCell
                                                    dayKey={day.key}
                                                    slotNum={slot.num}
                                                    schedule={flatSchedule}
                                                    onAddClick={handleAddClick}
                                                    onCloseClick={handleCloseClick}
                                                />
                                            </div>
                                        ))}
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Модалка создания */}
            {isModalOpen && targetSlot && (
                <div
                    className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-in fade-in duration-300"
                    style={{ background: "rgba(15,20,40,0.25)", backdropFilter: "blur(8px)" }}
                    onClick={() => setIsModalOpen(false)}
                >
                    <div
                        className="glass-card w-full max-w-md rounded-[36px] p-0 overflow-hidden shadow-2xl backdrop-blur-xl animate-in slide-in-from-bottom-4 fade-in duration-300"
                        style={{ boxShadow: "0 32px 80px rgba(15,20,60,0.12), 0 0 0 1px rgba(255,255,255,0.5)" }}
                        onClick={(e) => e.stopPropagation()}
                    >
                        <div className="px-6 pt-6 pb-4 flex items-center justify-between border-b border-black/5">
                            <div className="flex items-center gap-3">
                                <div className="w-9 h-9 rounded-[12px] bg-[var(--red-light)]/60 flex items-center justify-center ring-1 ring-[var(--red)]/10">
                                    <GraduationCap className="w-4 h-4 text-[var(--red)]" />
                                </div>
                                <div>
                                    <p className="font-black text-[var(--navy)] text-base leading-none">
                                        Новый урок
                                    </p>
                                    <p className="text-xs text-black/35 font-semibold mt-0.5">
                                        {WEEKDAYS.find((w) => w.key === targetSlot.dayOfWeek)?.full}, урок №{targetSlot.lessonNumber}
                                    </p>
                                </div>
                            </div>
                            <button
                                onClick={() => setIsModalOpen(false)}
                                className="w-8 h-8 rounded-xl bg-black/5 hover:bg-black/10 flex items-center justify-center text-black/30 hover:text-black/60 transition-all"
                            >
                                <X className="w-4 h-4" />
                            </button>
                        </div>

                        <div className="px-6 py-5 space-y-4">
                            <div className="space-y-1.5">
                                <label className="text-[11px] font-black uppercase tracking-widest text-black/30">
                                    Шаг 1 · Преподаватель
                                </label>
                                <Select value={formTeacherId} onValueChange={setFormTeacherId}>
                                    <SelectTrigger className="w-full h-12 text-sm font-bold rounded-2xl bg-white/40 border-white/60 text-[var(--navy)]">
                                        <SelectValue placeholder="Выберите учителя из штата" />
                                    </SelectTrigger>
                                    <SelectContent className="rounded-2xl">
                                        {uniqueTeachers.map((t) => (
                                            <SelectItem key={t.id} value={t.id.toString()} className="text-sm font-semibold">
                                                {`${t.lastName} ${t.firstName}`}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>

                            <div className="space-y-1.5">
                                <label className="text-[11px] font-black uppercase tracking-widest text-black/30">
                                    Шаг 2 · Дисциплина
                                </label>
                                <Select
                                    value={formSubjectId}
                                    onValueChange={setFormSubjectId}
                                    disabled={!formTeacherId}
                                >
                                    <SelectTrigger className="w-full h-12 text-sm font-bold rounded-2xl bg-white/40 border-white/60 text-[var(--navy)] disabled:opacity-40">
                                        <SelectValue
                                            placeholder={
                                                formTeacherId
                                                    ? "Выберите доступный предмет"
                                                    : "Сначала выберите учителя"
                                            }
                                        />
                                    </SelectTrigger>
                                    <SelectContent className="rounded-2xl">
                                        {availableSubjectsForTeacher.map((s) => (
                                            <SelectItem key={s.id} value={s.id.toString()} className="text-sm font-semibold">
                                                {s.name}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>

                            <div className="space-y-1.5">
                                <label className="text-[11px] font-black uppercase tracking-widest text-black/30">
                                    Аудитория
                                </label>
                                <input
                                    type="text"
                                    placeholder="Напр. 305, Лекторий"
                                    value={roomInput}
                                    onChange={(e) => setRoomInput(e.target.value)}
                                    className="w-full h-12 bg-white/40 border border-white/60 text-[var(--navy)] rounded-2xl px-4 text-sm font-semibold placeholder:font-normal placeholder:text-black/25 focus:outline-none focus:border-[var(--red)]/40"
                                />
                            </div>

                            <div className="pt-1">
                                <button
                                    onClick={handleSaveLesson}
                                    disabled={!formTeacherId || !formSubjectId || saveSuccess || isCreating}
                                    className="w-full gap-2 bg-[var(--red)] hover:bg-[var(--red-dark)] text-white rounded-2xl py-4 text-base font-black shadow-lg shadow-[var(--red)]/20 transition-all active:scale-[0.98] disabled:opacity-40 flex items-center justify-center"
                                >
                                    {isCreating ? (
                                        <Loader2 className="w-4 h-4 animate-spin" />
                                    ) : saveSuccess ? (
                                        <>
                                            <CheckCircle2 className="w-4 h-4" />
                                            Сохранено!
                                        </>
                                    ) : (
                                        <>
                                            Утвердить слот
                                            <Send className="w-4 h-4" />
                                        </>
                                    )}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Модалка подтверждения закрытия */}
            {isGenerateModalOpen && (
                <GenerateModal
                    classId={viewClassId}
                    isGenerating={isGenerating}
                    onConfirm={handleGenerateConfirm}
                    onCancel={() => setIsGenerateModalOpen(false)}
                />
            )}

            {lessonToClose && (
                <ConfirmCloseModal
                    lesson={lessonToClose}
                    isClosing={isClosing}
                    closeDate={closeDate}
                    onCloseDateChange={setCloseDate}
                    onConfirm={handleConfirmClose}
                    onCancel={() => setLessonToClose(null)}
                />
            )}
        </div>
    );
}