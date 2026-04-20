import React from "react";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useDiaryLessonsByStudentIdAndDateRange } from "@/hooks/use-schedule";
import type { RootState } from "@/store";
import { useDispatch, useSelector } from "react-redux";
import { addDays } from "date-fns/addDays";
import { format } from "date-fns/format";
import { ru } from "date-fns/locale";
import { nextWeek, prevWeek } from "@/store/slices/scheduleSlice";

// ─── Constants ────────────────────────────────────────────────────────────────

const RUSSIAN_DAYS: Record<string, string> = {
  MONDAY:    "Понедельник",
  TUESDAY:   "Вторник",
  WEDNESDAY: "Среда",
  THURSDAY:  "Четверг",
  FRIDAY:    "Пятница",
  SATURDAY:  "Суббота",
  SUNDAY:    "Воскресенье",
};

const formatDateLabel = (dateStr: string) =>
  new Date(dateStr).toLocaleDateString("ru-RU", { day: "numeric", month: "long" });

const mapAttendanceStatus = (status?: string) => {
  if (status === "ABSENT")  return "Н";
  if (status === "EXCUSED") return "ОП";
  if (status === "SICK")    return "Б";
  return "";
};

// ─── Primitives ───────────────────────────────────────────────────────────────

function Chip({ children, className = "" }: { children: React.ReactNode; className?: string }) {
  return (
    <Badge
      variant="outline"
      className={`text-[10px] px-3 py-1 font-extrabold tracking-[0.2em] uppercase rounded-full ${className}`}
    >
      {children}
    </Badge>
  );
}

function AttendanceBadge({ status }: { status: string }) {
  if (!status) return null;
  const styles: Record<string, string> = {
    "Н":  "bg-red-50     text-red-500    ring-red-100",
    "ОП": "bg-amber-50   text-amber-500  ring-amber-100",
    "Б":  "bg-emerald-50 text-emerald-600 ring-emerald-100",
  };
  return (
    <span className={`w-[34px] h-[34px] rounded-[10px] flex items-center justify-center font-serif text-[15px] font-bold flex-shrink-0 ring-1 ring-black/[0.06] ${styles[status] ?? "bg-gray-50 text-gray-500"}`}>
      {status}
    </span>
  );
}

function GradeBadge({ grade }: { grade: number | null }) {
  if (!grade) return null;
  const styles: Record<number, string> = {
    5: "bg-emerald-50 text-emerald-600",
    4: "bg-amber-50   text-amber-500",
    3: "bg-red-50     text-red-500",
    2: "bg-red-50     text-red-600",
  };
  return (
    <span className={`w-[34px] h-[34px] rounded-[10px] flex items-center justify-center font-serif text-[17px] font-bold flex-shrink-0 ring-1 ring-black/[0.06] ${styles[grade] ?? "bg-gray-50 text-gray-500"}`}>
      {grade}
    </span>
  );
}

// ─── Day Card ─────────────────────────────────────────────────────────────────

function DayCard({ dateKey, lessons }: { dateKey: string; lessons: any[] }) {
  const today = new Date().toISOString().split("T")[0];
  const isToday = dateKey === today;

  return (
    <div className={`col-span-12 md:col-span-6 glass-card rounded-[22px] p-6 anim-in transition-transform hover:-translate-y-0.5 ${isToday ? "ring-2 ring-[var(--red)]/20" : ""}`}>
      {/* Card header */}
      <div className="flex justify-between items-center mb-5">
        <Chip className={isToday
          ? "border-[var(--red)]/30 text-[var(--red)] bg-[var(--red-light)]/60"
          : "border-[var(--navy)]/20 text-[var(--navy)] bg-[var(--navy-light)]/30"
        }>
          {isToday ? `${RUSSIAN_DAYS[lessons[0]?.dayOfWeek] || "День"} · сегодня` : RUSSIAN_DAYS[lessons[0]?.dayOfWeek] || "День"}
        </Chip>
        <p className="text-[10px] font-bold text-black/25 uppercase tracking-[0.18em]">
          {formatDateLabel(dateKey)}
        </p>
      </div>

      {/* Lessons */}
      <div className="divide-y divide-black/[0.05]">
        {lessons.length > 0 ? lessons.map((lesson, idx) => (
          <div key={idx} className="py-3 first:pt-0 last:pb-0">
            <div className="flex items-start gap-3">
              {/* Lesson number */}
              <span className="font-serif text-[1.4rem] font-black text-black/10 leading-none min-w-[24px] mt-0.5">
                {lesson.lessonNumber}
              </span>

              <div className="flex-1 min-w-0">
                <div className="flex justify-between items-start gap-2">
                  <div className="min-w-0">
                    <p className="font-bold text-[13px] text-[var(--navy)] leading-tight">
                      {lesson.subjectName}
                    </p>
                    {lesson.homeworks && lesson.homeworks.length > 0 && (
                      <p className="text-[12px] text-black/35 mt-1 italic leading-snug line-clamp-2">
                        {lesson.homeworks[0].text}
                      </p>
                    )}
                  </div>

                  <div className="flex items-center gap-2 shrink-0">
                    <AttendanceBadge status={mapAttendanceStatus(lesson.attendance?.status)} />
                    <GradeBadge grade={lesson.grades[0]?.value || null} />
                  </div>
                </div>
              </div>
            </div>
          </div>
        )) : (
          <p className="py-3 text-[13px] text-black/25 italic">Занятий не запланировано</p>
        )}
      </div>
    </div>
  );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function Diary() {
  const dispatch = useDispatch();
  const currentWeekStartISO = useSelector((state: RootState) => state.schedule.currentWeekStart);
  const currentWeekStart = new Date(currentWeekStartISO);

  const startDate = format(currentWeekStart, "yyyy-MM-dd");
  const endDate   = format(addDays(currentWeekStart, 6), "yyyy-MM-dd");

  const { data, isLoading } = useDiaryLessonsByStudentIdAndDateRange(1, startDate, endDate);

  const weekEnd          = addDays(currentWeekStart, 6);
  const startDay         = format(currentWeekStart, "dd");
  const endDayWithMonth  = format(weekEnd, "dd MMM", { locale: ru });
  const fullMonthYear    = format(currentWeekStart, "LLLL yyyy", { locale: ru });
  const capitalizedMonth = fullMonthYear.charAt(0).toUpperCase() + fullMonthYear.slice(1);

  const sortedDays = data
    ? Object.entries(data).sort((a, b) => a[0].localeCompare(b[0]))
    : [];

  return (
    <div className="relative z-10 min-h-screen px-6 md:px-10 pt-28 pb-14">

      {/* ── Header ── */}
      <header className="flex items-end justify-between mb-10 pb-6 border-b border-black/[0.08] max-w-7xl mx-auto anim-in">
        <div>
          <p className="text-[10px] font-extrabold tracking-[0.25em] text-[var(--red)] uppercase mb-2 flex items-center gap-2">
            <span className="inline-block w-4 h-[2px] bg-[var(--red)] rounded-full" />
            Академический год 25/26
          </p>
          <h1 className="font-serif font-black text-[clamp(2rem,4vw,3rem)] text-[var(--navy)] leading-[0.95]">
            Учебный{" "}
            <em className="not-italic relative">
              <span className="relative z-10 text-[var(--red)]">дневник</span>
              <span className="absolute bottom-0 left-0 right-0 h-[5px] rounded-full opacity-15 bg-[var(--red)]" />
            </em>
          </h1>
        </div>

        {/* Week navigator */}
        <div className="flex items-center gap-3">
          <Button
            onClick={() => dispatch(prevWeek())}
            variant="outline"
            size="icon"
            className="glass-pill h-10 w-10 border-0 rounded-[12px] text-[var(--navy)] hover:scale-105 transition-transform"
          >
            <ChevronLeft className="h-4 w-4" />
          </Button>

          <div className="text-center min-w-[110px]">
            <p className="text-[9px] font-extrabold uppercase text-black/25 tracking-[0.2em]">
              {capitalizedMonth}
            </p>
            <p className="font-serif text-[17px] font-black text-[var(--navy)] leading-tight">
              {startDay} — {endDayWithMonth}
            </p>
          </div>

          <Button
            onClick={() => dispatch(nextWeek())}
            variant="outline"
            size="icon"
            className="glass-pill h-10 w-10 border-0 rounded-[12px] text-[var(--navy)] hover:scale-105 transition-transform"
          >
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
      </header>

      {/* ── Grid ── */}
      <main className="grid grid-cols-12 gap-4 max-w-7xl mx-auto">
        {isLoading
          ? Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className={`col-span-12 md:col-span-6 glass-card rounded-[22px] p-6 anim-in anim-delay-${(i % 5) + 1}`}>
                <Skeleton className="h-5 w-24 rounded-full mb-5" />
                <div className="flex flex-col gap-4">
                  <Skeleton className="h-4 w-full rounded-lg" />
                  <Skeleton className="h-4 w-3/4 rounded-lg" />
                  <Skeleton className="h-4 w-5/6 rounded-lg" />
                </div>
              </div>
            ))
          : sortedDays.map(([dateKey, lessons]) => (
              <DayCard key={dateKey} dateKey={dateKey} lessons={lessons} />
            ))
        }
      </main>
    </div>
  );
}