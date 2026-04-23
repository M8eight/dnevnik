import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Skeleton } from "@/components/ui/skeleton";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { useUser } from "@/hooks/use-user";
import { useAvgGrade, useGradesByDate } from "@/hooks/use-grade";
import { useScheduleByDate, useScheduleByStudentId } from "@/hooks/use-schedule";
import { useHomeworkByDate } from "@/hooks/use-homework";
import type { User } from "@/services/user-service";

const DAYS_MAP = [
  { key: "MONDAY",    label: "Пн" },
  { key: "TUESDAY",   label: "Вт" },
  { key: "WEDNESDAY", label: "Ср" },
  { key: "THURSDAY",  label: "Чт" },
  { key: "FRIDAY",    label: "Пт" },
];

// ─── Shared primitives (same as GradesPage) ───────────────────────────────────

function Chip({ children, className = "" }: { children: React.ReactNode; className?: string }) {
  return (
    <Badge
      variant="outline"
      className={`text-[10px] px-3 py-1 font-extrabold tracking-[0.2em] uppercase rounded-full mb-3 ${className}`}
    >
      {children}
    </Badge>
  );
}

function GradeBadge({ grade }: { grade: number }) {
  const styles: Record<number, string> = {
    5: "bg-emerald-50 text-emerald-600 ring-emerald-100",
    4: "bg-amber-50   text-amber-500   ring-amber-100",
    3: "bg-red-50     text-red-500     ring-red-100",
    2: "bg-red-50     text-red-600     ring-red-100",
  };
  return (
    <span className={`w-[36px] h-[36px] rounded-[10px] flex items-center justify-center font-serif text-[17px] font-bold flex-shrink-0 ring-1 ring-black/[0.06] ${styles[grade] ?? "bg-gray-50 text-gray-500"}`}>
      {grade}
    </span>
  );
}

function CurrentDate() {
  const now = new Date();
  const dayName = now.toLocaleDateString("ru-RU", { weekday: "long" });
  const day = now.getDate();
  const monthYear = now.toLocaleDateString("ru-RU", { month: "long", year: "numeric" });
  const monthYearCap = monthYear.charAt(0).toUpperCase() + monthYear.slice(1);

  return (
    <div className="text-right text-[10px] font-extrabold text-black/30 uppercase tracking-[0.2em]">
      {dayName}
      <strong className="block font-serif text-[2rem] font-black text-[var(--navy)] normal-case tracking-normal leading-tight">
        {day}
      </strong>
      {monthYearCap}
    </div>
  );
}

function subjectColor(name: string): string {
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
  return `hsl(${Math.abs(hash) % 360}, 55%, 45%)`;
}

// ─── Sub-cards ────────────────────────────────────────────────────────────────

function UserCard({ user }: { user: User }) {
  return (
    <div className="col-span-12 md:col-span-6 glass-card rounded-[22px] p-7 relative overflow-hidden anim-in anim-delay-1">
      {/* Decorative circle */}
      <div className="absolute top-5 right-6 w-20 h-20 rounded-full border-[3px] border-[var(--red)] opacity-[0.08]" />
      <div className="absolute top-8 right-9 w-10 h-10 rounded-full bg-[var(--red)] opacity-[0.05]" />

      <Chip className="border-[var(--red)]/30 text-[var(--red)] bg-[var(--red-light)]/60">ученик</Chip>

      <p className="font-serif text-[1.6rem] font-black text-[var(--navy)] mb-5 leading-tight">
        {user.lastName} {user.firstName}
      </p>

      <div className="flex flex-wrap gap-8">
        {[
          { label: "Класс",   value: user.schoolClass?.name,       accent: true },
          { label: "Профиль", value: user.studyProfile || "Общий"               },
          { label: "Период",  value: "4 четверть"                               },
        ].map(({ label, value, accent }) => (
          <div key={label}>
            <p className="text-[9px] font-bold tracking-[0.18em] uppercase text-black/30 mb-1">{label}</p>
            <p className={`font-bold text-[14px] ${accent ? "text-[var(--red)]" : "text-[var(--navy)]"}`}>{value}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

function RatingCard({ avgGrade }: { avgGrade?: number }) {
  return (
    <div className="col-span-6 md:col-span-2 glass-card rounded-[22px] p-6 flex flex-col items-center justify-center text-center anim-in anim-delay-2">
      <Chip className="border-[var(--gold)]/30 text-[var(--gold)] bg-amber-50">Рейтинг</Chip>
      <span className="font-serif text-[3.2rem] font-black text-[var(--navy)] leading-none">
        {avgGrade ? avgGrade.toFixed(1) : "—"}
      </span>
      <span className="text-[9px] font-extrabold tracking-[0.2em] uppercase text-black/30 mt-1 mb-4">
        средний балл
      </span>
      <Progress
        value={avgGrade ? (avgGrade / 5) * 100 : 0}
        className="h-1.5 w-full bg-amber-100 [&>div]:bg-[var(--gold)] rounded-full"
      />
    </div>
  );
}

function TeacherCard({ user }: { user?: User }) {
  const teacher = user?.schoolClassTeacher;
  return (
    <div className="col-span-6 md:col-span-4 glass-card rounded-[22px] p-6 anim-in anim-delay-3">
      <Chip className="border-[var(--brown)]/30 text-[var(--brown)] bg-[var(--brown-light)]/60">
        классный руководитель
      </Chip>
      <div className="flex items-center gap-3 mb-4">
        <Avatar className="h-11 w-11">
          <AvatarFallback className="bg-[var(--navy-light)] text-[var(--navy)] font-serif font-bold text-sm">
            {teacher?.firstName?.[0]}{teacher?.lastName?.[0]}
          </AvatarFallback>
        </Avatar>
        <p className="font-bold text-[var(--navy)] text-[14px] leading-snug">
          {teacher?.firstName} {teacher?.lastName}
        </p>
      </div>
      <div className="pt-4 border-t border-black/[0.06] flex flex-col gap-1">
        <p className="text-[11px] font-semibold text-black/40">
          <span className="text-black/25 mr-1">Email</span>{teacher?.email || "—"}
        </p>
        <p className="text-[11px] font-semibold text-black/40">
          <span className="text-black/25 mr-1">Тел</span>{teacher?.phoneNumber || "—"}
        </p>
      </div>
    </div>
  );
}

function TodayScheduleCard({ schedule }: { schedule?: any[] }) {
  return (
    <div className="col-span-12 md:col-span-4 glass-card rounded-[22px] p-6 anim-in anim-delay-3">
      <Chip className="border-[var(--red)]/30 text-[var(--red)] bg-[var(--red-light)]/60">Сегодня</Chip>
      <div className="divide-y divide-black/[0.05]">
        {schedule && schedule.length > 0
          ? schedule.map((l) => (
              <div key={l.id} className="flex items-center gap-3 py-2.5">
                <span className="font-serif text-[1.5rem] font-black text-black/10 leading-none min-w-[24px]">
                  {l.lessonNumber}
                </span>
                <div>
                  <p className="font-bold text-[13px] text-[var(--navy)]">{l.subjectName}</p>
                  <p className="text-[10px] font-semibold text-black/30 mt-0.5">{l.classRoom}</p>
                </div>
              </div>
            ))
          : <p className="py-4 text-[13px] text-black/30 italic">Уроков сегодня нет</p>}
      </div>
    </div>
  );
}

function TodayGradesCard({ grades }: { grades?: any[] }) {
  return (
    <div className="col-span-12 md:col-span-4 glass-card rounded-[22px] p-6 anim-in anim-delay-4">
      <Chip className="border-emerald-300/50 text-emerald-700 bg-emerald-50">Оценки за сегодня</Chip>
      <div className="divide-y divide-black/[0.05]">
        {grades && grades.length > 0
          ? grades.map((g, idx) => (
              <div key={idx} className="flex items-center justify-between py-2.5">
                <span className="font-semibold text-[13px] text-[var(--navy)]">{g.subjectName}</span>
                <GradeBadge grade={g.value} />
              </div>
            ))
          : <p className="py-4 text-[13px] text-black/30 italic">Оценок пока нет</p>}
      </div>
    </div>
  );
}

function HomeworkCard({ homework }: { homework?: any[] }) {
  return (
    <div className="col-span-12 md:col-span-4 glass-card rounded-[22px] p-6 anim-in anim-delay-4">
      <Chip className="border-[var(--brown)]/30 text-[var(--brown)] bg-[var(--brown-light)]/60">ДЗ на завтра</Chip>
      <div className="flex flex-col gap-2">
        {homework && homework.length > 0
          ? homework.map((hw) => (
              <div key={hw.id} className="flex items-center gap-3 px-3 py-2 rounded-[12px] bg-white/40 ring-1 ring-black/[0.05]">
                <span className="w-2 h-2 rounded-full flex-shrink-0" style={{ backgroundColor: subjectColor(hw.subjectName) }} />
                <span className="text-[12px] font-semibold text-[var(--navy)] truncate">{hw.text}</span>
                <span className="ml-auto text-[9px] font-bold text-black/25 uppercase tracking-[0.1em] shrink-0">{hw.subjectName}</span>
              </div>
            ))
          : <p className="py-4 text-[13px] text-black/30 italic">Домашних заданий нет</p>}
      </div>
    </div>
  );
}

function WeekScheduleCard({ fullSchedule, currentDayOfWeek }: { fullSchedule?: any; currentDayOfWeek: string }) {
  return (
    <div className="col-span-12 glass-card rounded-[22px] p-6 anim-in anim-delay-5">
      <Chip className="border-[var(--navy)]/20 text-[var(--navy)] bg-[var(--navy-light)]/30">
        Расписание на неделю
      </Chip>
      <div className="grid grid-cols-2 md:grid-cols-5 gap-3 mt-1">
        {DAYS_MAP.map((dayInfo) => {
          const lessons = fullSchedule?.[dayInfo.key] || [];
          const isToday = dayInfo.key === currentDayOfWeek;
          return (
            <div
              key={dayInfo.key}
              className={`px-3 py-3 rounded-[16px] ring-1 transition-colors ${
                isToday
                  ? "bg-[var(--red-light)]/70 ring-[var(--red)]/20"
                  : "bg-white/30 ring-black/[0.06] hover:bg-white/50"
              }`}
            >
              <p className={`text-[9px] font-extrabold tracking-[0.18em] uppercase mb-2 pb-1.5 border-b ${
                isToday ? "text-[var(--red)] border-[var(--red)]/15" : "text-black/30 border-black/[0.06]"
              }`}>
                {isToday ? `${dayInfo.label} · сегодня` : dayInfo.label}
              </p>
              <div className="flex flex-col gap-1">
                {lessons.length > 0
                  ? lessons.map((l: any) => (
                      <div key={l.id} className="flex items-center gap-1.5">
                        <span className={`w-1 h-1 rounded-full flex-shrink-0 ${isToday ? "bg-[var(--red)]" : "bg-black/20"}`} />
                        <span className={`text-[11px] font-semibold truncate ${isToday ? "text-[var(--navy)]" : "text-[var(--ink)]"}`}>
                          {l.subjectName}
                        </span>
                      </div>
                    ))
                  : <span className="text-[10px] text-black/25 italic">Нет уроков</span>}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

function Home() {
  const studentId = 27;
  const today = new Date();
  const todayDateStr = today.toISOString().split("T")[0];
  const currentDayOfWeek = new Intl.DateTimeFormat("en-US", { weekday: "long" })
    .format(today)
    .toUpperCase();

  const { data: user, isLoading, isError } = useUser(studentId);
  const { data: avgGrade }      = useAvgGrade(studentId, 4);
  const { data: todayGrades }   = useGradesByDate(studentId, todayDateStr);
  const { data: todaySchedule } = useScheduleByDate(studentId, currentDayOfWeek, todayDateStr);
  const { data: fullSchedule }  = useScheduleByStudentId(studentId);
  const { data: homework }      = useHomeworkByDate(todayDateStr, studentId);

  return (
    <div className="relative z-10 min-h-screen px-6 md:px-10 pt-28 pb-14">

      {/* ── Header ── */}
      <header className="flex items-end justify-between mb-10 pb-6 border-b border-black/[0.08] max-w-6xl mx-auto anim-in">
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
        <CurrentDate />
      </header>

      {/* ── Grid ── */}
      <main className="grid grid-cols-12 gap-4 max-w-6xl mx-auto">

        {/* User card */}
        {isLoading ? (
          <div className="col-span-12 md:col-span-6 glass-card rounded-[22px] p-7 anim-in anim-delay-1">
            <Skeleton className="h-5 w-16 mb-4 rounded-full" />
            <Skeleton className="h-7 w-48 mb-5 rounded-lg" />
            <div className="flex gap-8">
              <Skeleton className="h-4 w-14 rounded" />
              <Skeleton className="h-4 w-20 rounded" />
              <Skeleton className="h-4 w-18 rounded" />
            </div>
          </div>
        ) : isError || !user ? (
          <div className="col-span-12 md:col-span-6 glass-card rounded-[22px] p-7 anim-in anim-delay-1">
            <p className="text-[var(--red)] text-sm font-semibold">Ошибка загрузки профиля</p>
          </div>
        ) : (
          <UserCard user={user} />
        )}

        <RatingCard avgGrade={avgGrade} />
        <TeacherCard user={user} />

        <TodayScheduleCard schedule={todaySchedule} />
        <TodayGradesCard grades={todayGrades} />
        <HomeworkCard homework={homework} />

        <WeekScheduleCard fullSchedule={fullSchedule} currentDayOfWeek={currentDayOfWeek} />
      </main>
    </div>
  );
}

export default Home;