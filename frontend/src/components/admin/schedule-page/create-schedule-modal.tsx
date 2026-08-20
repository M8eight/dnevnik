import { useEffect, useState, useMemo } from "react";
import { useForm, Controller, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { GraduationCap, CheckCircle2, X, Send, Loader2, Split } from "lucide-react";
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

const formSchema = z.object({
    isSplit: z.boolean(),
    teacherId: z.string().min(1, "Выберите учителя"),
    subjectId: z.string().min(1, "Выберите предмет"),
    room: z.string(),
    groupId: z.string(),
    teacherId2: z.string().default(""),
    subjectId2: z.string().default(""),
    room2: z.string().default(""),
    groupId2: z.string().default(""),
}).refine(
    (data) => {
        if (!data.isSplit) return true;
        return (
            data.teacherId2.length > 0 &&
            data.subjectId2.length > 0 &&
            data.groupId.length > 0 &&
            data.groupId2.length > 0 &&
            data.groupId !== data.groupId2
        );
    },
    { message: "Заполните все поля для второй подгруппы и выберите разные группы" }
);

type FormValues = z.infer<typeof formSchema>;

interface TeacherSubject {
    teacher: { id: number; firstName: string; lastName: string };
    subject: { id: number; name: string };
}

interface CreateScheduleModalProps {
    isOpen: boolean;
    targetSlot: { dayOfWeek: string; lessonNumber: number } | null;
    schoolClassId: number;
    teacherSubjects: TeacherSubject[];
    validFrom: string;
    onClose: () => void;
}

const selectContentClass =
    "rounded-xl min-w-[var(--radix-select-trigger-width)] max-h-[280px]";
const selectItemClass = "text-xs font-semibold py-2.5 px-3 cursor-pointer";

export default function CreateScheduleModal({
    isOpen,
    targetSlot,
    schoolClassId,
    teacherSubjects,
    validFrom,
    onClose,
}: CreateScheduleModalProps) {
    const [success, setSuccess] = useState(false);
    const createMutation = useCreateSchedule();

    const { control, handleSubmit, reset, setValue, getValues, formState: { errors } } = useForm<FormValues>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            isSplit: false,
            teacherId: "",
            subjectId: "",
            room: "",
            groupId: "",
            teacherId2: "",
            subjectId2: "",
            room2: "",
            groupId2: "",
        },
    });

    const isSplit = useWatch({ control, name: "isSplit" });
    const teacherId = useWatch({ control, name: "teacherId" });
    const teacherId2 = useWatch({ control, name: "teacherId2" });
    const groupId = useWatch({ control, name: "groupId" });
    const groupId2 = useWatch({ control, name: "groupId2" });

    const {
        data: classGroups = [],
        isLoading: isGroupsLoading,
        isError: isGroupsError,
    } = useAllClassGroupsBySchoolClass(isOpen ? schoolClassId : 0);

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

    const availableSubjects1 = useMemo(() => {
        if (!teacherId) return [];
        return teacherSubjects
            .filter((ts) => ts.teacher.id === Number(teacherId))
            .map((ts) => ts.subject);
    }, [teacherId, teacherSubjects]);

    const availableSubjects2 = useMemo(() => {
        if (!teacherId2) return [];
        return teacherSubjects
            .filter((ts) => ts.teacher.id === Number(teacherId2))
            .map((ts) => ts.subject);
    }, [teacherId2, teacherSubjects]);

    useEffect(() => {
        if (!isSplit || classGroups.length === 0) return;
        const currentGroupId = getValues("groupId");
        const currentGroupId2 = getValues("groupId2");
        if (!currentGroupId) setValue("groupId", classGroups[0].id.toString());
        if (!currentGroupId2) {
            const fallbackId = currentGroupId || classGroups[0].id.toString();
            const second = classGroups.find((g) => g.id.toString() !== fallbackId) ?? classGroups[0];
            setValue("groupId2", second.id.toString());
        }
    }, [isSplit, classGroups]);

    useEffect(() => {
        if (!isSplit || !groupId || !groupId2) return;
        if (groupId !== groupId2) return;
        const alternative = classGroups.find((g) => g.id.toString() !== groupId);
        if (alternative) setValue("groupId2", alternative.id.toString());
    }, [isSplit, groupId, groupId2, classGroups]);

    useEffect(() => {
        if (isOpen) {
            reset({
                isSplit: false,
                teacherId: "",
                subjectId: "",
                room: "",
                groupId: "",
                teacherId2: "",
                subjectId2: "",
                room2: "",
                groupId2: "",
            });
            setSuccess(false);
        }
    }, [isOpen, reset]);

    if (!isOpen || !targetSlot) return null;

    const onSubmit = (values: FormValues) => {
        const base: ScheduleRequest = {
            classId: schoolClassId,
            teacherId: Number(values.teacherId),
            subjectId: Number(values.subjectId),
            dayOfWeek: targetSlot.dayOfWeek,
            lessonNumber: targetSlot.lessonNumber,
            classRoom: values.room.trim() || "Не указ.",
            validFrom,
            classGroupId: values.groupId ? Number(values.groupId) : null,
        };

        createMutation.mutate(base, {
            onSuccess: () => {
                if (values.isSplit && values.teacherId2 && values.subjectId2) {
                    const second: ScheduleRequest = {
                        classId: schoolClassId,
                        teacherId: Number(values.teacherId2),
                        subjectId: Number(values.subjectId2),
                        dayOfWeek: targetSlot.dayOfWeek,
                        lessonNumber: targetSlot.lessonNumber,
                        classRoom: values.room2.trim() || "Не указ.",
                        validFrom,
                        classGroupId: values.groupId2 ? Number(values.groupId2) : null,
                    };
                    createMutation.mutate(second, {
                        onSuccess: () => {
                            setSuccess(true);
                            setTimeout(() => {
                                setSuccess(false);
                                onClose();
                            }, 1000);
                        },
                    });
                } else {
                    setSuccess(true);
                    setTimeout(() => {
                        setSuccess(false);
                        onClose();
                    }, 1000);
                }
            },
        });
    };

    const renderGroupSelect = (
        name: "groupId" | "groupId2",
        excludeName: "groupId" | "groupId2",
        label: string
    ) => {
        const currentValue = name === "groupId" ? groupId : groupId2;
        const excludeValue = name === "groupId" ? groupId2 : groupId;
        const options = classGroups.filter((g) => g.id.toString() !== excludeValue);

        return (
            <div className="space-y-1">
                <label className="text-[10px] font-black uppercase tracking-widest text-black/40">
                    {label}
                </label>
                <Controller
                    name={name}
                    control={control}
                    render={({ field }) => (
                        <Select
                            value={field.value}
                            onValueChange={field.onChange}
                            disabled={isGroupsLoading || options.length === 0}
                        >
                            <SelectTrigger className="w-full h-11 text-xs font-bold rounded-xl bg-white/50 border-white/60 text-(--navy)">
                                <SelectValue
                                    placeholder={
                                        isGroupsLoading
                                            ? "Загрузка групп..."
                                            : isGroupsError
                                            ? "Ошибка загрузки"
                                            : options.length === 0
                                            ? "Нет доступных групп"
                                            : "Выберите группу"
                                    }
                                />
                            </SelectTrigger>
                            <SelectContent className={selectContentClass}>
                                {options.map((g) => (
                                    <SelectItem key={g.id} value={g.id.toString()} className={selectItemClass}>
                                        {g.name}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    )}
                />
            </div>
        );
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-in fade-in duration-300"
            style={{ background: "rgba(15,20,40,0.25)", backdropFilter: "blur(8px)" }}
            onClick={onClose}
        >
            <div
                className={`glass-card w-full ${isSplit ? "max-w-4xl" : "max-w-xl"} rounded-[36px] p-0 overflow-hidden shadow-2xl backdrop-blur-xl animate-in slide-in-from-bottom-4 fade-in duration-300 transition-all`}
                style={{ boxShadow: "0 32px 80px rgba(15,20,60,0.12), 0 0 0 1px rgba(255,255,255,0.5)" }}
                onClick={(e) => e.stopPropagation()}
            >
                {/* Шапка */}
                <div className="px-8 pt-7 pb-5 flex items-center justify-between border-b border-black/5">
                    <div className="flex items-center gap-3">
                        <div className="w-11 h-11 rounded-[14px] bg-(--red-light)/60 flex items-center justify-center ring-1 ring-(--red)/10">
                            <GraduationCap className="w-5 h-5 text-(--red)" />
                        </div>
                        <div>
                            <p className="font-black text-(--navy) text-lg leading-none">
                                {isSplit ? "Деление урока на группы" : "Новый урок"}
                            </p>
                            <p className="text-xs text-black/35 font-semibold mt-1">
                                {DAYS_MAP.find((w) => w.key === targetSlot.dayOfWeek)?.full}, урок №{targetSlot.lessonNumber}
                            </p>
                        </div>
                    </div>
                    <button
                        onClick={onClose}
                        className="w-9 h-9 rounded-xl bg-black/5 hover:bg-black/10 flex items-center justify-center text-black/30 hover:text-black/60 transition-all cursor-pointer"
                    >
                        <X className="w-4 h-4" />
                    </button>
                </div>

                <form onSubmit={handleSubmit(onSubmit)}>
                    <div className="px-8 py-6 space-y-5 max-h-[85vh] overflow-y-auto">
                        {/* Переключатель деления на группы */}
                        <Controller
                            name="isSplit"
                            control={control}
                            render={({ field }) => (
                                <div className="flex items-center justify-between p-4 rounded-2xl bg-white/40 border border-white/60">
                                    <div className="flex items-center gap-2">
                                        <Split className="w-4 h-4 text-(--red)" />
                                        <span className="text-sm font-bold text-(--navy)">Разделить урок на 2 подгруппы</span>
                                    </div>
                                    <label className="relative inline-flex items-center cursor-pointer">
                                        <input
                                            type="checkbox"
                                            checked={field.value}
                                            onChange={field.onChange}
                                            className="sr-only peer"
                                        />
                                        <div className="w-9 h-5 bg-black/15 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-(--red)"></div>
                                    </label>
                                </div>
                            )}
                        />

                        {errors.root && (
                            <p className="text-xs text-(--red) font-semibold text-center">
                                {errors.root.message}
                            </p>
                        )}

                        {/* Сетка для 1 или 2 колонок */}
                        <div className={`grid ${isSplit ? "grid-cols-1 md:grid-cols-2" : "grid-cols-1"} gap-5`}>
                            {/* Подгруппа 1 / Весь класс */}
                            <div className="space-y-4 p-4 rounded-2xl bg-white/20 border border-white/40">
                                {isSplit && renderGroupSelect("groupId", "groupId2", "Группа (Подгруппа 1)")}

                                <div className="space-y-1">
                                    <label className="text-[10px] font-black uppercase tracking-widest text-black/40">
                                        Преподаватель {isSplit && "1"}
                                    </label>
                                    <Controller
                                        name="teacherId"
                                        control={control}
                                        render={({ field }) => (
                                            <Select
                                                value={field.value}
                                                onValueChange={(val) => {
                                                    field.onChange(val);
                                                    setValue("subjectId", "");
                                                }}
                                            >
                                                <SelectTrigger className="w-full h-11 text-xs font-bold rounded-xl bg-white/50 border-white/60 text-(--navy)">
                                                    <SelectValue placeholder="Выберите учителя" />
                                                </SelectTrigger>
                                                <SelectContent className={selectContentClass}>
                                                    {uniqueTeachers.map((t) => (
                                                        <SelectItem key={t.id} value={t.id.toString()} className={selectItemClass}>
                                                            {`${t.lastName} ${t.firstName}`}
                                                        </SelectItem>
                                                    ))}
                                                </SelectContent>
                                            </Select>
                                        )}
                                    />
                                </div>

                                <div className="space-y-1">
                                    <label className="text-[10px] font-black uppercase tracking-widest text-black/40">
                                        Дисциплина
                                    </label>
                                    <Controller
                                        name="subjectId"
                                        control={control}
                                        render={({ field }) => (
                                            <Select value={field.value} onValueChange={field.onChange} disabled={!teacherId}>
                                                <SelectTrigger className="w-full h-11 text-xs font-bold rounded-xl bg-white/50 border-white/60 text-(--navy) disabled:opacity-40">
                                                    <SelectValue placeholder={teacherId ? "Выберите предмет" : "Сначала учителя"} />
                                                </SelectTrigger>
                                                <SelectContent className={selectContentClass}>
                                                    {availableSubjects1.map((s) => (
                                                        <SelectItem key={s.id} value={s.id.toString()} className={selectItemClass}>
                                                            {s.name}
                                                        </SelectItem>
                                                    ))}
                                                </SelectContent>
                                            </Select>
                                        )}
                                    />
                                </div>

                                <div className="space-y-1">
                                    <label className="text-[10px] font-black uppercase tracking-widest text-black/40">
                                        Аудитория
                                    </label>
                                    <Controller
                                        name="room"
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

                            {/* Подгруппа 2 */}
                            {isSplit && (
                                <div className="space-y-4 p-4 rounded-2xl bg-white/20 border border-white/40 animate-in fade-in duration-200">
                                    {renderGroupSelect("groupId2", "groupId", "Группа (Подгруппа 2)")}

                                    <div className="space-y-1">
                                        <label className="text-[10px] font-black uppercase tracking-widest text-black/40">
                                            Преподаватель 2
                                        </label>
                                        <Controller
                                            name="teacherId2"
                                            control={control}
                                            render={({ field }) => (
                                                <Select
                                                    value={field.value}
                                                    onValueChange={(val) => {
                                                        field.onChange(val);
                                                        setValue("subjectId2", "");
                                                    }}
                                                >
                                                    <SelectTrigger className="w-full h-11 text-xs font-bold rounded-xl bg-white/50 border-white/60 text-(--navy)">
                                                        <SelectValue placeholder="Выберите 2-го учителя" />
                                                    </SelectTrigger>
                                                    <SelectContent className={selectContentClass}>
                                                        {uniqueTeachers.map((t) => (
                                                            <SelectItem key={t.id} value={t.id.toString()} className={selectItemClass}>
                                                                {`${t.lastName} ${t.firstName}`}
                                                            </SelectItem>
                                                        ))}
                                                    </SelectContent>
                                                </Select>
                                            )}
                                        />
                                    </div>

                                    <div className="space-y-1">
                                        <label className="text-[10px] font-black uppercase tracking-widest text-black/40">
                                            Дисциплина
                                        </label>
                                        <Controller
                                            name="subjectId2"
                                            control={control}
                                            render={({ field }) => (
                                                <Select value={field.value} onValueChange={field.onChange} disabled={!teacherId2}>
                                                    <SelectTrigger className="w-full h-11 text-xs font-bold rounded-xl bg-white/50 border-white/60 text-(--navy) disabled:opacity-40">
                                                        <SelectValue placeholder={teacherId2 ? "Выберите предмет" : "Сначала учителя"} />
                                                    </SelectTrigger>
                                                    <SelectContent className={selectContentClass}>
                                                        {availableSubjects2.map((s) => (
                                                            <SelectItem key={s.id} value={s.id.toString()} className={selectItemClass}>
                                                                {s.name}
                                                            </SelectItem>
                                                        ))}
                                                    </SelectContent>
                                                </Select>
                                            )}
                                        />
                                    </div>

                                    <div className="space-y-1">
                                        <label className="text-[10px] font-black uppercase tracking-widest text-black/40">
                                            Аудитория
                                        </label>
                                        <Controller
                                            name="room2"
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
                            )}
                        </div>

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
                                        Утвердить {isSplit ? "уроки" : "слот"}
                                        <Send className="w-4 h-4" />
                                    </>
                                )}
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    );
}
