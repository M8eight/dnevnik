import React, { useMemo, useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { ChevronLeft, ChevronRight, BookOpen, Users, TrendingUp } from "lucide-react";
import { useTeacherJournal } from "@/hooks/use-teacher-journal";
import { format } from "date-fns/format";
import { ru } from "date-fns/locale";
import type {
  StudentMetadata,
  LessonInstanceDto,
  StudentJournalEntry,
  GradeJournalDto,
  AttendanceJournalDto,
} from "@/services/teacher-journal-service";

// ─── Constants ────────────────────────────────────────────────────────────────

const ATTENDANCE_LABEL: Record<string, string> = {
  ABSENT:  "Н",
  EXCUSED: "ОП",
  SICK:    "Б",
  LATE:    "О",
};

const ATTENDANCE_STYLE: Record<string, string> = {
  "Н":  "bg-red-50 text-red-500 ring-red-100",
  "ОП": "bg-amber-50 text-amber-500 ring-amber-100",
  "Б":  "bg-emerald-50 text-emerald-600 ring-emerald-100",
  "О":  "bg-blue-50 text-blue-500 ring-blue-100",
};

const GRADE_STYLE: Record<number, string> = {
  5: "bg-emerald-50 text-emerald-600 ring-emerald-100",
  4: "bg-amber-50 text-amber-500 ring-amber-100",
  3: "bg-orange-50 text-orange-500 ring-orange-100",
  2: "bg-red-50 text-red-600 ring-red-100",
};

const PAGE_SIZE = 15;

// ─── Helpers ──────────────────────────────────────────────────────────────────

const formatColDate = (dateStr: string) =>
  format(new Date(dateStr), "dd MMM", { locale: ru });

const formatColDay = (dateStr: string) =>
  format(new Date(dateStr), "EEEEEE", { locale: ru }).toUpperCase();

const calcAvg = (grades: GradeJournalDto[]): string => {
  if (!grades.length) return "—";
  const avg = grades.reduce((s, g) => s + g.value, 0) / grades.length;
  return avg.toFixed(1);
};

const avgStyle = (avg: string): string => {
  if (avg === "—") return "text-black/25";
  const n = parseFloat(avg);
  if (n >= 4.5) return "text-emerald-600 font-black";
  if (n >= 3.5) return "text-amber-500 font-black";
  if (n >= 2.5) return "text-orange-500 font-bold";
  return "text-red-600 font-bold";
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

// ─── Cell: grade + attendance in one lesson slot ───────────────────────────────

function LessonCell({
  grade,
  attendance,
}: {
  grade?: GradeJournalDto;
  attendance?: AttendanceJournalDto;
}) {
  const attLabel = attendance ? (ATTENDANCE_LABEL[attendance.status] ?? "") : "";
  const attStyle = ATTENDANCE_STYLE[attLabel] ?? "";

  if (!grade && !attendance) {
    return (
      <div className="w-full h-full flex items-center justify-center">
        <span className="w-1.5 h-1.5 rounded-full bg-black/5 block" />
      </div>
    );
  }

  return (
    <div className="w-full h-full flex flex-col items-center justify-center gap-1.5 py-1">
      {grade && (
        <span
          className={`w-[32px] h-[32px] rounded-[10px] flex items-center justify-center font-serif text-[16px] font-bold ring-1 ring-black/[0.06] ${GRADE_STYLE[grade.value] ?? "bg-gray-50 text-gray-500"}`}
        >
          {grade.value}
        </span>
      )}
      {attLabel && (
        <span
          className={`w-[32px] h-[32px] rounded-[10px] flex items-center justify-center font-extrabold text-[13px] ring-1 ring-black/[0.06] ${attStyle}`}
        >
          {attLabel}
        </span>
      )}
    </div>
  );
}

// ─── Column header ─────────────────────────────────────────────────────────────

function ColHeader({ lesson }: { lesson: LessonInstanceDto }) {
  const isToday = lesson.date === new Date().toISOString().split("T")[0];
  return (
    <th
      className={`min-w-[64px] w-[64px] text-center align-bottom pb-4 border-b border-black/[0.08] ${isToday ? "border-b-2 border-b-[var(--red)]/40" : ""}`}
    >
      <div className="flex flex-col items-center gap-1">
        <span className={`text-[9px] font-extrabold tracking-[0.15em] uppercase ${isToday ? "text-[var(--red)]" : "text-black/30"}`}>
          {formatColDay(lesson.date)}
        </span>
        <span className={`text-[12px] font-bold leading-tight ${isToday ? "text-[var(--red)]" : "text-[var(--navy)]"}`}>
          {formatColDate(lesson.date)}
        </span>
        {isToday && <span className="w-1.5 h-1.5 rounded-full bg-[var(--red)] block mt-1 opacity-60" />}
      </div>
    </th>
  );
}

// ─── Student row ───────────────────────────────────────────────────────────────

function StudentRow({
  student,
  entry,
  visibleLessons,
  rowIdx,
}: {
  student: StudentMetadata;
  entry?: StudentJournalEntry;
  visibleLessons: LessonInstanceDto[];
  rowIdx: number;
}) {
  const gradeMap = useMemo(() => {
    const m: Record<number, GradeJournalDto> = {};
    entry?.grades.forEach((g) => { m[g.lessonInstanceId] = g; });
    return m;
  }, [entry]);

  const attendanceMap = useMemo(() => {
    const m: Record<number, AttendanceJournalDto> = {};
    entry?.attendances.forEach((a) => { m[a.lessonInstanceId] = a; });
    return m;
  }, [entry]);

  const avg = calcAvg(entry?.grades ?? []);

  return (
    <tr
      className={`transition-colors hover:bg-[var(--navy-light)]/20 group ${rowIdx % 2 === 0 ? "bg-transparent" : "bg-black/[0.012]"}`}
    >
      {/* Name (Sticky Left) */}
      <td className="sticky left-0 z-10 bg-white/95 group-hover:bg-[#F4F7FB]/95 transition-colors px-4 py-2 border-b border-r border-black/[0.05] shadow-[2px_0_8px_-2px_rgba(0,0,0,0.03)]"
          style={{ backdropFilter: "blur(8px)" }}>
        <div className="flex items-center gap-3">
          <span className="text-[11px] font-extrabold text-black/15 min-w-[20px] text-right">
            {rowIdx + 1}
          </span>
          <div>
            <p className="text-[13px] font-bold text-[var(--navy)] whitespace-nowrap leading-tight">
              {student.lastName}
            </p>
            <p className="text-[11px] text-black/35 whitespace-nowrap">{student.firstName}</p>
          </div>
        </div>
      </td>

      {/* Lesson cells */}
      {visibleLessons.map((lesson) => (
        <td
          key={lesson.id}
          // Высота увеличена до 80px, чтобы два квадрата по 32px + отступы помещались свободно
          className="text-center border-b border-black/[0.04] h-[80px] p-0"
        >
          <LessonCell
            grade={gradeMap[lesson.id]}
            attendance={attendanceMap[lesson.id]}
          />
        </td>
      ))}

      {/* Average (Sticky Right) */}
      <td className="sticky right-0 z-10 bg-white/95 group-hover:bg-[#F4F7FB]/95 transition-colors text-center border-b border-l border-black/[0.08] px-4 py-2 shadow-[-2px_0_8px_-2px_rgba(0,0,0,0.03)]"
          style={{ backdropFilter: "blur(8px)" }}>
        <span className={`font-serif text-[18px] ${avgStyle(avg)}`}>{avg}</span>
      </td>
    </tr>
  );
}

// ─── Stats strip ──────────────────────────────────────────────────────────────

function StatsStrip({
  data,
}: {
  data: {
    academicPeriod: { name: string; schoolYear: string };
    students: StudentMetadata[];
    lessonInstances: LessonInstanceDto[];
    studentsJournal: StudentJournalEntry[];
  };
}) {
  const totalGrades = data.studentsJournal.reduce((s, e) => s + e.grades.length, 0);
  const totalAbsent = data.studentsJournal.reduce(
    (s, e) => s + e.attendances.filter((a) => a.status === "ABSENT").length,
    0
  );
  const allGrades   = data.studentsJournal.flatMap((e) => e.grades.map((g) => g.value));
  const periodAvg   = allGrades.length
    ? (allGrades.reduce((a, b) => a + b, 0) / allGrades.length).toFixed(2)
    : "—";

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8 anim-in">
      {[
        { icon: Users,      label: "Учеников",   val: data.students.length,         sub: data.academicPeriod.name },
        { icon: BookOpen,   label: "Уроков",     val: data.lessonInstances.length,  sub: `Оценок: ${totalGrades}` },
        { icon: TrendingUp, label: "Ср. балл",   val: periodAvg,                    sub: `Пропусков: ${totalAbsent}` },
      ].map(({ icon: Icon, label, val, sub }) => (
        <div key={label} className="glass-card rounded-[22px] p-6 flex items-center gap-5">
          <div className="w-12 h-12 rounded-[14px] bg-[var(--navy-light)]/40 flex items-center justify-center flex-shrink-0">
            <Icon className="w-5 h-5 text-[var(--navy)]" />
          </div>
          <div>
            <p className="text-[10px] font-extrabold uppercase tracking-[0.2em] text-black/30 mb-1">{label}</p>
            <p className="font-serif text-[1.8rem] font-black text-[var(--navy)] leading-none mb-1">{val}</p>
            <p className="text-[11px] font-medium text-black/40">{sub}</p>
          </div>
        </div>
      ))}
    </div>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────

interface TeacherJournalProps {
  teachingAssignmentId?: number;
  academicPeriodId?: number;
  subjectName?: string;
  className?: string;
}

export default function TeacherJournal({
  teachingAssignmentId = 2,
  academicPeriodId = 4,
  subjectName = "НАЗВАНИЕПРЕДМЕТА",
  className: groupName = "Класс",
}: TeacherJournalProps) {
  const { data, isLoading } = useTeacherJournal(teachingAssignmentId, academicPeriodId);
  const [page, setPage] = useState(0);

  const sortedLessons = useMemo(
    () => [...(data?.lessonInstances ?? [])].sort((a, b) => a.date.localeCompare(b.date)),
    [data]
  );

  const totalPages = Math.max(1, Math.ceil(sortedLessons.length / PAGE_SIZE));
  const visibleLessons = sortedLessons.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  const sortedStudents = useMemo(
    () => [...(data?.students ?? [])].sort((a, b) => a.lastName.localeCompare(b.lastName, "ru")),
    [data]
  );

  const journalMap = useMemo(() => {
    const m: Record<number, StudentJournalEntry> = {};
    data?.studentsJournal.forEach((e) => { m[e.studentId] = e; });
    return m;
  }, [data]);

  return (
    <div className="relative z-10 min-h-screen px-6 md:px-10 pt-28 pb-14">

      {/* ── Header ── */}
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-10 pb-6 border-b border-black/[0.08] max-w-[1400px] mx-auto anim-in">
        <div>
          <p className="text-[10px] font-extrabold tracking-[0.25em] text-[var(--red)] uppercase mb-2 flex items-center gap-2">
            <span className="inline-block w-4 h-[2px] bg-[var(--red)] rounded-full" />
            {data?.academicPeriod.schoolYear ?? "2025–2026"} · {groupName}
          </p>
          <h1 className="font-serif font-black text-[clamp(2rem,4vw,3rem)] text-[var(--navy)] leading-[0.95]">
            Табель успеваемости{" "}
            <em className="not-italic relative">
              <span className="relative z-10 text-[var(--red)]">{subjectName}</span>
              <span className="absolute bottom-0 left-0 right-0 h-[5px] rounded-full opacity-15 bg-[var(--red)]" />
            </em>
          </h1>
          {data && (
            <p className="text-[12px] font-medium text-black/40 mt-3">
              {data.academicPeriod.name} · {data.academicPeriod.startDate} — {data.academicPeriod.endDate}
            </p>
          )}
        </div>

        {/* Column pagination */}
        {!isLoading && totalPages > 1 && (
          <div className="flex items-center gap-3">
            <Button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              variant="outline"
              size="icon"
              className="glass-pill h-11 w-11 border-0 rounded-[14px] bg-white/50 text-[var(--navy)] hover:bg-white hover:scale-105 transition-all shadow-sm disabled:opacity-40 disabled:hover:scale-100"
            >
              <ChevronLeft className="h-5 w-5" />
            </Button>

            <div className="text-center min-w-[80px]">
              <p className="text-[9px] font-extrabold uppercase text-black/30 tracking-[0.2em] mb-0.5">Страница</p>
              <p className="font-serif text-[18px] font-black text-[var(--navy)] leading-none">
                {page + 1} <span className="text-black/20 font-sans text-[14px]">/</span> {totalPages}
              </p>
            </div>

            <Button
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page === totalPages - 1}
              variant="outline"
              size="icon"
              className="glass-pill h-11 w-11 border-0 rounded-[14px] bg-white/50 text-[var(--navy)] hover:bg-white hover:scale-105 transition-all shadow-sm disabled:opacity-40 disabled:hover:scale-100"
            >
              <ChevronRight className="h-5 w-5" />
            </Button>
          </div>
        )}
      </header>

      <div className="max-w-[1400px] mx-auto">

        {/* ── Stats ── */}
        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
            {[0, 1, 2].map((i) => (
              <div key={i} className="glass-card rounded-[22px] p-6 flex gap-4">
                <Skeleton className="h-12 w-12 rounded-[14px]" />
                <div>
                  <Skeleton className="h-3 w-16 mb-2 rounded-full" />
                  <Skeleton className="h-6 w-10 mb-2 rounded-lg" />
                  <Skeleton className="h-3 w-20 rounded-full" />
                </div>
              </div>
            ))}
          </div>
        ) : data ? (
          <StatsStrip data={data} />
        ) : null}

        {/* ── Table ── */}
        <div className="glass-card rounded-[22px] overflow-hidden anim-in relative">
          {isLoading ? (
            <div className="p-8 flex flex-col gap-4">
              {Array.from({ length: 8 }).map((_, i) => (
                <div key={i} className="flex gap-4 items-center">
                  <Skeleton className="h-10 w-48 rounded-xl" />
                  {Array.from({ length: 6 }).map((__, j) => (
                    <Skeleton key={j} className="h-10 w-14 rounded-lg flex-shrink-0" />
                  ))}
                  <Skeleton className="h-10 w-12 rounded-xl ml-auto" />
                </div>
              ))}
            </div>
          ) : (
            <div className="overflow-x-auto w-full custom-scrollbar pb-2">
              <table className="w-full min-w-max border-collapse">
                <thead>
                  <tr>
                    {/* Name column header (Sticky Left) */}
                    <th className="sticky left-0 z-20 bg-white/95 text-left px-4 pb-4 border-b border-r border-black/[0.08] shadow-[2px_0_8px_-2px_rgba(0,0,0,0.03)]"
                        style={{ backdropFilter: "blur(8px)", minWidth: 240 }}>
                      <div className="flex items-center gap-2">
                        <Chip className="border-[var(--navy)]/20 text-[var(--navy)] bg-[var(--navy-light)]/30 mt-1">
                          Ученик
                        </Chip>
                      </div>
                    </th>

                    {visibleLessons.map((l) => (
                      <ColHeader key={l.id} lesson={l} />
                    ))}

                    {/* Average column header (Sticky Right) */}
                    <th className="sticky right-0 z-20 bg-white/95 text-center pb-4 px-4 border-b border-l border-black/[0.08] shadow-[-2px_0_8px_-2px_rgba(0,0,0,0.03)]"
                        style={{ backdropFilter: "blur(8px)", minWidth: 80 }}>
                      <Chip className="border-[var(--gold)]/30 text-[var(--gold)] bg-amber-50 mt-1">
                        Ср.б
                      </Chip>
                    </th>
                  </tr>
                </thead>

                <tbody>
                  {sortedStudents.map((student, idx) => (
                    <StudentRow
                      key={student.id}
                      student={student}
                      entry={journalMap[student.id]}
                      visibleLessons={visibleLessons}
                      rowIdx={idx}
                    />
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* ── Legend ── */}
        <div className="mt-6 flex flex-wrap gap-5 items-center bg-white/40 px-5 py-3 rounded-full border border-black/[0.04] w-fit">
          <p className="text-[9px] font-extrabold uppercase tracking-[0.2em] text-black/30">Обозначения</p>
          {[
            { label: "Н — отсутствовал", cls: "text-red-500" },
            { label: "ОП — оправдан",    cls: "text-amber-500" },
            { label: "Б — болезнь",      cls: "text-emerald-600" },
            { label: "О — опоздание",    cls: "text-blue-500" },
          ].map(({ label, cls }) => (
            <span key={label} className={`text-[12px] font-bold ${cls} flex items-center gap-1.5`}>
              <span className={`w-2 h-2 rounded-full currentColor bg-current opacity-20`} />
              {label}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}