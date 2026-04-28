import React, { useState, useEffect } from "react";
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
    CalendarDays
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
import {
    Card,
    CardContent,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { ScrollArea } from "@/components/ui/scroll-area";
import {
    Pagination,
    PaginationContent,
    PaginationItem,
    PaginationLink,
    PaginationNext,
    PaginationPrevious,
    PaginationEllipsis,
} from "@/components/ui/pagination";
import { Calendar } from "@/components/ui/calendar";
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover";
import { format, isSameDay, isValid, parseISO } from "date-fns";
import { ru } from "date-fns/locale";
import type { HomeworkResponse, PageResponse } from "@/services/homework-service";
import { useLessonInstancesByTeachingAssignment } from "@/hooks/use-lesson-instances";
import type { lessonInstance } from "@/services/lesson-instance-service";
import { useAcademicPeriods } from "@/hooks/use-academic-period";

function HomeworkPagination({
    currentPage,
    totalPages,
    onPageChange,
}: {
    currentPage: number;
    totalPages: number;
    onPageChange: (page: number) => void;
}) {
    if (totalPages <= 1) return null;

    const getPageNumbers = () => {
        const delta = 2;
        const range: (number | string)[] = [];
        for (let i = 0; i < totalPages; i++) {
            if (
                i === 0 ||
                i === totalPages - 1 ||
                (i >= currentPage - delta && i <= currentPage + delta)
            ) {
                range.push(i);
            } else if (range[range.length - 1] !== "...") {
                range.push("...");
            }
        }
        return range;
    };

    return (
        <Pagination className="mt-4">
            <PaginationContent>
                <PaginationItem>
                    <PaginationPrevious
                        onClick={() => onPageChange(currentPage - 1)}
                        className={
                            currentPage === 0
                                ? "pointer-events-none opacity-50"
                                : "cursor-pointer"
                        }
                    />
                </PaginationItem>
                {getPageNumbers().map((page, idx) =>
                    typeof page === "number" ? (
                        <PaginationItem key={idx}>
                            <PaginationLink
                                isActive={page === currentPage}
                                onClick={() => onPageChange(page)}
                                className="cursor-pointer"
                            >
                                {page + 1}
                            </PaginationLink>
                        </PaginationItem>
                    ) : (
                        <PaginationItem key={idx}>
                            <PaginationEllipsis />
                        </PaginationItem>
                    )
                )}
                <PaginationItem>
                    <PaginationNext
                        onClick={() => onPageChange(currentPage + 1)}
                        className={
                            currentPage === totalPages - 1
                                ? "pointer-events-none opacity-50"
                                : "cursor-pointer"
                        }
                    />
                </PaginationItem>
            </PaginationContent>
        </Pagination>
    );
}

function CreateHomeworkForm({
    onSubmit,
    isSubmitting,
    lessonInstances,
}: {
    onSubmit: (text: string, lessonInstanceId: number) => void;
    isSubmitting: boolean;
    lessonInstances: lessonInstance[];
}) {
    const [text, setText] = useState("");
    const [selectedInstanceId, setSelectedInstanceId] = useState<number | null>(null);

    const selectedInstance = lessonInstances.find(i => i.id === selectedInstanceId);
    const date = selectedInstance ? parseISO(selectedInstance.lessonDate) : undefined;

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (text.trim() && selectedInstanceId) {
            onSubmit(text.trim(), selectedInstanceId);
            setText("");
            setSelectedInstanceId(null);
        }
    };

    const disabledDays = (calendarDate: Date) => {
        return !lessonInstances.some(inst =>
            isSameDay(parseISO(inst.lessonDate), calendarDate)
        );
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
                <label className="text-sm font-bold text-[var(--navy)]">
                    Дата урока
                </label>
                <Popover>
                    <PopoverTrigger asChild>
                        <Button
                            variant="outline"
                            className={cn(
                                "w-full justify-start text-left text-base font-bold bg-white/40 border-black/10 rounded-xl h-12",
                                !date && "text-muted-foreground font-normal"
                            )}
                        >
                            <CalendarIcon className="mr-2 h-5 w-5 text-[var(--red)]" />
                            {date ? (
                                format(date, "PPP", { locale: ru })
                            ) : (
                                <span>Выберите дату урока</span>
                            )}
                        </Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0 bg-white rounded-2xl border-none shadow-xl">
                        <Calendar
                            mode="single"
                            selected={date}
                            onSelect={(selectedDate) => {
                                if (selectedDate) {
                                    const inst = lessonInstances.find(i =>
                                        isSameDay(parseISO(i.lessonDate), selectedDate)
                                    );
                                    setSelectedInstanceId(inst?.id || null);
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
                <label className="text-sm font-bold text-[var(--navy)]">Текст задания</label>
                <Textarea
                    placeholder="Введите текст домашнего задания..."
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    className="min-h-[120px] resize-none bg-white/40 border-black/10 rounded-xl focus-visible:ring-[var(--red)] text-base"
                    disabled={isSubmitting}
                />
            </div>

            <Button
                type="submit"
                disabled={!text.trim() || !selectedInstanceId || isSubmitting}
                className="w-full gap-2 bg-[var(--red)] hover:bg-[var(--red-dark)] text-white rounded-xl py-6 text-md font-bold shadow-lg shadow-[var(--red)]/20 transition-all active:scale-[0.98]"
            >
                {isSubmitting ? "Создание..." : "Создать задание"}
                <Send className="w-5 h-5" />
            </Button>
        </form>
    );
}

function HomeworkList({
    pageData,
    isLoading,
}: {
    pageData: PageResponse<HomeworkResponse> | null;
    isLoading: boolean;
}) {
    if (isLoading && !pageData) {
        return (
            <div className="flex justify-center py-8">
                <div className="animate-pulse text-black/40">Загрузка заданий...</div>
            </div>
        );
    }

    if (!pageData || pageData.content.length === 0) {
        return (
            <div className="text-center py-12 text-black/40">
                <FileText className="w-16 h-16 mx-auto mb-3 opacity-30" />
                <p className="font-medium">Нет домашних заданий</p>
                <p className="text-sm">Добавьте задание через форму справа</p>
            </div>
        );
    }

    return (
        <ScrollArea className="h-[calc(100vh-320px)] pr-4">
            <div className="space-y-3">
                {pageData.content.map((hw) => {
                    const dateStr = hw.lessonInstance?.lessonDate;
                    const parsedDate = dateStr ? parseISO(dateStr) : new Date(NaN);
                    const dateIsValid = isValid(parsedDate);

                    return (
                        <Card
                            key={hw.id}
                            className="bg-white/30 backdrop-blur-sm border-none shadow-sm rounded-2xl"
                        >
                            <CardHeader className="pb-2 pt-3 px-4">
                                <div className="flex items-start justify-between">
                                    <CardTitle className="text-sm flex items-start gap-2">
                                        <FileText className="w-4 h-4 text-[var(--red)] mt-0.5 flex-shrink-0" />
                                        <span className="font-medium text-[var(--navy)]">
                                            Задание #{hw.id}
                                        </span>
                                    </CardTitle>
                                    <div className="text-sm text-black/55 flex items-center gap-1">
                                        <CalendarIcon className="w-3 h-3" />
                                        {dateIsValid
                                            ? format(parsedDate, "dd MMM yyyy", { locale: ru })
                                            : "—"}
                                    </div>
                                </div>
                            </CardHeader>
                            <CardContent className="pt-0 pb-3 px-4">
                                <p className="text-sm text-black/70 whitespace-pre-wrap">
                                    {hw.text}
                                </p>
                            </CardContent>
                        </Card>
                    );
                })}
            </div>
        </ScrollArea>
    );
}

export default function HomeworkJournal() {
    const teacherId = 17;

    const { data: periods } = useAcademicPeriods();
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
    const [currentPage, setCurrentPage] = useState(0);
    const pageSize = 10;

    const { data: lessonInstances = [] } = useLessonInstancesByTeachingAssignment(
        selectedAssignmentId ? parseInt(selectedAssignmentId) : 0,
        periodIdToFetch ?? 0
    );

    const {
        data: pageData,
        isLoading: homeworksLoading,
        isFetching
    } = useHomeworksByTeachingAssignment(
        selectedAssignmentId ? parseInt(selectedAssignmentId) : 0,
        currentPage,
        pageSize
    );

    const createMutation = useCreateHomework();

    useEffect(() => {
        setCurrentPage(0);
    }, [selectedAssignmentId]);

    const handleCreateHomework = (text: string, lessonInstanceId: number) => {
        if (!selectedAssignmentId) return;

        createMutation.mutate({
            text,
            lessonInstanceId,
        }, {
            onSuccess: () => {
                setCurrentPage(0);
            }
        });
    };

    const goToPage = (page: number) => {
        if (pageData && (page < 0 || page >= pageData.totalPages)) return;
        setCurrentPage(page);
    };

    useEffect(() => {
        if (assignments && assignments.length > 0 && !selectedAssignmentId) {
            setSelectedAssignmentId(
                assignments[0].teachingAssignmentId.toString()
            );
        }
    }, [assignments, selectedAssignmentId]);

    const currentAssignment = assignments?.find(
        (a) => a.teachingAssignmentId.toString() === selectedAssignmentId
    );

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
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
                            <NavItem
                                to="/teacher/homework"
                                label="Добавить ДЗ"
                            />
                            <NavItem
                                to="/teacher/classes"
                                label="Мои классы"
                            />
                        </nav>
                        <div className="flex items-center gap-4">
                            <div className="text-right hidden sm:block">
                                <p className="text-[13px] font-black text-[var(--navy)] leading-none mb-1">
                                    Алексей
                                </p>
                                <p className="text-[9px] font-extrabold tracking-[0.2em] uppercase text-black/25">
                                    Преподаватель
                                </p>
                            </div>
                            <div className="w-11 h-11 rounded-[15px] bg-[var(--navy-light)]/40 ring-1 ring-black/[0.05] flex items-center justify-center shadow-inner">
                                <span className="font-serif font-black text-[15px] text-[var(--navy)]">
                                    А
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </header>

            <div className="max-w-[1400px] mx-auto mb-8">
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
                                <p className="text-sm text-black/50 mt-1">
                                    {currentAssignment.schoolClassName} ·{" "}
                                    {currentAssignment.subjectName}
                                </p>
                            )}
                        </div>
                    </div>

                    <div className="flex gap-3 items-center">
                        <Select value={selectedPeriodId} onValueChange={setSelectedPeriodId}>
                            <SelectTrigger className="glass-pill w-[240px] h-11 font-bold text-[13px] rounded-2xl text-[var(--navy)] px-4 border-0 shadow-none">
                                <div className="flex items-center gap-2">
                                    <CalendarDays className="w-4 h-4 text-[var(--red)] shrink-0" />
                                    <SelectValue placeholder="Выберите четверть" />
                                </div>
                            </SelectTrigger>
                            <SelectContent className="rounded-2xl border border-white/60 shadow-2xl p-1 bg-white/90 backdrop-blur-2xl">
                                {periods?.map((p) => (
                                    <SelectItem key={p.id} value={p.id.toString()} className="font-bold text-[13px] text-[var(--navy)] py-2.5 px-3 rounded-xl cursor-pointer">
                                        {p.name}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>

                    <Select
                        value={selectedAssignmentId}
                        onValueChange={setSelectedAssignmentId}
                    >
                        <SelectTrigger className="glass-pill h-10 px-5 text-[12px] font-bold rounded-2xl text-[var(--navy)] border-0 shadow-sm gap-2 min-w-[220px]">
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
                                    <span className="text-[var(--red)] mr-1">
                                        {p.schoolClassName}
                                    </span>{" "}
                                    · {p.subjectName}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>

                </div>
            </div>

            <div className="max-w-[1400px] mx-auto grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2">
                    <div className="glass-card rounded-[32px] p-6 backdrop-blur-md h-full">
                        <div className="flex items-center justify-between mb-4">
                            <h2 className="text-xl font-black text-[var(--navy)] flex items-center gap-2">
                                <FileText className="w-5 h-5 text-[var(--red)]" />
                                Список заданий
                                {pageData && pageData.totalElements > 0 && (
                                    <span className="text-sm font-normal text-black/40 ml-2">
                                        (всего {pageData.totalElements})
                                    </span>
                                )}
                            </h2>
                            {isFetching && !homeworksLoading && (
                                <div className="text-xs text-black/40">
                                    Обновление...
                                </div>
                            )}
                        </div>
                        <HomeworkList
                            pageData={pageData ?? null}
                            isLoading={homeworksLoading}
                        />
                        {pageData && pageData.totalPages > 1 && (
                            <HomeworkPagination
                                currentPage={currentPage}
                                totalPages={pageData.totalPages}
                                onPageChange={goToPage}
                            />
                        )}
                    </div>
                </div>

                <div className="lg:col-span-1">
                    <div className="glass-card rounded-[32px] p-6 backdrop-blur-md sticky top-24">
                        <h2 className="text-xl font-black text-[var(--navy)] flex items-center gap-2 mb-5">
                            <Plus className="w-5 h-5 text-[var(--red)]" />
                            Создать задание
                        </h2>
                        <CreateHomeworkForm
                            onSubmit={handleCreateHomework}
                            isSubmitting={createMutation.isPending}
                            lessonInstances={lessonInstances}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
}

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

