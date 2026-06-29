import { useState, useMemo } from "react";
import {
    CalendarDays,
    Users,
    GraduationCap,
    CheckCircle2,
    X,
    Calendar,
    Send,
    Loader2,
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
import { useGetTeacherSubjects } from "@/hooks/use-teacher-subject";
import { useScheduleByClassId, useCreateSchedule, useCloseSchedule, useLoadLessonInstance } from "@/hooks/use-schedule";
import type { ScheduleLessonDto } from "@/services/schedule-service";
import { useGetAllClasses } from "@/hooks/use-school-class";
import { DAYS_MAP, LESSON_SLOTS } from "@/constants/component-constants";
import LessonCell from "@/components/admin/schedule-page/lesson-cell";
import ConfirmCloseModal from "@/components/admin/schedule-page/confirm-close-modal";
import GenerateModal from "@/components/admin/schedule-page/generate-modal";

export default function SchedulePage() {
    const todayStr = useMemo(() => {
        const d = new Date();
        return d.toISOString().split("T")[0];
    }, []);

    const [date, setDate] = useState<string>(todayStr);
    
    const [viewClassId, setViewClassId] = useState<string>("");

    const { data: classes = [], isLoading: isClassesLoading } = useGetAllClasses();

    const activeClassId = viewClassId || (classes.length > 0 ? String(classes[0].id) : "");

    const {
        data: scheduleRecord,
        isLoading: isScheduleLoading,
        refetch: refetchSchedule,
    } = useScheduleByClassId(Number(activeClassId), date);

    const flatSchedule = useMemo<ScheduleLessonDto[]>(() => {
        if (!scheduleRecord) return [];
        return Object.values(scheduleRecord).flat();
    }, [scheduleRecord]);

    const { data: teacherSubjects = [], isLoading: isTeachersLoading } = useGetTeacherSubjects();

    const { mutate: createSchedule, isPending: isCreating } = useCreateSchedule();
    const { mutate: closeSchedule, isPending: isClosing } = useCloseSchedule();
    const { mutate: loadInstances, isPending: isGenerating } = useLoadLessonInstance();

    const [isGenerateModalOpen, setIsGenerateModalOpen] = useState(false);

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [targetSlot, setTargetSlot] = useState<{ dayOfWeek: string; lessonNumber: number } | null>(null);
    const [formTeacherId, setFormTeacherId] = useState("");
    const [formSubjectId, setFormSubjectId] = useState("");
    const [roomInput, setRoomInput] = useState("");
    const [saveSuccess, setSaveSuccess] = useState(false);

    const [lessonToClose, setLessonToClose] = useState<ScheduleLessonDto | null>(null);
    const [closeDate, setCloseDate] = useState<string>(todayStr);

    const isLoading = isScheduleLoading || isTeachersLoading || isClassesLoading;

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

    const availableSubjectsForTeacher = useMemo(() => {
        if (!formTeacherId) return [];
        return teacherSubjects
            .filter((ts) => ts.teacher.id === Number(formTeacherId))
            .map((ts) => ts.subject);
    }, [formTeacherId, teacherSubjects]);

    const handleGenerateConfirm = (from: string, to: string) => {
        if (!activeClassId) return;
        loadInstances(
            { classId: Number(activeClassId), fromDate: from, toDate: to },
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
        if (!targetSlot || !formTeacherId || !formSubjectId || !activeClassId) return;

        createSchedule(
            {
                classId: Number(activeClassId),
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

    const currentClass = classes.find((c) => c.id === Number(activeClassId));
    const lessonsCount = flatSchedule.length;

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
            <AdminNavbar />

            {/* Header */}
            <div className="max-w-350 mx-auto mb-6">
                <div className="glass-card rounded-[24px] p-5 flex flex-col lg:flex-row lg:items-center justify-between gap-5 border-none shadow-lg backdrop-blur-md">
                    <div className="flex items-center gap-4">
                        <div className="hidden sm:flex w-12 h-12 rounded-[18px] bg-(--red-light)/60 items-center justify-center ring-1 ring-(--red)/10">
                            <CalendarDays className="w-6 h-6 text-(--red)" />
                        </div>
                        <div>
                            <h1 className="font-serif font-black text-2xl lg:text-3xl text-(--navy) tracking-tight">
                                Расписание
                            </h1>
                            <p className="text-sm text-black/40 mt-0.5">
                                {isLoading
                                    ? "Загрузка данных..."
                                    : `${currentClass?.name ?? "—"} · ${lessonsCount} уро${lessonsCount === 1 ? "к" : lessonsCount < 5 && lessonsCount > 0 ? "ка" : "ков"} на неделе`}
                            </p>
                        </div>
                    </div>

                    <div className="flex flex-wrap items-center gap-3">
                        <div className="flex items-center gap-2 bg-white/40 border border-white/60 rounded-2xl px-4 py-2 text-xs font-semibold text-black/50">
                            <Calendar className="w-3.5 h-3.5 text-(--red)" />
                            <span>Дата:</span>
                            <input
                                type="date"
                                value={date}
                                onChange={(e) => setDate(e.target.value)}
                                className="bg-transparent text-(--navy) font-bold focus:outline-none cursor-pointer"
                            />
                        </div>

                        <button
                            onClick={() => setIsGenerateModalOpen(true)}
                            disabled={!activeClassId}
                            className="flex items-center gap-2 h-10 px-4 rounded-2xl bg-white/40 border border-white/60 text-xs font-bold text-(--navy) hover:bg-white/60 transition-all disabled:opacity-40"
                        >
                            <RefreshCw className="w-3.5 h-3.5 text-(--red)" />
                            Загрузить уроки
                        </button>

                        <Select value={activeClassId} onValueChange={setViewClassId}>
                            <SelectTrigger className="w-35 h-10 text-xs font-bold rounded-2xl bg-white/40 border-white/60 text-(--navy)">
                                <div className="flex items-center gap-2">
                                    <Users className="w-3.5 h-3.5 text-(--red)" />
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
            <div className="max-w-350 mx-auto">
                <div className="glass-card rounded-[32px] p-5 backdrop-blur-md overflow-x-auto">
                    {isLoading ? (
                        <div className="flex flex-col items-center justify-center py-32 text-(--navy)/50 gap-4">
                            <Loader2 className="w-8 h-8 animate-spin text-(--red)" />
                            <p className="text-sm font-bold animate-pulse">Загрузка расписания...</p>
                        </div>
                    ) : (
                        <div className="min-w-175">
                            <div className="grid grid-cols-6 mb-2">
                                <div className="pr-3" />
                                {DAYS_MAP.map((day) => (
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
                                            <span className="text-xs font-black text-(--navy)">{slot.num}</span>
                                            <span className="text-[9px] font-semibold text-black/30 mt-0.5 leading-tight">
                                                {slot.time}
                                            </span>
                                        </div>
                                        {DAYS_MAP.map((day) => (
                                            <div key={day.key} className="min-h-20">
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
                                <div className="w-9 h-9 rounded-[12px] bg-(--red-light)/60 flex items-center justify-center ring-1 ring-(--red)/10">
                                    <GraduationCap className="w-4 h-4 text-(--red)" />
                                </div>
                                <div>
                                    <p className="font-black text-(--navy) text-base leading-none">
                                        Новый урок
                                    </p>
                                    <p className="text-xs text-black/35 font-semibold mt-0.5">
                                        {DAYS_MAP.find((w) => w.key === targetSlot.dayOfWeek)?.full}, урок №{targetSlot.lessonNumber}
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
                                <Select 
                                    value={formTeacherId} 
                                    onValueChange={(val) => {
                                        setFormTeacherId(val);
                                        setFormSubjectId("");
                                    }}
                                >
                                    <SelectTrigger className="w-full h-12 text-sm font-bold rounded-2xl bg-white/40 border-white/60 text-(--navy)">
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
                                    <SelectTrigger className="w-full h-12 text-sm font-bold rounded-2xl bg-white/40 border-white/60 text-(--navy) disabled:opacity-40">
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
                                    className="w-full h-12 bg-white/40 border border-white/60 text-(--navy) rounded-2xl px-4 text-sm font-semibold placeholder:font-normal placeholder:text-black/25 focus:outline-none focus:border-(--red)/40"
                                />
                            </div>

                            <div className="pt-1">
                                <button
                                    onClick={handleSaveLesson}
                                    disabled={!formTeacherId || !formSubjectId || saveSuccess || isCreating}
                                    className="w-full gap-2 bg-(--red) hover:bg-(--red-dark) text-white rounded-2xl py-4 text-base font-black shadow-lg shadow-(--red)/20 transition-all active:scale-[0.98] disabled:opacity-40 flex items-center justify-center"
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