import React, { useState, useEffect, useMemo } from "react";
import { cn } from "@/lib/utils";
import { NavLink } from "react-router-dom";
import {
    BookOpen,
    Users,
    GraduationCap,
    Plus,
    Send,
    FileText,
    Calendar as CalendarIcon,
    CalendarDays,
    ChevronLeft,
    ChevronRight,
    Sparkles,
} from "lucide-react";
import { useTeachingAssignmentDetail } from "@/hooks/use-teaching-assignment";
import { useCreateHomework, useHomeworksByTeachingAssignment } from "@/hooks/use-homework";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Calendar } from "@/components/ui/calendar";
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover";
import { format, isSameDay, parseISO, startOfMonth, endOfMonth, eachDayOfInterval, getDay, isToday } from "date-fns";
import { ru } from "date-fns/locale";
import type { HomeworkResponse } from "@/services/homework-service";
import { useLessonInstancesByTeachingAssignment } from "@/hooks/use-lesson-instances";
import type { lessonInstance } from "@/services/lesson-instance-service";
import { useGetAcademicPeriods } from "@/hooks/use-academic-period";
import { Trash2 } from "lucide-react";
import { useDeleteHomework } from "@/hooks/use-homework";

// ─── helpers ────────────────────────────────────────────────────────────────

function toDateKey(date: Date) {
    return format(date, "yyyy-MM-dd");
}

// ─── CreateHomeworkForm ──────────────────────────────────────────────────────

function CreateHomeworkForm({
    onSubmit,
    isSubmitting,
    lessonInstances,
    preselectedDate,
}: {
    onSubmit: (text: string, lessonInstanceId: number) => void;
    isSubmitting: boolean;
    lessonInstances: lessonInstance[];
    preselectedDate?: Date | null;
}) {
    const [text, setText] = useState("");
    const [selectedInstanceId, setSelectedInstanceId] = useState<number | null>(null);

    useEffect(() => {
        if (preselectedDate) {
            const inst = lessonInstances.find((i) =>
                isSameDay(parseISO(i.lessonDate), preselectedDate)
            );
            setSelectedInstanceId(inst?.id ?? null);
        }
    }, [preselectedDate, lessonInstances]);

    const selectedInstance = lessonInstances.find((i) => i.id === selectedInstanceId);
    const date = selectedInstance ? parseISO(selectedInstance.lessonDate) : undefined;

    const disabledDays = (d: Date) =>
        !lessonInstances.some((inst) => isSameDay(parseISO(inst.lessonDate), d));

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (text.trim() && selectedInstanceId) {
            onSubmit(text.trim(), selectedInstanceId);
            setText("");
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
                <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                    Дата урока
                </label>
                <Popover>
                    <PopoverTrigger asChild>
                        <Button
                            variant="outline"
                            className={cn(
                                "w-full justify-start text-left font-semibold bg-white/40 border-black/10 rounded-2xl h-12 text-sm",
                                !date && "text-black/30 font-normal"
                            )}
                        >
                            <CalendarIcon className="mr-2 h-4 w-4 text-[var(--red)]" />
                            {date
                                ? format(date, "d MMMM yyyy", { locale: ru })
                                : "Выберите дату урока"}
                        </Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0 bg-white rounded-2xl border-none shadow-2xl">
                        <Calendar
                            mode="single"
                            selected={date}
                            onSelect={(sel) => {
                                if (sel) {
                                    const inst = lessonInstances.find((i) =>
                                        isSameDay(parseISO(i.lessonDate), sel)
                                    );
                                    setSelectedInstanceId(inst?.id ?? null);
                                } else {
                                    setSelectedInstanceId(null);
                                }
                            }}
                            disabled={disabledDays}
                            initialFocus
                            locale={ru}
                            className="rounded-2xl"
                        />
                    </PopoverContent>
                </Popover>
            </div>

            <div className="space-y-2">
                <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                    Текст задания
                </label>
                <Textarea
                    placeholder="Введите домашнее задание..."
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    className="min-h-[110px] resize-none bg-white/40 border-black/10 rounded-2xl focus-visible:ring-[var(--red)] text-sm"
                    disabled={isSubmitting}
                />
            </div>

            <Button
                type="submit"
                disabled={!text.trim() || !selectedInstanceId || isSubmitting}
                className="w-full gap-2 bg-[var(--red)] hover:bg-[var(--red-dark)] text-white rounded-2xl py-6 text-sm font-bold shadow-lg shadow-[var(--red)]/20 transition-all active:scale-[0.98] disabled:opacity-40"
            >
                {isSubmitting ? "Создание..." : "Создать задание"}
                <Send className="w-4 h-4" />
            </Button>
        </form>
    );
}

// ─── Big Calendar ────────────────────────────────────────────────────────────

const WEEKDAYS = ["Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"];

function HomeworkCalendar({
    homeworks,
    lessonInstances,
    selectedDate,
    onSelectDate,
    currentMonth,
    onMonthChange,
}: {
    homeworks: HomeworkResponse[];
    lessonInstances: lessonInstance[];
    selectedDate: Date | null;
    onSelectDate: (d: Date) => void;
    currentMonth: Date;
    onMonthChange: (d: Date) => void;
}) {
    const hwByDay = useMemo(() => {
        const map = new Map<string, HomeworkResponse[]>();
        homeworks.forEach((hw) => {
            if (!hw.lessonInstance?.lessonDate) return;
            const key = hw.lessonInstance.lessonDate.slice(0, 10);
            if (!map.has(key)) map.set(key, []);
            map.get(key)!.push(hw);
        });
        return map;
    }, [homeworks]);

    const lessonDays = useMemo(
        () => new Set(lessonInstances.map((i) => i.lessonDate.slice(0, 10))),
        [lessonInstances]
    );

    const days = useMemo(() => {
        const start = startOfMonth(currentMonth);
        const end = endOfMonth(currentMonth);
        const monthDays = eachDayOfInterval({ start, end });

        // Monday-based offset
        const startDow = getDay(start);
        const offset = startDow === 0 ? 6 : startDow - 1;
        const prefixDays: (Date | null)[] = Array(offset).fill(null);

        const allCells = [...prefixDays, ...monthDays];
        // pad to full weeks
        while (allCells.length % 7 !== 0) allCells.push(null);
        return allCells;
    }, [currentMonth]);

    const prevMonth = () => {
        const d = new Date(currentMonth);
        d.setMonth(d.getMonth() - 1);
        onMonthChange(d);
    };
    const nextMonth = () => {
        const d = new Date(currentMonth);
        d.setMonth(d.getMonth() + 1);
        onMonthChange(d);
    };

    return (
        <div className="glass-card rounded-[32px] p-6 backdrop-blur-md h-full flex flex-col">
            {/* Month nav */}
            <div className="flex items-center justify-between mb-6">
                <button
                    onClick={prevMonth}
                    className="w-9 h-9 rounded-2xl bg-white/40 hover:bg-white/60 border border-black/5 flex items-center justify-center transition-all active:scale-95"
                >
                    <ChevronLeft className="w-4 h-4 text-[var(--navy)]" />
                </button>
                <h2 className="font-serif font-black text-lg text-[var(--navy)] tracking-tight capitalize">
                    {format(currentMonth, "LLLL yyyy", { locale: ru })}
                </h2>
                <button
                    onClick={nextMonth}
                    className="w-9 h-9 rounded-2xl bg-white/40 hover:bg-white/60 border border-black/5 flex items-center justify-center transition-all active:scale-95"
                >
                    <ChevronRight className="w-4 h-4 text-[var(--navy)]" />
                </button>
            </div>

            {/* Weekday headers */}
            <div className="grid grid-cols-7 mb-2">
                {WEEKDAYS.map((w) => (
                    <div
                        key={w}
                        className="text-center text-[10px] font-bold tracking-widest uppercase text-black/25 pb-2"
                    >
                        {w}
                    </div>
                ))}
            </div>

            {/* Day cells */}
            <div className="grid grid-cols-7 gap-1.5 flex-1">
                {days.map((day, idx) => {
                    if (!day) {
                        return <div key={`empty-${idx}`} />;
                    }
                    const key = toDateKey(day);
                    const hws = hwByDay.get(key) ?? [];
                    const hasLesson = lessonDays.has(key);
                    const isSelected =
                        selectedDate && isSameDay(day, selectedDate);
                    const isTodayCell = isToday(day);

                    return (
                        <button
                            key={key}
                            onClick={() => onSelectDate(day)}
                            className={cn(
                                "relative flex flex-col items-start rounded-2xl p-2 min-h-[72px] transition-all text-left border",
                                isSelected
                                    ? "bg-[var(--ink-dim)] border-transparent shadow-lg shadow-[var(--navy)]/20"
                                    : hws.length > 0
                                    ? "bg-white/50 border-black/5 hover:bg-white/70 hover:border-black/10"
                                    : "bg-white/20 border-transparent hover:bg-white/35 hover:border-black/5"
                            )}
                        >
                            {/* Day number */}
                            <span
                                className={cn(
                                    "text-xs font-bold mb-1.5 w-5 h-5 flex items-center justify-center rounded-full",
                                    isSelected
                                        ? "bg-white/20 text-white"
                                        : isTodayCell
                                        ? "bg-[var(--red)] text-white"
                                        : "text-[var(--navy)]"
                                )}
                            >
                                {format(day, "d")}
                            </span>

                            {/* Lesson dot */}
                            {hasLesson && hws.length === 0 && (
                                <div
                                    className={cn(
                                        "w-1.5 h-1.5 rounded-full mt-auto",
                                        isSelected ? "bg-white/40" : "bg-black/15"
                                    )}
                                />
                            )}

                            {/* HW chips */}
                            {hws.slice(0, 2).map((hw, i) => (
                                <div
                                    key={hw.id}
                                    className={cn(
                                        "w-full text-[9px] font-bold rounded-lg px-1.5 py-0.5 truncate leading-tight",
                                        isSelected
                                            ? i === 0
                                                ? "bg-white/25 text-white"
                                                : "bg-white/15 text-white/70"
                                            : i === 0
                                            ? "bg-[var(--red-light)] text-[var(--red)]"
                                            : "bg-blue-50 text-blue-600"
                                    )}
                                >
                                    Задание #{hw.id}
                                </div>
                            ))}
                            {hws.length > 2 && (
                                <div
                                    className={cn(
                                        "text-[9px] font-bold px-1",
                                        isSelected ? "text-white/50" : "text-black/30"
                                    )}
                                >
                                    +{hws.length - 2}
                                </div>
                            )}
                        </button>
                    );
                })}
            </div>
        </div>
    );
}

// ─── Day Detail Panel ────────────────────────────────────────────────────────

function DayDetailPanel({
    selectedDate,
    homeworks,
    lessonInstances,
    onCreateHomework,
    isSubmitting,
}: {
    selectedDate: Date | null;
    homeworks: HomeworkResponse[];
    lessonInstances: lessonInstance[];
    onCreateHomework: (text: string, id: number) => void;
    isSubmitting: boolean;
}) {
    const deleteMutation = useDeleteHomework(); // ← добавь

    const dayHws = useMemo(() => {
        if (!selectedDate) return [];
        const key = toDateKey(selectedDate);
        return homeworks.filter(
            (hw) => hw.lessonInstance?.lessonDate?.slice(0, 10) === key
        );
    }, [selectedDate, homeworks]);

    return (
        <div className="flex flex-col gap-5">
            <div className="glass-card rounded-[32px] p-6 backdrop-blur-md">
                <h2 className="text-base font-black text-[var(--navy)] flex items-center gap-2 mb-4">
                    <FileText className="w-4 h-4 text-[var(--red)]" />
                    {selectedDate
                        ? format(selectedDate, "d MMMM, EEEE", { locale: ru })
                        : "Выберите день"}
                </h2>

                {!selectedDate && (
                    <div className="text-center py-8 text-black/30">
                        <CalendarDays className="w-10 h-10 mx-auto mb-2 opacity-30" />
                        <p className="text-sm">Нажмите на дату в календаре</p>
                    </div>
                )}

                {selectedDate && dayHws.length === 0 && (
                    <div className="text-center py-8 text-black/30">
                        <Sparkles className="w-8 h-8 mx-auto mb-2 opacity-30" />
                        <p className="text-sm font-medium">Нет заданий</p>
                        <p className="text-xs mt-1">Добавьте задание ниже</p>
                    </div>
                )}

                {dayHws.length > 0 && (
                    <ScrollArea className="max-h-[280px] pr-2">
                        <div className="space-y-3">
                            {dayHws.map((hw) => (
                                <div
                                    key={hw.id}
                                    className="rounded-2xl bg-white/40 border border-black/5 p-4"
                                >
                                    <div className="flex items-center justify-between mb-2">
                                        <div className="flex items-center gap-1.5">
                                            <div className="w-1.5 h-1.5 rounded-full bg-[var(--red)]" />
                                            <span className="text-xs font-bold text-black/40 uppercase tracking-wider">
                                                Задание #{hw.id}
                                            </span>
                                        </div>
                                        {/* ← кнопка удаления */}
                                        <button
                                            onClick={() => deleteMutation.mutate(hw.id)}
                                            disabled={deleteMutation.isPending}
                                            className="w-7 h-7 rounded-xl flex items-center justify-center text-black/20 hover:text-[var(--red)] hover:bg-[var(--red-light)]/60 transition-all active:scale-90 disabled:opacity-40"
                                        >
                                            <Trash2 className="w-3.5 h-3.5" />
                                        </button>
                                    </div>
                                    <p className="text-sm text-[var(--navy)] leading-relaxed whitespace-pre-wrap">
                                        {hw.text}
                                    </p>
                                </div>
                            ))}
                        </div>
                    </ScrollArea>
                )}
            </div>

            {/* Create form card — без изменений */}
            <div className="glass-card rounded-[32px] p-6 backdrop-blur-md">
                <h2 className="text-base font-black text-[var(--navy)] flex items-center gap-2 mb-5">
                    <Plus className="w-4 h-4 text-[var(--red)]" />
                    Создать задание
                </h2>
                <CreateHomeworkForm
                    onSubmit={onCreateHomework}
                    isSubmitting={isSubmitting}
                    lessonInstances={lessonInstances}
                    preselectedDate={selectedDate}
                />
            </div>
        </div>
    );
}

// ─── Main page ───────────────────────────────────────────────────────────────

export default function HomeworkJournal() {
    const teacherId = 17;

    const { data: periods } = useGetAcademicPeriods();
    const [selectedPeriodId, setSelectedPeriodId] = useState<string>("");

    useEffect(() => {
        if (periods && periods.length > 0 && !selectedPeriodId) {
            const active = periods.find((p) => !p.isClosed) ?? periods[periods.length - 1];
            setSelectedPeriodId(active.id.toString());
        }
    }, [periods, selectedPeriodId]);

    const periodIdToFetch = selectedPeriodId ? parseInt(selectedPeriodId, 10) : null;

    const { data: assignments } = useTeachingAssignmentDetail(teacherId);
    const [selectedAssignmentId, setSelectedAssignmentId] = useState<string>("");

    useEffect(() => {
        if (assignments && assignments.length > 0 && !selectedAssignmentId) {
            setSelectedAssignmentId(assignments[0].teachingAssignmentId.toString());
        }
    }, [assignments, selectedAssignmentId]);

    const { data: lessonInstances = [] } = useLessonInstancesByTeachingAssignment(
        selectedAssignmentId ? parseInt(selectedAssignmentId) : 0,
        periodIdToFetch ?? 0
    );

    // Load all homeworks for calendar (large page size)
    const { data: pageData, isLoading: homeworksLoading } = useHomeworksByTeachingAssignment(
        selectedAssignmentId ? parseInt(selectedAssignmentId) : 0,
        0,
        500
    );

    const allHomeworks: HomeworkResponse[] = pageData?.content ?? [];

    const createMutation = useCreateHomework();

    const [selectedDate, setSelectedDate] = useState<Date | null>(null);
    const [currentMonth, setCurrentMonth] = useState<Date>(new Date());

    const handleCreateHomework = (text: string, lessonInstanceId: number) => {
        if (!selectedAssignmentId) return;
        createMutation.mutate({ text, lessonInstanceId });
    };

    const currentAssignment = assignments?.find(
        (a) => a.teachingAssignmentId.toString() === selectedAssignmentId
    );

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
            {/* ── Header ── */}
            <header className="mb-6 top-0 left-0 right-0 z-[100]">
                <div className="max-w-[1400px] mx-auto px-4 md:px-10 pt-6">
                    <div className="glass-card rounded-[24px] px-6 h-16 flex items-center justify-between border-none shadow-lg backdrop-blur-md">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 rounded-[14px] bg-[var(--red-light)]/60 flex items-center justify-center ring-1 ring-[var(--red)]/10">
                                <GraduationCap className="w-5 h-5 text-[var(--red)]" />
                            </div>
                            <span className="font-serif font-black text-[1.2rem] text-[var(--navy)] tracking-tight">
                                Домашние задания
                            </span>
                        </div>
                        <nav className="hidden lg:flex items-center gap-2">
                            <NavItem to="/teacher/journal" label="Табель" />
                            <NavItem to="/teacher/homework" label="Добавить ДЗ" />
                            <NavItem to="/teacher/classes" label="Мои классы" />
                        </nav>
                        <div className="flex items-center gap-4">
                            <div className="text-right hidden sm:block">
                                <p className="text-[13px] font-black text-[var(--navy)] leading-none mb-1">Алексей</p>
                                <p className="text-[9px] font-extrabold tracking-[0.2em] uppercase text-black/25">Преподаватель</p>
                            </div>
                            <div className="w-11 h-11 rounded-[15px] bg-[var(--navy-light)]/40 ring-1 ring-black/[0.05] flex items-center justify-center shadow-inner">
                                <span className="font-serif font-black text-[15px] text-[var(--navy)]">А</span>
                            </div>
                        </div>
                    </div>
                </div>
            </header>

            {/* ── Controls bar ── */}
            <div className="max-w-[1400px] mx-auto mb-6">
                <div className="glass-card rounded-[24px] p-5 flex flex-col lg:flex-row lg:items-center justify-between gap-5 border-none shadow-lg backdrop-blur-md">
                    <div className="flex items-center gap-4">
                        <div className="hidden sm:flex w-12 h-12 rounded-[18px] bg-[var(--red-light)]/60 items-center justify-center ring-1 ring-[var(--red)]/10">
                            <BookOpen className="w-6 h-6 text-[var(--red)]" />
                        </div>
                        <div>
                            <h1 className="font-serif font-black text-2xl lg:text-3xl text-[var(--navy)] tracking-tight">
                                Домашние задания
                            </h1>
                            {currentAssignment && (
                                <p className="text-sm text-black/40 mt-0.5">
                                    {currentAssignment.schoolClassName} · {currentAssignment.subjectName}
                                    {allHomeworks.length > 0 && (
                                        <span className="ml-2 text-black/25">· {allHomeworks.length} заданий</span>
                                    )}
                                </p>
                            )}
                        </div>
                    </div>

                    <div className="flex flex-wrap gap-3 items-center">
                        {/* Period select */}
                        <Select value={selectedPeriodId} onValueChange={setSelectedPeriodId}>
                            <SelectTrigger className="glass-pill w-[220px] h-11 font-bold text-[13px] rounded-2xl text-[var(--navy)] px-4 border-0 shadow-none">
                                <div className="flex items-center gap-2">
                                    <CalendarDays className="w-4 h-4 text-[var(--red)] shrink-0" />
                                    <SelectValue placeholder="Выберите четверть" />
                                </div>
                            </SelectTrigger>
                            <SelectContent className="rounded-2xl border border-white/60 shadow-2xl p-1 bg-white/90 backdrop-blur-2xl">
                                {periods?.map((p) => (
                                    <SelectItem
                                        key={p.id}
                                        value={p.id.toString()}
                                        className="font-bold text-[13px] text-[var(--navy)] py-2.5 px-3 rounded-xl cursor-pointer"
                                    >
                                        {p.name}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>

                        {/* Assignment select */}
                        <Select value={selectedAssignmentId} onValueChange={setSelectedAssignmentId}>
                            <SelectTrigger className="glass-pill h-11 px-5 text-[13px] font-bold rounded-2xl text-[var(--navy)] border-0 shadow-sm gap-2 min-w-[220px]">
                                <Users className="w-4 h-4 text-[var(--red)]" />
                                <SelectValue placeholder="Выберите группу" />
                            </SelectTrigger>
                            <SelectContent className="rounded-2xl border-none shadow-2xl bg-white/95 backdrop-blur-xl max-h-[350px]">
                                {assignments?.map((p) => (
                                    <SelectItem
                                        key={p.teachingAssignmentId}
                                        value={p.teachingAssignmentId.toString()}
                                        className="font-bold text-[13px] py-3 rounded-xl cursor-pointer"
                                    >
                                        <span className="text-[var(--red)] mr-1">{p.schoolClassName}</span>
                                        {" "}· {p.subjectName}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>
                </div>
            </div>

            {/* ── Main grid ── */}
            <div className="max-w-[1400px] mx-auto grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Calendar — 2 cols */}
                <div className="lg:col-span-2">
                    {homeworksLoading ? (
                        <div className="glass-card rounded-[32px] p-6 backdrop-blur-md h-full flex items-center justify-center min-h-[500px]">
                            <div className="animate-pulse text-black/30 font-medium">Загрузка...</div>
                        </div>
                    ) : (
                        <HomeworkCalendar
                            homeworks={allHomeworks}
                            lessonInstances={lessonInstances}
                            selectedDate={selectedDate}
                            onSelectDate={setSelectedDate}
                            currentMonth={currentMonth}
                            onMonthChange={setCurrentMonth}
                        />
                    )}
                </div>

                {/* Side panel — 1 col */}
                <div className="lg:col-span-1">
                    <div className="sticky top-6">
                        <DayDetailPanel
                            selectedDate={selectedDate}
                            homeworks={allHomeworks}
                            lessonInstances={lessonInstances}
                            onCreateHomework={handleCreateHomework}
                            isSubmitting={createMutation.isPending}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
}

// ─── NavItem ─────────────────────────────────────────────────────────────────

function NavItem({ to, label }: { to: string; label: string }) {
    return (
        <NavLink
            to={to}
            className={({ isActive }) =>
                cn(
                    "glass-pill px-5 h-10 flex items-center rounded-2xl text-[12px] font-extrabold uppercase tracking-wider transition-all",
                    isActive
                        ? "text-[var(--navy)] bg-white/40 shadow-sm"
                        : "text-black/30 hover:text-[var(--navy)] hover:bg-white/20"
                )
            }
        >
            {label}
        </NavLink>
    );
}