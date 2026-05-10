import { useState, useEffect } from "react";
import {
    BookOpen,
    Users,
    CalendarDays,
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
import type { HomeworkResponse } from "@/services/homework-service";
import { useLessonInstancesByTeachingAssignment } from "@/hooks/use-lesson-instances";
import { useGetAcademicPeriods } from "@/hooks/use-academic-period";
import HomeworkCalendar from "@/components/teacher/homework-journal/homework-calendar";
import DayDetailPanel from "@/components/teacher/homework-journal/day-detail-panel";
import TeacherNavbar from "@/templates/navbars/TeacherNavbar";


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
            {/* ── Header & controls ── */}
            <TeacherNavbar />

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