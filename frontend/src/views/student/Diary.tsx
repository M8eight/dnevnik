import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useDiaryScheduleByStudentId } from "@/hooks/use-schedule";
import type { RootState } from "@/store";
import { useDispatch, useSelector } from "react-redux";
import { addDays } from "date-fns/addDays";
import { format } from "date-fns/format";
import { ru } from "date-fns/locale";
import { nextWeek, prevWeek } from "@/store/slices/scheduleSlice";
import Chip from "@/components/student/chip";
import { RUSSIAN_DAYS } from "@/constants/component-constants";
import { AttendanceBadge, GradeBadge } from "@/components/student/diary/badges";
import StudentNavbar from "@/components/layout/navbars/StudentNavbar";
import type { DiaryScheduleDto } from "@/services/schedule-service";

const DAY_ORDER = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];

const LESSON_TIMES: Record<number, string> = {
  1: "8:00–8:45",
  2: "9:00–9:45",
  3: "10:00–10:45",
  4: "11:00–11:45",
  5: "12:00–12:45",
  6: "13:00–13:45",
  7: "14:00–14:45",
  8: "15:00–15:45",
};

const formatDateLabel = (dateStr: string) =>
  new Date(dateStr).toLocaleDateString("ru-RU", { day: "numeric", month: "long" });

const mapAttendanceStatus = (status?: string) => {
  if (status === "ABSENT") return "Н";
  if (status === "EXCUSED") return "ОП";
  if (status === "LATE") return "О";
  return "";
};

// вычисляем дату дня по началу недели и dayOfWeek
function lessonDate(weekStart: Date, dayOfWeek: string): string {
  const idx = DAY_ORDER.indexOf(dayOfWeek);
  return format(addDays(weekStart, idx), "yyyy-MM-dd");
}

function groupByDay(lessons: DiaryScheduleDto[]): Record<string, DiaryScheduleDto[]> {
  return lessons.reduce((acc, lesson) => {
    const key = lesson.dayOfWeek;
    if (!acc[key]) acc[key] = [];
    acc[key].push(lesson);
    return acc;
  }, {} as Record<string, DiaryScheduleDto[]>);
}

function DayCard({
  dayOfWeek,
  date,
  lessons,
}: {
  dayOfWeek: string;
  date: string;
  lessons: DiaryScheduleDto[];
}) {
  const today = new Date().toISOString().split("T")[0];
  const isToday = date === today;

  return (
    <div className={`col-span-12 md:col-span-6 glass-card rounded-[22px] p-6 anim-in transition-transform hover:-translate-y-0.5 ${isToday ? "ring-2 ring-(--red)/20" : ""}`}>

      <div className="flex justify-between items-center mb-5">
        <Chip className={isToday
          ? "border-(--red)/30 text-(--red) bg-(--red-light)/60"
          : "border-(--navy)/20 text-(--navy) bg-(--navy-light)/30"
        }>
          {isToday ? `${RUSSIAN_DAYS[dayOfWeek] || "День"} · сегодня` : RUSSIAN_DAYS[dayOfWeek] || "День"}
        </Chip>
        <p className="text-[10px] font-bold text-black/25 uppercase tracking-[0.18em]">
          {formatDateLabel(date)}
        </p>
      </div>

      <div className="divide-y divide-black/5">
        {lessons.map((lesson, idx) => {
          const attendance = lesson.instance?.attendances?.[0];
          const grade = lesson.instance?.grades?.[0];
          const homework = lesson.instance?.homework;

          return (
            <div key={idx} className="py-3 first:pt-0 last:pb-0">
              <div className="flex items-start gap-3">

                {/* Время урока */}
                <div className="flex flex-col justify-center items-end min-w-14 pt-1 shrink-0">
                  <span className="text-[13px] font-extrabold text-black/30 leading-none tabular-nums">
                    {LESSON_TIMES[lesson.lessonNumber]?.split("–")[0] ?? "—"}
                  </span>
                  <span className="text-[12px] font-medium text-black/15 leading-none tabular-nums">
                    {LESSON_TIMES[lesson.lessonNumber]?.split("–")[1] ?? ""}
                  </span>
                </div>

                {/* Разделитель */}
                <div className="w-0.5 self-stretch rounded-full bg-black/6 shrink-0" />

                {/* Контент урока */}
                <div className="flex-1 min-w-0">
                  <div className="flex justify-between items-start gap-2">
                    <div className="min-w-0">
                      <p className="font-bold text-[13px] text-(--navy) leading-tight">
                        {lesson.subject?.name ?? "—"}
                      </p>
                      <p className="text-[11px] text-black/20 mt-0.5">
                        {lesson.classRoom}
                      </p>
                      {homework && (
                        <p className="text-[12px] text-black/35 mt-1 italic leading-snug line-clamp-2">
                          {homework.text}
                        </p>
                      )}
                    </div>
                    <div className="flex items-center gap-2 shrink-0">
                      <AttendanceBadge status={mapAttendanceStatus(attendance?.status)} />
                      <GradeBadge grade={grade?.value ?? null} />
                    </div>
                  </div>
                </div>

              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default function Diary() {
  const dispatch = useDispatch();
  const currentWeekStartISO = useSelector((state: RootState) => state.schedule.currentWeekStart);
  const currentWeekStart = new Date(currentWeekStartISO);

  const startDate = format(currentWeekStart, "yyyy-MM-dd");
  const endDate = format(addDays(currentWeekStart, 6), "yyyy-MM-dd");

  const { data, isLoading } = useDiaryScheduleByStudentId(startDate, endDate);

  const weekEnd = addDays(currentWeekStart, 6);
  const startDay = format(currentWeekStart, "dd");
  const endDayWithMonth = format(weekEnd, "dd MMM", { locale: ru });
  const fullMonthYear = format(currentWeekStart, "LLLL yyyy", { locale: ru });
  const capitalizedMonth = fullMonthYear.charAt(0).toUpperCase() + fullMonthYear.slice(1);

  const grouped = data ? groupByDay(data) : {};
  const sortedDays = DAY_ORDER
    .filter(day => grouped[day])
    .map(day => ({
      dayOfWeek: day,
      date: lessonDate(currentWeekStart, day),
      lessons: grouped[day].sort((a, b) => a.lessonNumber - b.lessonNumber),
    }));

  return (
    <div className="relative z-10 min-h-screen px-6 md:px-10 pt-2 pb-14">
      <StudentNavbar />

      <header className="flex items-end justify-between mb-10 pb-6 border-b border-black/8 max-w-7xl mx-auto anim-in">
        <div>
          <p className="text-[10px] font-extrabold tracking-[0.25em] text-(--red) uppercase mb-2 flex items-center gap-2">
            <span className="inline-block w-4 h-0.5 bg-(--red) rounded-full" />
            Академический год 25/26
          </p>
          <h1 className="font-serif font-black text-[clamp(2rem,4vw,3rem)] text-(--navy) leading-[0.95]">
            Учебный{" "}
            <em className="not-italic relative">
              <span className="relative z-10 text-(--red)">дневник</span>
              <span className="absolute bottom-0 left-0 right-0 h-1.25 rounded-full opacity-15 bg-(--red)" />
            </em>
          </h1>
        </div>

        <div className="flex items-center gap-3">
          <Button onClick={() => dispatch(prevWeek())} variant="outline" size="icon"
            className="glass-pill h-10 w-10 border-0 rounded-[12px] text-(--navy) hover:scale-105 transition-transform">
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <div className="text-center min-w-27.5">
            <p className="text-[9px] font-extrabold uppercase text-black/25 tracking-[0.2em]">
              {capitalizedMonth}
            </p>
            <p className="font-serif text-[17px] font-black text-(--navy) leading-tight">
              {startDay} — {endDayWithMonth}
            </p>
          </div>
          <Button onClick={() => dispatch(nextWeek())} variant="outline" size="icon"
            className="glass-pill h-10 w-10 border-0 rounded-[12px] text-(--navy) hover:scale-105 transition-transform">
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
      </header>

      <main className="grid grid-cols-12 gap-4 max-w-7xl mx-auto">
        {isLoading
          ? Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="col-span-12 md:col-span-6 glass-card rounded-[22px] p-6 anim-in">
              <Skeleton className="h-5 w-24 rounded-full mb-5" />
              <div className="flex flex-col gap-4">
                <Skeleton className="h-4 w-full rounded-lg" />
                <Skeleton className="h-4 w-3/4 rounded-lg" />
                <Skeleton className="h-4 w-5/6 rounded-lg" />
              </div>
            </div>
          ))
          : sortedDays.map(({ dayOfWeek, date, lessons }) => (
            <DayCard key={dayOfWeek} dayOfWeek={dayOfWeek} date={date} lessons={lessons} />
          ))
        }
      </main>
    </div>
  );
}