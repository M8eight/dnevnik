import React, { useMemo, useState, useEffect } from "react";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { BookOpen, Users, TrendingUp, BookCheck } from "lucide-react";
import { useTeacherJournal } from "@/hooks/use-teacher-journal";
import { format } from "date-fns/format";
import { ru } from "date-fns/locale";
import type {
  StudentJournalEntry,
  GradeJournalDto,
  AttendanceJournalDto,
} from "@/services/teacher-journal-service";
import { useTeachingAssignmentDetail } from "@/hooks/use-teaching-assignment";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

// ─── Constants ────────────────────────────────────────────────────────────────

const ATTENDANCE_LABEL: Record<string, string> = {
  ABSENT: "Н", EXCUSED: "ОП", SICK: "Б", LATE: "О",
};

const ATTENDANCE_STYLE: Record<string, string> = {
  "Н": "bg-red-50 text-red-500 ring-red-100",
  "ОП": "bg-amber-50 text-amber-500 ring-amber-100",
  "Б": "bg-emerald-50 text-emerald-600 ring-emerald-100",
  "О": "bg-blue-50 text-blue-500 ring-blue-100",
};

const GRADE_STYLE: Record<number, string> = {
  5: "bg-emerald-50 text-emerald-600 ring-emerald-100",
  4: "bg-amber-50 text-amber-500 ring-amber-100",
  3: "bg-orange-50 text-orange-500 ring-orange-100",
  2: "bg-red-50 text-red-600 ring-red-100",
};

// ─── Helpers ──────────────────────────────────────────────────────────────────

const formatColDate = (dateStr: string) => format(new Date(dateStr), "dd MMM", { locale: ru });
const formatColDay = (dateStr: string) => format(new Date(dateStr), "EEEEEE", { locale: ru }).toUpperCase();

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

// ─── Components ───────────────────────────────────────────────────────────────

function Chip({ children, className = "" }: { children: React.ReactNode; className?: string }) {
  return (
    <Badge variant="outline" className={`text-[10px] px-3 py-1 font-extrabold tracking-[0.2em] uppercase rounded-full ${className}`}>
      {children}
    </Badge>
  );
}

function LessonCell({ grade, attendance }: { grade?: GradeJournalDto; attendance?: AttendanceJournalDto; }) {
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
        <span className={`w-[32px] h-[32px] rounded-[10px] flex items-center justify-center font-serif text-[16px] font-bold ring-1 ring-black/[0.06] ${GRADE_STYLE[grade.value] ?? "bg-gray-50 text-gray-500"}`}>
          {grade.value}
        </span>
      )}
      {attLabel && (
        <span className={`w-[32px] h-[32px] rounded-[10px] flex items-center justify-center font-extrabold text-[13px] ring-1 ring-black/[0.06] ${attStyle}`}>
          {attLabel}
        </span>
      )}
    </div>
  );
}

function StatsStrip({ data }: { data: any }) {
  const totalGrades = data.studentsJournal.reduce((s: number, e: any) => s + e.grades.length, 0);
  const totalAbsent = data.studentsJournal.reduce((s: number, e: any) => s + e.attendances.filter((a: any) => a.status === "ABSENT").length, 0);
  const allGrades = data.studentsJournal.flatMap((e: any) => e.grades.map((g: any) => g.value));
  const periodAvg = allGrades.length ? (allGrades.reduce((a: number, b: number) => a + b, 0) / allGrades.length).toFixed(2) : "—";

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8 anim-in">
      {[
        { icon: Users, label: "Учеников", val: data.students.length, sub: data.academicPeriod.name },
        { icon: BookOpen, label: "Уроков", val: data.lessonInstances.length, sub: `Оценок: ${totalGrades}` },
        { icon: TrendingUp, label: "Ср. балл", val: periodAvg, sub: `Пропусков: ${totalAbsent}` },
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

export default function TeacherJournal() {
  const teacherId = 17;
  const academicPeriodId = 4;
  const { data: assignments } = useTeachingAssignmentDetail(teacherId);
  const [selectedAssignmentId, setSelectedAssignmentId] = useState<string>("");

  useEffect(() => {
    if (assignments && assignments.length > 0 && !selectedAssignmentId) {
      setSelectedAssignmentId(assignments[0].teachingAssignmentId.toString());
    }
  }, [assignments, selectedAssignmentId]);

  const { data, isLoading } = useTeacherJournal(
    selectedAssignmentId ? parseInt(selectedAssignmentId) : 0,
    academicPeriodId
  );

  // Фильтрация уроков: убираем дубликаты по датам, если они есть
  const sortedLessons = useMemo(() => {
    if (!data?.lessonInstances) return [];
    const sorted = [...data.lessonInstances].sort((a, b) => a.date.localeCompare(b.date));

    // Если хочешь оставить дубликаты дат, просто верни sorted. 
    // Если хочешь только одну колонку на дату, используй фильтр ниже:
    const uniqueDates = new Set();
    return sorted.filter(lesson => {
      if (uniqueDates.has(lesson.date)) return false;
      uniqueDates.add(lesson.date);
      return true;
    });
  }, [data]);

  const sortedStudents = useMemo(() =>
    [...(data?.students ?? [])].sort((a, b) => a.lastName.localeCompare(b.lastName, "ru")),
    [data]
  );

  const journalMap = useMemo(() => {
    const m: Record<number, StudentJournalEntry> = {};
    data?.studentsJournal.forEach((e) => { m[e.studentId] = e; });
    return m;
  }, [data]);

  const currentAssignment = assignments?.find(a => a.teachingAssignmentId.toString() === selectedAssignmentId);

  return (
    <div className="relative z-10 min-h-screen px-6 md:px-10 pt-28 pb-14">
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-10 pb-6 border-b border-black/[0.08] max-w-[1400px] mx-auto anim-in">
        <div>
          <p className="text-[10px] font-extrabold tracking-[0.25em] text-[var(--red)] uppercase mb-2 flex items-center gap-2">
            <span className="inline-block w-4 h-[2px] bg-[var(--red)] rounded-full" />
            {data?.academicPeriod.schoolYear ?? "2025–2026"} · {currentAssignment?.schoolClassName ?? "..."}
          </p>
          <h1 className="font-serif font-black text-[clamp(2rem,4vw,3rem)] text-[var(--navy)] leading-[0.95]">
            Табель успеваемости{" "}
            <em className="not-italic relative">
              <span className="relative z-10 text-[var(--red)]">{currentAssignment?.subjectName ?? "..."}</span>
              <span className="absolute bottom-0 left-0 right-0 h-[5px] rounded-full opacity-15 bg-[var(--red)]" />
            </em>
          </h1>
        </div>

        <Select value={selectedAssignmentId} onValueChange={setSelectedAssignmentId}>
          <SelectTrigger className="glass-pill min-w-[280px] h-5 font-bold text-[13px] rounded-2xl text-[var(--navy)] px-4 border-0 shadow-none">
            <div className="flex items-center gap-2">
              <BookCheck className="w-4 h-4 text-[var(--red)] shrink-0" />
              <SelectValue placeholder="Выберите группу" />
            </div>
          </SelectTrigger>
          <SelectContent className="rounded-2xl border border-white/60 shadow-2xl p-1 bg-white/90 backdrop-blur-2xl">
            {assignments?.map((p) => (
              <SelectItem key={p.teachingAssignmentId} value={p.teachingAssignmentId.toString()} className="font-bold text-[13px] py-2.5 px-3 rounded-xl cursor-pointer">
                <span className="text-[var(--red)]">{p.schoolClassName}</span> · {p.subjectName}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

      </header>

      <div className="max-w-[1400px] mx-auto">
        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
            {[0, 1, 2].map((i) => <Skeleton key={i} className="h-32 rounded-[22px]" />)}
          </div>
        ) : data && <StatsStrip data={data} />}

        {/* Контейнер таблицы с горизонтальным скроллом */}
        <div className="glass-card rounded-[22px] overflow-hidden anim-in border-none shadow-xl">
          <div className="overflow-x-auto custom-scrollbar">
            <table className="w-full border-collapse bg-white">
              <thead>
                <tr className="bg-slate-50/50">
                  {/* Уменьшил ширину колонки (w-[180px]) и сделал её монотонной */}
                  <th className="sticky left-0 z-20 bg-slate-50/95 text-left px-4 py-6 border-b border-r border-black/[0.05] w-[180px] min-w-[180px]">
                    <Chip className="border-[var(--navy)]/20 text-[var(--navy)]">Ученик</Chip>
                  </th>
                  {sortedLessons.map((l) => (
                    <th key={l.id} className="min-w-[64px] text-center align-middle py-4 border-b border-black/[0.05]">
                      <div className="flex flex-col items-center gap-0.5">
                        <span className="text-[12px] font-extrabold text-black/30 uppercase">{formatColDay(l.date)}</span>
                        <span className="text-[12px] font-bold text-[var(--navy)]">{formatColDate(l.date)}</span>
                      </div>
                    </th>
                  ))}
                  <th className="sticky right-0 z-20 bg-slate-50/95 text-center px-4 border-b border-l border-black/[0.05] w-[70px]">
                    <Chip className="border-amber-200 text-amber-600 bg-amber-50/50">Ср.б</Chip>
                  </th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr><td colSpan={sortedLessons.length + 2} className="p-10"><Skeleton className="h-20 w-full" /></td></tr>
                ) : sortedStudents.map((student, idx) => {
                  const entry = journalMap[student.id];
                  const avg = calcAvg(entry?.grades ?? []);
                  return (
                    <tr key={student.id} className="hover:bg-slate-50/80 transition-colors border-b border-black/[0.03]">
                      <td className="sticky left-0 z-10 bg-white/95 px-4 py-3 border-r border-black/[0.05]">
                        <div className="truncate">
                          <p className="text-[13px] font-bold text-[var(--navy)] leading-tight">{student.lastName} {student.firstName}</p>
                        </div>
                      </td>
                      {sortedLessons.map((lesson) => (
                        <td key={lesson.id} className="h-[70px] p-0 text-center">
                          <LessonCell
                            grade={entry?.grades.find(g => g.lessonInstanceId === lesson.id)}
                            attendance={entry?.attendances.find(a => a.lessonInstanceId === lesson.id)}
                          />
                        </td>
                      ))}
                      <td className="sticky right-0 z-10 bg-white/95 text-center border-l border-black/[0.05]">
                        <span className={`font-serif text-[16px] ${avgStyle(avg)}`}>{avg}</span>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}