import { useEffect, useState, useMemo } from "react";
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

export interface CreateScheduleFormData {
    isSplit: boolean;
    // Данные для обычного урока или Группы 1
    teacherId: string;
    subjectId: string;
    room: string;
    groupId?: number; // id реальной группы (при делении)
    // Данные для Группы 2 (при делении)
    teacherId2?: string;
    subjectId2?: string;
    room2?: string;
    groupId2?: number;
}

interface TeacherSubject {
    teacher: { id: number; firstName: string; lastName: string };
    subject: { id: number; name: string };
}

interface CreateScheduleModalProps {
    isOpen: boolean;
    targetSlot: { dayOfWeek: string; lessonNumber: number } | null;
    schoolClassId: number;
    teacherSubjects: TeacherSubject[];
    isCreating: boolean;
    onClose: () => void;
    onSubmit: (data: CreateScheduleFormData) => void;
}

// Общие классы, чтобы все дропдауны в модалке выглядели одинаково опрятно
const selectContentClass =
    "rounded-xl min-w-[var(--radix-select-trigger-width)] max-h-[280px]";
const selectItemClass = "text-xs font-semibold py-2.5 px-3 cursor-pointer";

export default function CreateScheduleModal({
    isOpen,
    targetSlot,
    schoolClassId,
    teacherSubjects,
    isCreating,
    onClose,
    onSubmit,
}: CreateScheduleModalProps) {
    const [isSplit, setIsSplit] = useState(false);

    // Группа 1 / Обычный урок
    const [teacherId, setTeacherId] = useState("");
    const [subjectId, setSubjectId] = useState("");
    const [room, setRoom] = useState("");
    const [groupId, setGroupId] = useState("");

    // Группа 2
    const [teacherId2, setTeacherId2] = useState("");
    const [subjectId2, setSubjectId2] = useState("");
    const [room2, setRoom2] = useState("");
    const [groupId2, setGroupId2] = useState("");

    const [saveSuccess, setSaveSuccess] = useState(false);

    // Реальные группы класса вместо моков
    const {
        data: classGroups = [],
        isLoading: isGroupsLoading,
        isError: isGroupsError,
    } = useAllClassGroupsBySchoolClass(isOpen ? schoolClassId : 0);

    // Список уникальных учителей
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

    // Предметы для 1-го учителя
    const availableSubjects1 = useMemo(() => {
        if (!teacherId) return [];
        return teacherSubjects
            .filter((ts) => ts.teacher.id === Number(teacherId))
            .map((ts) => ts.subject);
    }, [teacherId, teacherSubjects]);

    // Предметы для 2-го учителя
    const availableSubjects2 = useMemo(() => {
        if (!teacherId2) return [];
        return teacherSubjects
            .filter((ts) => ts.teacher.id === Number(teacherId2))
            .map((ts) => ts.subject);
    }, [teacherId2, teacherSubjects]);

    // Автовыбор первых доступных групп, когда список подгрузился (без пересечений)
    useEffect(() => {
        if (!isSplit || classGroups.length === 0) return;
        if (!groupId) setGroupId(classGroups[0].id.toString());
        if (!groupId2) {
            const fallbackId = groupId || classGroups[0].id.toString();
            const second = classGroups.find((g) => g.id.toString() !== fallbackId) ?? classGroups[0];
            setGroupId2(second.id.toString());
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isSplit, classGroups]);

    // Если группы совпали (например, обе стали равны после смены класса) — развести их
    useEffect(() => {
        if (!isSplit || !groupId || !groupId2) return;
        if (groupId !== groupId2) return;
        const alternative = classGroups.find((g) => g.id.toString() !== groupId);
        if (alternative) setGroupId2(alternative.id.toString());
    }, [isSplit, groupId, groupId2, classGroups]);

    if (!isOpen || !targetSlot) return null;

    const isFormValid = isSplit
        ? teacherId && subjectId && teacherId2 && subjectId2 && groupId && groupId2 && groupId !== groupId2
        : teacherId && subjectId;

    const handleSubmit = () => {
        if (!isFormValid) return;

        onSubmit({
            isSplit,
            teacherId,
            subjectId,
            room: room.trim() || "Не указ.",
            groupId: isSplit ? Number(groupId) : undefined,
            teacherId2: isSplit ? teacherId2 : undefined,
            subjectId2: isSplit ? subjectId2 : undefined,
            room2: isSplit ? room2.trim() || "Не указ." : undefined,
            groupId2: isSplit ? Number(groupId2) : undefined,
        });

        setSaveSuccess(true);
        setTimeout(() => {
            setSaveSuccess(false);
            onClose();
        }, 1000);
    };

    const renderGroupSelect = (
        value: string,
        onChange: (v: string) => void,
        label: string,
        excludeId: string
    ) => {
        const options = classGroups.filter((g) => g.id.toString() !== excludeId);
        return (
            <div className="space-y-1">
                <label className="text-[10px] font-black uppercase tracking-widest text-black/40">
                    {label}
                </label>
                <Select
                    value={value}
                    onValueChange={onChange}
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

                <div className="px-8 py-6 space-y-5 max-h-[85vh] overflow-y-auto">
                    {/* Переключатель деления на группы */}
                    <div className="flex items-center justify-between p-4 rounded-2xl bg-white/40 border border-white/60">
                        <div className="flex items-center gap-2">
                            <Split className="w-4 h-4 text-(--red)" />
                            <span className="text-sm font-bold text-(--navy)">Разделить урок на 2 подгруппы</span>
                        </div>
                        <label className="relative inline-flex items-center cursor-pointer">
                            <input
                                type="checkbox"
                                checked={isSplit}
                                onChange={(e) => setIsSplit(e.target.checked)}
                                className="sr-only peer"
                            />
                            <div className="w-9 h-5 bg-black/15 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-(--red)"></div>
                        </label>
                    </div>

                    {/* Сетка для 1 или 2 колонок */}
                    <div className={`grid ${isSplit ? "grid-cols-1 md:grid-cols-2" : "grid-cols-1"} gap-5`}>
                        {/* Подгруппа 1 / Весь класс */}
                        <div className="space-y-4 p-4 rounded-2xl bg-white/20 border border-white/40">
                            {isSplit && renderGroupSelect(groupId, setGroupId, "Группа (Подгруппа 1)", groupId2)}

                            <div className="space-y-1">
                                <label className="text-[10px] font-black uppercase tracking-widest text-black/40">
                                    Преподаватель {isSplit && "1"}
                                </label>
                                <Select
                                    value={teacherId}
                                    onValueChange={(val) => {
                                        setTeacherId(val);
                                        setSubjectId("");
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
                            </div>

                            <div className="space-y-1">
                                <label className="text-[10px] font-black uppercase tracking-widest text-black/40">
                                    Дисциплина
                                </label>
                                <Select value={subjectId} onValueChange={setSubjectId} disabled={!teacherId}>
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
                            </div>

                            <div className="space-y-1">
                                <label className="text-[10px] font-black uppercase tracking-widest text-black/40">
                                    Аудитория
                                </label>
                                <input
                                    type="text"
                                    placeholder="Кабинет"
                                    value={room}
                                    onChange={(e) => setRoom(e.target.value)}
                                    className="w-full h-11 bg-white/50 border border-white/60 text-(--navy) rounded-xl px-3 text-xs font-semibold placeholder:font-normal placeholder:text-black/25 focus:outline-none"
                                />
                            </div>
                        </div>

                        {/* Подгруппа 2 (Рендерится только при включенном чекбоксе) */}
                        {isSplit && (
                            <div className="space-y-4 p-4 rounded-2xl bg-white/20 border border-white/40 animate-in fade-in duration-200">
                                {renderGroupSelect(groupId2, setGroupId2, "Группа (Подгруппа 2)", groupId)}

                                <div className="space-y-1">
                                    <label className="text-[10px] font-black uppercase tracking-widest text-black/40">
                                        Преподаватель 2
                                    </label>
                                    <Select
                                        value={teacherId2}
                                        onValueChange={(val) => {
                                            setTeacherId2(val);
                                            setSubjectId2("");
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
                                </div>

                                <div className="space-y-1">
                                    <label className="text-[10px] font-black uppercase tracking-widest text-black/40">
                                        Дисциплина
                                    </label>
                                    <Select value={subjectId2} onValueChange={setSubjectId2} disabled={!teacherId2}>
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
                                </div>

                                <div className="space-y-1">
                                    <label className="text-[10px] font-black uppercase tracking-widest text-black/40">
                                        Аудитория
                                    </label>
                                    <input
                                        type="text"
                                        placeholder="Кабинет"
                                        value={room2}
                                        onChange={(e) => setRoom2(e.target.value)}
                                        className="w-full h-11 bg-white/50 border border-white/60 text-(--navy) rounded-xl px-3 text-xs font-semibold placeholder:font-normal placeholder:text-black/25 focus:outline-none"
                                    />
                                </div>
                            </div>
                        )}
                    </div>

                    <div className="pt-2">
                        <button
                            onClick={handleSubmit}
                            disabled={!isFormValid || saveSuccess || isCreating}
                            className="w-full gap-2 bg-(--red) hover:bg-(--red-dark) text-white rounded-2xl py-4 text-base font-black shadow-lg shadow-(--red)/20 transition-all active:scale-[0.98] disabled:opacity-40 flex items-center justify-center cursor-pointer"
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
                                    Утвердить {isSplit ? "уроки" : "слот"}
                                    <Send className="w-4 h-4" />
                                </>
                            )}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}