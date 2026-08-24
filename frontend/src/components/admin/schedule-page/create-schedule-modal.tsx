import { useState, useCallback, useMemo } from "react";
import {
    useForm,
    Controller,
    useFieldArray,
    useWatch,
    type Control,
    type FieldPath,
    type UseFormSetValue,
} from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { GraduationCap, CheckCircle2, X, Send, Loader2, Plus, Trash2 } from "lucide-react";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { DAYS_MAP } from "@/constants/component-constants";
import { useAllClassGroupsBySchoolClass } from "@/hooks/use-class-group";
import { useCreateSchedule } from "@/hooks/use-schedule";
import type { ScheduleRequest } from "@/services/schedule-service";


const lessonSchema = z.object({
    teacherId: z.string().min(1, "Выберите учителя"),
    subjectId: z.string().min(1, "Выберите предмет"),
    room: z.string(),
    groupId: z.string(),
});

const formSchema = z.object({
    lessons: z.array(lessonSchema).min(1).max(2),
});

type FormValues = z.infer<typeof formSchema>;
type LessonFieldName = `lessons.${number}.${"teacherId" | "subjectId" | "groupId"}`;

interface TeacherSubject {
    teacher: { id: number; firstName: string; lastName: string };
    subject: { id: number; name: string };
}

interface Slot {
    dayOfWeek: string;
    lessonNumber: number;
}

interface CreateScheduleModalProps {
    isOpen: boolean;
    targetSlot: Slot | null;
    schoolClassId: number;
    teacherSubjects: TeacherSubject[];
    validFrom: string;
    onClose: () => void;
}

const MAX_LESSONS = 2;
const DEFAULT_ROOM_LABEL = "Не указ.";
const EMPTY_LESSON = { teacherId: "", subjectId: "", room: "", groupId: "" };

const SELECT_CONTENT_CLASS = "rounded-xl min-w-[var(--radix-select-trigger-width)] max-h-[280px]";
const SELECT_ITEM_CLASS = "text-xs font-semibold py-2.5 px-3 cursor-pointer";
const SELECT_TRIGGER_CLASS =
    "w-full h-11 text-xs font-bold rounded-xl bg-white/50 border-white/60 text-(--navy) disabled:opacity-40";
const FIELD_LABEL_CLASS = "text-[10px] font-black uppercase tracking-widest text-black/40";
const FIELD_ERROR_CLASS = "text-[10px] text-(--red) font-semibold mt-1";



export default function CreateScheduleModal({
    isOpen,
    targetSlot,
    schoolClassId,
    teacherSubjects,
    validFrom,
    onClose,
}: CreateScheduleModalProps) {
    if (!isOpen || !targetSlot) return null;

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-in fade-in duration-300"
            style={{ background: "rgba(15,20,40,0.25)", backdropFilter: "blur(8px)" }}
            onClick={onClose}
        >
            <div
                className="glass-card w-full max-w-xl rounded-[36px] p-0 overflow-hidden shadow-2xl backdrop-blur-xl animate-in slide-in-from-bottom-4 fade-in duration-300 transition-all"
                style={{ boxShadow: "0 32px 80px rgba(15,20,60,0.12), 0 0 0 1px rgba(255,255,255,0.5)" }}
                onClick={(e) => e.stopPropagation()}
            >
                <ScheduleForm
                    key={`${targetSlot.dayOfWeek}-${targetSlot.lessonNumber}`}
                    targetSlot={targetSlot}
                    schoolClassId={schoolClassId}
                    teacherSubjects={teacherSubjects}
                    validFrom={validFrom}
                    onClose={onClose}
                />
            </div>
        </div>
    );
}


function ScheduleForm({
    targetSlot,
    schoolClassId,
    teacherSubjects,
    validFrom,
    onClose,
}: {
    targetSlot: Slot;
    schoolClassId: number;
    teacherSubjects: TeacherSubject[];
    validFrom: string;
    onClose: () => void;
}) {
    const [success, setSuccess] = useState(false);
    const [submitError, setSubmitError] = useState<string | null>(null);
    const createMutation = useCreateSchedule();

    const {
        control,
        handleSubmit,
        setValue,
        formState: { errors },
    } = useForm<FormValues>({
        resolver: zodResolver(formSchema),
        defaultValues: { lessons: [EMPTY_LESSON] },
    });

    const { fields, append, remove } = useFieldArray({ control, name: "lessons" });

    const {
        data: classGroups = [],
        isLoading: isGroupsLoading,
        isError: isGroupsError,
    } = useAllClassGroupsBySchoolClass(schoolClassId);

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

    const getAvailableSubjects = useCallback(
        (teacherId: string) => {
            if (!teacherId) return [];
            return teacherSubjects
                .filter((ts) => ts.teacher.id === Number(teacherId))
                .map((ts) => ts.subject);
        },
        [teacherSubjects],
    );

    const onSubmit = async (values: FormValues) => {
        setSubmitError(null);

        const requests: ScheduleRequest[] = values.lessons.map((lesson) => ({
            classId: schoolClassId,
            teacherId: Number(lesson.teacherId),
            subjectId: Number(lesson.subjectId),
            dayOfWeek: targetSlot.dayOfWeek,
            lessonNumber: targetSlot.lessonNumber,
            classRoom: lesson.room.trim() || DEFAULT_ROOM_LABEL,
            validFrom,
            classGroupId: lesson.groupId ? Number(lesson.groupId) : null,
        }));

        try {
            await Promise.all(requests.map((req) => createMutation.mutateAsync(req)));
            setSuccess(true);
            setTimeout(() => {
                setSuccess(false);
                onClose();
            }, 1000);
        } catch {
            setSubmitError("Не удалось сохранить урок. Попробуйте ещё раз.");
        }
    };

    return (
        <>
            <ModalHeader
                dayLabel={DAYS_MAP.find((w) => w.key === targetSlot.dayOfWeek)?.full}
                lessonNumber={targetSlot.lessonNumber}
                onClose={onClose}
            />

            <form onSubmit={handleSubmit(onSubmit)}>
                <div className="px-8 py-6 space-y-4 max-h-[85vh] overflow-y-auto">
                    {(errors.root || submitError) && (
                        <p className="text-xs text-(--red) font-semibold text-center">
                            {errors.root?.message ?? submitError}
                        </p>
                    )}

                    {fields.map((field, index) => (
                        <LessonFields
                            key={field.id}
                            control={control}
                            setValue={setValue}
                            index={index}
                            teachers={uniqueTeachers}
                            getAvailableSubjects={getAvailableSubjects}
                            classGroups={classGroups}
                            isGroupsLoading={isGroupsLoading}
                            isGroupsError={isGroupsError}
                            canRemove={fields.length > 1}
                            onRemove={() => remove(index)}
                        />
                    ))}

                    {fields.length < MAX_LESSONS && (
                        <button
                            type="button"
                            onClick={() => append(EMPTY_LESSON)}
                            className="w-full flex items-center justify-center gap-2 py-3 rounded-2xl border-2 border-dashed border-black/10 hover:border-(--red)/30 hover:bg-(--red-light)/10 text-black/30 hover:text-(--red) text-xs font-bold transition-all cursor-pointer"
                        >
                            <Plus className="w-4 h-4" />
                            Добавить подгруппу
                        </button>
                    )}

                    <div className="pt-2">
                        <button
                            type="submit"
                            disabled={createMutation.isPending || success}
                            className="w-full gap-2 bg-(--red) hover:bg-(--red-dark) text-white rounded-2xl py-4 text-base font-black shadow-lg shadow-(--red)/20 transition-all active:scale-[0.98] disabled:opacity-40 flex items-center justify-center cursor-pointer"
                        >
                            {createMutation.isPending ? (
                                <Loader2 className="w-4 h-4 animate-spin" />
                            ) : success ? (
                                <>
                                    <CheckCircle2 className="w-4 h-4" />
                                    Сохранено!
                                </>
                            ) : (
                                <>
                                    Утвердить
                                    <Send className="w-4 h-4" />
                                </>
                            )}
                        </button>
                    </div>
                </div>
            </form>
        </>
    );
}


function ModalHeader({
    dayLabel,
    lessonNumber,
    onClose,
}: {
    dayLabel: string | undefined;
    lessonNumber: number;
    onClose: () => void;
}) {
    return (
        <div className="px-8 pt-7 pb-5 flex items-center justify-between border-b border-black/5">
            <div className="flex items-center gap-3">
                <div className="w-11 h-11 rounded-[14px] bg-(--red-light)/60 flex items-center justify-center ring-1 ring-(--red)/10">
                    <GraduationCap className="w-5 h-5 text-(--red)" />
                </div>
                <div>
                    <p className="font-black text-(--navy) text-lg leading-none">Новый урок</p>
                    <p className="text-xs text-black/35 font-semibold mt-1">
                        {dayLabel}, урок №{lessonNumber}
                    </p>
                </div>
            </div>
            <button
                onClick={onClose}
                aria-label="Закрыть"
                className="w-9 h-9 rounded-xl bg-black/5 hover:bg-black/10 flex items-center justify-center text-black/30 hover:text-black/60 transition-all cursor-pointer"
            >
                <X className="w-4 h-4" />
            </button>
        </div>
    );
}


interface FormSelectProps {
    control: Control<FormValues>;
    name: LessonFieldName;
    label: string;
    optional?: boolean;
    placeholder: string;
    options: { value: string; label: string }[];
    disabled?: boolean;
    onValueChange?: (value: string) => void;
}

function FormSelect({
    control,
    name,
    label,
    optional,
    placeholder,
    options,
    disabled,
    onValueChange,
}: FormSelectProps) {
    return (
        <div className="space-y-1">
            <label className={FIELD_LABEL_CLASS}>
                {label} {optional && <span className="text-black/20 normal-case">(необязательно)</span>}
            </label>
            <Controller
                name={name as FieldPath<FormValues>}
                control={control}
                render={({ field, fieldState }) => (
                    <>
                        <Select
                            value={field.value as string}
                            onValueChange={(value) => {
                                field.onChange(value);
                                onValueChange?.(value);
                            }}
                            disabled={disabled}
                        >
                            <SelectTrigger className={SELECT_TRIGGER_CLASS}>
                                <SelectValue placeholder={placeholder} />
                            </SelectTrigger>
                            <SelectContent className={SELECT_CONTENT_CLASS}>
                                {options.map((opt) => (
                                    <SelectItem key={opt.value} value={opt.value} className={SELECT_ITEM_CLASS}>
                                        {opt.label}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                        {fieldState.invalid && <p className={FIELD_ERROR_CLASS}>{fieldState.error?.message}</p>}
                    </>
                )}
            />
        </div>
    );
}


function LessonFields({
    control,
    setValue,
    index,
    teachers,
    getAvailableSubjects,
    classGroups,
    isGroupsLoading,
    isGroupsError,
    canRemove,
    onRemove,
}: {
    control: Control<FormValues>;
    setValue: UseFormSetValue<FormValues>;
    index: number;
    teachers: { id: number; firstName: string; lastName: string }[];
    getAvailableSubjects: (teacherId: string) => { id: number; name: string }[];
    classGroups: { id: number; name: string }[];
    isGroupsLoading: boolean;
    isGroupsError: boolean;
    canRemove: boolean;
    onRemove: () => void;
}) {
    const teacherId = useWatch({ control, name: `lessons.${index}.teacherId` });
    const subjectId = useWatch({ control, name: `lessons.${index}.subjectId` });

    const availableSubjects = useMemo(
        () => getAvailableSubjects(teacherId),
        [teacherId, getAvailableSubjects],
    );

    const teacherOptions = useMemo(
        () => teachers.map((t) => ({ value: t.id.toString(), label: `${t.lastName} ${t.firstName}` })),
        [teachers],
    );
    const subjectOptions = useMemo(
        () => availableSubjects.map((s) => ({ value: s.id.toString(), label: s.name })),
        [availableSubjects],
    );
    const groupOptions = useMemo(
        () => classGroups.map((g) => ({ value: g.id.toString(), label: g.name })),
        [classGroups],
    );

    const groupPlaceholder = isGroupsLoading
        ? "Загрузка групп..."
        : isGroupsError
          ? "Ошибка загрузки"
          : classGroups.length === 0
            ? "Нет доступных групп"
            : "Весь класс";

    const handleTeacherChange = (newTeacherId: string) => {
        const nextSubjects = getAvailableSubjects(newTeacherId);
        if (subjectId && !nextSubjects.some((s) => s.id.toString() === subjectId)) {
            setValue(`lessons.${index}.subjectId`, "");
        }
    };

    return (
        <div className="relative space-y-4 p-4 rounded-2xl bg-white/20 border border-white/40 animate-in fade-in duration-200">
            {canRemove && (
                <button
                    type="button"
                    onClick={onRemove}
                    aria-label="Удалить подгруппу"
                    className="absolute top-3 right-3 w-7 h-7 rounded-lg bg-black/5 hover:bg-red-100 flex items-center justify-center text-black/30 hover:text-(--red) transition-all cursor-pointer"
                >
                    <Trash2 className="w-3.5 h-3.5" />
                </button>
            )}

            <FormSelect
                control={control}
                name={`lessons.${index}.groupId`}
                label="Группа"
                optional
                placeholder={groupPlaceholder}
                options={groupOptions}
                disabled={isGroupsLoading || classGroups.length === 0}
            />

            <FormSelect
                control={control}
                name={`lessons.${index}.teacherId`}
                label="Преподаватель"
                placeholder="Выберите учителя"
                options={teacherOptions}
                onValueChange={handleTeacherChange}
            />

            <FormSelect
                control={control}
                name={`lessons.${index}.subjectId`}
                label="Дисциплина"
                placeholder={teacherId ? "Выберите предмет" : "Сначала учителя"}
                options={subjectOptions}
                disabled={!teacherId}
            />

            <div className="space-y-1">
                <label className={FIELD_LABEL_CLASS}>Аудитория</label>
                <Controller
                    name={`lessons.${index}.room`}
                    control={control}
                    render={({ field }) => (
                        <input
                            type="text"
                            placeholder="Кабинет"
                            value={field.value}
                            onChange={field.onChange}
                            className="w-full h-11 bg-white/50 border border-white/60 text-(--navy) rounded-xl px-3 text-xs font-semibold placeholder:font-normal placeholder:text-black/25 focus:outline-none"
                        />
                    )}
                />
            </div>
        </div>
    );
}
