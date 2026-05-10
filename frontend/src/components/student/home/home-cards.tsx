import { DAYS_MAP } from "@/constants/component-constants";
import type { StudentDetailsResponse } from "@/services/student-service";
import GradeBadge from "./grade-badge";
import { subjectColor } from "@/helpers/student-helpers";
import Chip from "@/components/teacher/teacher-journal/chip";
import { Progress } from "@/components/ui/progress";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";


export function UserCard({ user }: { user: StudentDetailsResponse }) {
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

export function RatingCard({ avgGrade }: { avgGrade?: number }) {
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

export function TeacherCard({ user }: { user?: StudentDetailsResponse }) {
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

export function TodayScheduleCard({ schedule }: { schedule?: any[] }) {
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

export function TodayGradesCard({ grades }: { grades?: any[] }) {
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

export function HomeworkCard({ homework }: { homework?: any[] }) {
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

export function WeekScheduleCard({ fullSchedule, currentDayOfWeek }: { fullSchedule?: any; currentDayOfWeek: string }) {
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