import Chip from "@/components/student/chip";
import { calculateWeightedAvg, avgColor, formatAvg, formatDate } from "@/components/student/grades-page/grades-page-helper";
import { TableHeader, TableRow, TableHead, TableBody, TableCell } from "@/components/ui/table";
import React, { useRef, useState, useEffect, useCallback, useLayoutEffect } from "react";
import GradeBadge from "@/components/student/grades-page/grade-badge";
import StatCard from "@/components/student/grades-page/stat-card";
import { Award, BookOpen, Star, TrendingUp } from "lucide-react";
import { useGradesLessonsByStudentId } from "@/hooks/use-grade";
import { useHorizontalScrollDrag } from "@/helpers/teacher-helpers";

export interface GradeJournalGradeTabProps {
    academicPeriodId: number;
}

type GradesResponse = NonNullable<ReturnType<typeof useGradesLessonsByStudentId>['data']>;

function CustomScrollbar({ scrollRef }: { scrollRef: React.RefObject<HTMLDivElement | null> }) {
    const trackRef = useRef<HTMLDivElement>(null);
    const [thumb, setThumb] = useState({ widthPct: 100, leftPct: 0 });
    const [visible, setVisible] = useState(false);
    const draggingRef = useRef(false);

    const recompute = useCallback(() => {
        const el = scrollRef.current;
        if (!el) return;

        const { scrollWidth, clientWidth, scrollLeft } = el;
        const maxScroll = scrollWidth - clientWidth;

        if (maxScroll <= 1) {
            setVisible(false);
            return;
        }

        setVisible(true);
        const widthPct = Math.max((clientWidth / scrollWidth) * 100, 4);
        const leftPct = (scrollLeft / maxScroll) * (100 - widthPct);
        setThumb({ widthPct, leftPct });
    }, [scrollRef]);

    useEffect(() => {
        const el = scrollRef.current;
        if (!el) return;

        el.addEventListener("scroll", recompute, { passive: true });

        const resizeObserver = new ResizeObserver(recompute);
        resizeObserver.observe(el);

        window.addEventListener("resize", recompute);

        return () => {
            el.removeEventListener("scroll", recompute);
            resizeObserver.disconnect();
            window.removeEventListener("resize", recompute);
        };
    }, [recompute, scrollRef]);

    const onThumbMouseDown = (e: React.MouseEvent) => {
        const el = scrollRef.current;
        if (!el) return;
        e.preventDefault();
        draggingRef.current = true;

        const startX = e.clientX;
        const startScrollLeft = el.scrollLeft;

        const onMove = (ev: MouseEvent) => {
            const el = scrollRef.current;
            if (!el || !draggingRef.current || !trackRef.current) return;

            const { scrollWidth, clientWidth } = el;
            const maxScroll = scrollWidth - clientWidth;
            const trackWidth = trackRef.current.clientWidth;

            const deltaX = ev.clientX - startX;
            const scrollDelta = (deltaX / trackWidth) * scrollWidth;

            el.scrollLeft = Math.max(0, Math.min(maxScroll, startScrollLeft + scrollDelta));
        };

        const onUp = () => {
            draggingRef.current = false;
            window.removeEventListener("mousemove", onMove);
            window.removeEventListener("mouseup", onUp);
        };

        window.addEventListener("mousemove", onMove);
        window.addEventListener("mouseup", onUp);
    };

    const onTrackMouseDown = (e: React.MouseEvent) => {
        const el = scrollRef.current;
        if (!el || !trackRef.current || e.target !== trackRef.current) return;

        const { scrollWidth, clientWidth } = el;
        const trackRect = trackRef.current.getBoundingClientRect();
        const clickRatio = (e.clientX - trackRect.left) / trackRect.width;

        el.scrollTo({ left: clickRatio * scrollWidth - clientWidth / 2, behavior: "smooth" });
    };

    if (!visible) return null;

    return (
        <div className="px-3 pb-2.5 pt-1.5">
            <div
                ref={trackRef}
                onMouseDown={onTrackMouseDown}
                className="relative h-2 w-full rounded-full bg-black/4 cursor-pointer"
            >
                <div
                    onMouseDown={onThumbMouseDown}
                    className="absolute top-0 h-2 rounded-full bg-(--navy)/35 hover:bg-(--navy)/50 active:bg-(--navy)/60 transition-colors cursor-grab active:cursor-grabbing"
                    style={{ width: `${thumb.widthPct}%`, left: `${thumb.leftPct}%` }}
                />
            </div>
        </div>
    );
}

function GradeTableScrollArea({
    response,
    filteredDates,
    scrollRef,
}: {
    response: GradesResponse;
    filteredDates: string[];
    scrollRef: React.RefObject<HTMLDivElement | null>;
}) {
    const dateHeaderRefs = useRef<Record<string, HTMLTableCellElement | null>>({});


    useHorizontalScrollDrag(scrollRef);

    useLayoutEffect(() => {
        const el = scrollRef.current;
        if (!el || filteredDates.length === 0) return;

        const today = new Date();
        const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;

        const targetDate = [...filteredDates].filter((d) => d <= todayStr).pop() ?? filteredDates[0];
        const targetEl = dateHeaderRefs.current[targetDate];
        if (!targetEl) return;

        const stickyColWidth = el.querySelector<HTMLElement>("[data-sticky-col]")?.getBoundingClientRect().width ?? 0;
        const offset = targetEl.offsetLeft - stickyColWidth - 24; // небольшой отступ, чтобы дата не липла к sticky-столбцу
        el.scrollLeft = Math.max(0, offset);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [filteredDates.length]);

    return (
        <div
            ref={scrollRef}
            className="w-full min-w-0 overflow-x-auto cursor-grab [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
        >
            <table className="border-collapse w-max min-w-full">
                <TableHeader>
                    <TableRow className="hover:bg-transparent border-black/5 bg-black/1.5">
                        <TableHead
                            data-sticky-col
                            className="w-50 pl-7 h-12 sticky left-0 top-0 bg-white/90 backdrop-blur-sm z-40 border-r border-black/5 font-extrabold text-[9px] uppercase tracking-[0.22em] text-black/30"
                        >
                            Предмет
                        </TableHead>
                        {filteredDates.map((date) => (
                            <TableHead
                                key={date}
                                ref={(node) => { dateHeaderRefs.current[date] = node; }}
                                className="text-center min-w-19 px-1 font-bold text-[10px] text-black/25 border-r border-black/4 sticky top-0 z-30 bg-white/90 backdrop-blur-sm"
                            >
                                {formatDate(date)}
                            </TableHead>
                        ))}
                        <TableHead className="w-22 sticky right-0 top-0 z-40 bg-white/90 backdrop-blur-sm border-l border-black/[0.07] text-center font-extrabold text-[9px] uppercase tracking-[0.18em] text-(--red)">
                            Средний
                        </TableHead>
                    </TableRow>
                </TableHeader>

                <TableBody>
                    {response.subjects.map((sub) => {
                        const weightedAvg = calculateWeightedAvg(sub.grades);

                        return (
                            <TableRow
                                key={sub.subject}
                                className="border-black/4 transition-colors group h-14.5 hover:bg-black/1.5"
                            >
                                <TableCell className="pl-7 py-0 font-bold text-[13px] text-(--navy) sticky left-0 bg-white/85 backdrop-blur-sm z-20 border-r border-black/5 group-hover:bg-amber-50/50 transition-colors">
                                    {sub.subject}
                                </TableCell>

                                {filteredDates.map((date) => {
                                    const dayGrades = sub.grades.filter((g) => g.date === date);
                                    return (
                                        <TableCell
                                            key={date}
                                            className="p-0 text-center border-r border-black/3 min-w-19"
                                        >
                                            <div className="flex items-center justify-center h-14.5 px-1">
                                                {dayGrades.length > 0 ? (
                                                    <div className={`flex items-center justify-center ${dayGrades.length > 1 ? "gap-0.5" : ""}`}>
                                                        {dayGrades.map((g, idx) => (
                                                            <React.Fragment key={g.gradeId}>
                                                                <GradeBadge
                                                                    grade={g.value}
                                                                    size={dayGrades.length > 1 ? "sm" : "md"}
                                                                />
                                                                {dayGrades.length > 1 && idx === 0 && (
                                                                    <span className="text-black/15 font-light -mx-px select-none">
                                                                        /
                                                                    </span>
                                                                )}
                                                            </React.Fragment>
                                                        ))}
                                                    </div>
                                                ) : (
                                                    <GradeBadge />
                                                )}
                                            </div>
                                        </TableCell>
                                    );
                                })}

                                <TableCell className="sticky right-0 z-20 border-l border-black/[0.07] p-0 bg-white/85 backdrop-blur-sm group-hover:bg-amber-50/60 transition-colors">
                                    <div className="flex items-center justify-center h-14.5">
                                        <span className={`font-serif text-[20px] font-black ${avgColor(weightedAvg)}`}>
                                            {formatAvg(weightedAvg)}
                                        </span>
                                    </div>
                                </TableCell>
                            </TableRow>
                        );
                    })}
                </TableBody>
            </table>
        </div>
    );
}

function GradeTableCard({ response, filteredDates }: {
    response: GradesResponse;
    filteredDates: string[];
}) {
    const scrollRef = useRef<HTMLDivElement>(null);

    return (
        <div className="glass-card rounded-[28px] overflow-hidden anim-in anim-delay-5">
            <div className="flex items-center justify-between px-7 pt-6 pb-4 border-b border-black/5">
                <Chip className="border-(--navy)/20 text-(--navy) bg-(--navy-light)/30">
                    Электронный журнал
                </Chip>
                <span className="text-[10px] font-bold text-black/20 uppercase tracking-widest">
                    {response.academicPeriod.academicYear.name}
                </span>
            </div>

            <GradeTableScrollArea response={response} filteredDates={filteredDates} scrollRef={scrollRef} />
            <CustomScrollbar scrollRef={scrollRef} />
        </div>
    );
}

export default function GradeJournalGradeTab({ academicPeriodId }: GradeJournalGradeTabProps) {
    const { data: response, isLoading, isError } = useGradesLessonsByStudentId(academicPeriodId);

    const today = new Date();
    const localTodayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;

    const filteredDates = response?.dates.filter((date) => {
        if (date <= localTodayStr) return true;
        return response.subjects.some((sub) => sub.grades.some((g) => g.date === date));
    }) ?? [];

    const allCurrentGrades = response?.subjects.flatMap((s) => s.grades) ?? [];
    const overallAvg = allCurrentGrades.length
        ? allCurrentGrades.reduce((a, g) => a + g.value, 0) / allCurrentGrades.length
        : 0;
    const excellentCount = allCurrentGrades.filter((g) => g.value === 5).length;

    if (isLoading) {
        return (
            <div className="glass-card rounded-[28px] overflow-hidden anim-in anim-delay-5 p-8 text-center">
                <p className="text-black/40">Загружаем данные...</p>
            </div>
        );
    }

    if (isError || !response) {
        return (
            <div className="glass-card rounded-[28px] overflow-hidden anim-in anim-delay-5 p-8 text-center">
                <p className="text-red-500">Ошибка при загрузке данных</p>
            </div>
        );
    }

    return (
        <>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
                <StatCard
                    icon={TrendingUp}
                    label="Средний балл"
                    value={overallAvg ? parseFloat(overallAvg.toFixed(2)).toString() : "—"}
                    sub="за четверть"
                    accent="var(--red)"
                    delay="anim-delay-1"
                />
                <StatCard
                    icon={Star}
                    label="Пятёрок"
                    value={excellentCount.toString()}
                    sub={`из ${allCurrentGrades.length} оценок`}
                    accent="var(--gold)"
                    delay="anim-delay-2"
                />
                <StatCard
                    icon={BookOpen}
                    label="Предметов"
                    value={response.subjects.length.toString()}
                    sub="в этой четверти"
                    accent="var(--navy)"
                    delay="anim-delay-3"
                />
                <StatCard
                    icon={Award}
                    label="Дней занятий"
                    value={filteredDates.length.toString()}
                    sub="в журнале"
                    accent="var(--green)"
                    delay="anim-delay-4"
                />
            </div>

            <GradeTableCard response={response} filteredDates={filteredDates} />
        </>
    );
}