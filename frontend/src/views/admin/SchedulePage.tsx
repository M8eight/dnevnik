import { useState, useMemo } from "react";
import {
    CalendarDays,
    Users,
    Calendar,
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
import { useScheduleByClassId, useCreateSchedule, useCloseSchedule, useLoadLessonInstance, useDeleteSchedule } from "@/hooks/use-schedule";
import type { ScheduleLessonDto } from "@/services/schedule-service";
import { useGetAllClassesByAcademicYear } from "@/hooks/use-school-class";
import { DAYS_MAP, LESSON_SLOTS } from "@/constants/component-constants";
import LessonCell from "@/components/admin/schedule-page/lesson-cell";
import ConfirmCloseModal from "@/components/admin/schedule-page/confirm-close-modal";
import GenerateModal from "@/components/admin/schedule-page/generate-modal";
import CreateScheduleModal, { type CreateScheduleFormData } from "@/components/admin/schedule-page/create-schedule-modal";
import { useAcademicYearSelection } from "@/hooks/use-academic-year-selection";
import PageHeader from "@/components/admin/page-header";
import AcademicYearSelect from "@/components/admin/academic-year-select";
import ClosedYearAlert from "@/components/admin/closed-year-alert";
import { getCurrentWeekString, getMondayFromWeekString, toISODate } from "@/lib/date";

export default function SchedulePage() {
    const todayStr = useMemo(() => toISODate(new Date()), []);
    const [week, setWeek] = useState<string>(() => getCurrentWeekString());

    const selectedMondayDate = useMemo(() => getMondayFromWeekString(week), [week]);

    const {
        resolvedAcademicYearId,
        setSelectedAcademicYearId,
        currentAcademicYear,
        isYearClosed,
    } = useAcademicYearSelection();

    const [viewClassId, setViewClassId] = useState<string>("");

    const { data: classes = [], isLoading: isClassesLoading } = useGetAllClassesByAcademicYear(parseInt(resolvedAcademicYearId, 10));
    const activeClassId = viewClassId || (classes.length > 0 ? String(classes[0].id) : "");

    const {
        data: scheduleRecord,
        isLoading: isScheduleLoading,
        refetch: refetchSchedule,
    } = useScheduleByClassId(Number(activeClassId), selectedMondayDate);

    const flatSchedule = useMemo<ScheduleLessonDto[]>(() => {
        if (!scheduleRecord) return [];
        return Object.values(scheduleRecord).flat();
    }, [scheduleRecord]);

    const { data: teacherSubjects = [], isLoading: isTeachersLoading } = useGetTeacherSubjects();

    const { mutate: createSchedule, isPending: isCreating } = useCreateSchedule();
    const { mutate: closeSchedule, isPending: isClosing } = useCloseSchedule();
    const { mutate: deleteSchedule, isPending: isDeleting } = useDeleteSchedule();
    const { mutate: loadInstances, isPending: isGenerating } = useLoadLessonInstance();

    const [isGenerateModalOpen, setIsGenerateModalOpen] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [targetSlot, setTargetSlot] = useState<{ dayOfWeek: string; lessonNumber: number } | null>(null);

    const [lessonToClose, setLessonToClose] = useState<ScheduleLessonDto | null>(null);
    const [closeDate, setCloseDate] = useState<string>(todayStr);

    const isLoading = isScheduleLoading || isTeachersLoading || isClassesLoading;

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

    const handleSaveSchedule = (formData: CreateScheduleFormData) => {
        if (!targetSlot || !activeClassId) return;

        const payload1 = {
            classId: Number(activeClassId),
            teacherId: Number(formData.teacherId),
            subjectId: Number(formData.subjectId),
            dayOfWeek: targetSlot.dayOfWeek,
            lessonNumber: targetSlot.lessonNumber,
            classRoom: formData.room,
            validFrom: selectedMondayDate,
            ...(formData.groupId ? { groupId: formData.groupId } : {}),
        };

        createSchedule(payload1, {
            onSuccess: () => {
                if (formData.isSplit && formData.teacherId2 && formData.subjectId2) {
                    const payload2 = {
                        classId: Number(activeClassId),
                        teacherId: Number(formData.teacherId2),
                        subjectId: Number(formData.subjectId2),
                        dayOfWeek: targetSlot.dayOfWeek,
                        lessonNumber: targetSlot.lessonNumber,
                        classRoom: formData.room2 || "Не указ.",
                        validFrom: selectedMondayDate,
                        ...(formData.groupId2 ? { groupId: formData.groupId2 } : {}),
                    };
                    createSchedule(payload2, {
                        onSuccess: () => refetchSchedule(),
                    });
                } else {
                    refetchSchedule();
                }
            },
        });
    };

    const currentClass = classes.find((c) => c.id === Number(activeClassId));
    const lessonsCount = flatSchedule.length;

    const handleDeleteClick = (lesson: ScheduleLessonDto) => {
        if (!window.confirm(`Удалить урок без возможности восстановления? Это действие необратимо.`)) {
            return;
        }
        deleteSchedule(lesson.id, {
            onSuccess: () => {
                refetchSchedule();
            },
        });
    };

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
            <AdminNavbar />

            <PageHeader
                icon={CalendarDays}
                title="Расписание"
                subtitle={
                    isLoading
                        ? "Загрузка данных..."
                        : `${currentClass?.name ?? "—"} · ${lessonsCount} уро${lessonsCount === 1 ? "к" : lessonsCount < 5 && lessonsCount > 0 ? "ка" : "ков"} на неделе`
                }
            >
                <div className="flex items-center gap-2 bg-white/40 border border-white/60 rounded-2xl px-4 py-2 text-xs font-semibold text-black/50">
                    <Calendar className="w-3.5 h-3.5 text-(--red)" />
                    <span>Дата:</span>
                    <input
                        type="week"
                        value={week}
                        onChange={(e) => setWeek(e.target.value)}
                        className="bg-transparent text-(--navy) font-bold focus:outline-none cursor-pointer"
                    />
                </div>

                <button
                    onClick={() => setIsGenerateModalOpen(true)}
                    disabled={!activeClassId}
                    className="flex items-center gap-2 h-10 px-4 rounded-2xl bg-white/40 border border-white/60 text-xs font-bold text-(--navy) hover:bg-white/60 transition-all disabled:opacity-40 cursor-pointer"
                >
                    <RefreshCw className="w-3.5 h-3.5 text-(--red)" />
                    Загрузить уроки
                </button>

                <AcademicYearSelect
                    value={resolvedAcademicYearId}
                    onChange={setSelectedAcademicYearId}
                />

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

            </PageHeader>

            {isYearClosed && (
                <ClosedYearAlert
                    yearName={currentAcademicYear?.name}
                    description="Операции удаления и редактирования классов запрещены"
                />
            )}

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
                                            <span className="text-sm font-black text-(--navy)">{slot.num}</span>
                                            <span className="text-xs font-semibold text-black/30 mt-0.5 leading-tight">
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
                                                    onDeleteClick={handleDeleteClick}
                                                    isDeleting={isDeleting}
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
            <CreateScheduleModal
                schoolClassId={Number(activeClassId)}
                isOpen={isModalOpen}
                targetSlot={targetSlot}
                teacherSubjects={teacherSubjects}
                isCreating={isCreating}
                onClose={() => setIsModalOpen(false)}
                onSubmit={handleSaveSchedule}
            />

            {/* Модалка генерации / загрузки */}
            {isGenerateModalOpen && (
                <GenerateModal
                    isGenerating={isGenerating}
                    onConfirm={handleGenerateConfirm}
                    onCancel={() => setIsGenerateModalOpen(false)}
                />
            )}

            {/* Модалка подтверждения закрытия */}
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