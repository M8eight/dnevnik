import React, { useMemo, useState, useEffect, useRef } from "react";
import { cn } from "@/lib/utils";
import { NavLink } from "react-router-dom";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import {
  BookOpen,
  Users,
  TrendingUp,
  BookCheck,
  Trash2,
  Scale,
  GraduationCap,
} from "lucide-react";
import { useTeacherJournal } from "@/hooks/use-teacher-journal";
import { format } from "date-fns/format";
import { ru } from "date-fns/locale";
import type {
  StudentJournalEntry,
  GradeJournalDto,
  AttendanceJournalDto,
} from "@/services/teacher-journal-service";
import { useTeachingAssignmentDetail } from "@/hooks/use-teaching-assignment";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { useCreateGrade, useDeleteGrade } from "@/hooks/use-grade";

const ATTENDANCE_LABEL: Record<string, string> = {
  ABSENT: "Н",
  EXCUSED: "ОП",
  SICK: "Б",
  LATE: "О",
};

const ATTENDANCE_STYLE: Record<string, string> = {
  Н: "bg-red-50 text-red-500 ring-red-100",
  ОП: "bg-amber-50 text-amber-500 ring-amber-100",
  Б: "bg-emerald-50 text-emerald-600 ring-emerald-100",
  О: "bg-blue-50 text-blue-500 ring-blue-100",
};

const GRADE_STYLE: Record<number, string> = {
  5: "bg-emerald-50 text-emerald-600 ring-emerald-100",
  4: "bg-amber-50 text-amber-500 ring-amber-100",
  3: "bg-orange-50 text-orange-500 ring-orange-100",
  2: "bg-red-50 text-red-600 ring-red-100",
};

const GRADE_TYPES = [
  { value: "TEST", label: "Тест" },
  { value: "CONTROL", label: "Контрольная" },
  { value: "HOMEWORK", label: "Домашняя" },
];

const WEIGHT_OPTIONS = [
  { value: "1", label: "Вес: 1" },
  { value: "2", label: "Вес: 2" },
  { value: "3", label: "Вес: 3" },
  { value: "4", label: "Вес: 4" },
];


const formatColDate = (dateStr: string) =>
  format(new Date(dateStr), "dd MMM", { locale: ru });
const formatColDay = (dateStr: string) =>
  format(new Date(dateStr), "EEEEEE", { locale: ru }).toUpperCase();

const calcAvg = (grades: GradeJournalDto[]): string => {
  if (!grades.length) return "—";
  const avg = grades.reduce((s, g) => s + g.value, 0) / grades.length;
  return parseFloat(avg.toFixed(1)).toString();
};

const avgStyle = (avg: string): string => {
  if (avg === "—") return "text-black/25";
  const n = parseFloat(avg);
  if (n >= 4.5) return "text-emerald-600 font-black";
  if (n >= 3.5) return "text-amber-500 font-black";
  if (n >= 2.5) return "text-orange-500 font-bold";
  return "text-red-600 font-bold";
};

function useHorizontalScrollDrag(
  ref: React.RefObject<HTMLDivElement | null>
) {
  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    let isDown = false;
    let startX: number;
    let scrollLeft: number;

    const onMouseDown = (e: MouseEvent) => {
      if ((e.target as HTMLElement).closest("button, a, input, select, [role='dialog']"))
        return;
      isDown = true;
      el.style.cursor = "grabbing";
      el.style.userSelect = "none";
      startX = e.pageX - el.offsetLeft;
      scrollLeft = el.scrollLeft;
    };

    const onMouseLeave = () => {
      isDown = false;
      el.style.cursor = "grab";
      el.style.userSelect = "";
    };

    const onMouseUp = () => {
      isDown = false;
      el.style.cursor = "grab";
      el.style.userSelect = "";
    };

    const onMouseMove = (e: MouseEvent) => {
      if (!isDown) return;
      e.preventDefault();
      const x = e.pageX - el.offsetLeft;
      const walk = (x - startX) * 1.5;
      el.scrollLeft = scrollLeft - walk;
    };

    el.addEventListener("mousedown", onMouseDown);
    el.addEventListener("mouseleave", onMouseLeave);
    el.addEventListener("mouseup", onMouseUp);
    el.addEventListener("mousemove", onMouseMove);

    window.addEventListener("mouseup", onMouseUp);

    return () => {
      el.removeEventListener("mousedown", onMouseDown);
      el.removeEventListener("mouseleave", onMouseLeave);
      el.removeEventListener("mouseup", onMouseUp);
      el.removeEventListener("mousemove", onMouseMove);
      window.removeEventListener("mouseup", onMouseUp);
    };
  }, [ref]);
}

function Chip({
  children,
  className = "",
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <Badge
      variant="outline"
      className={`text-[10px] px-3 py-1 font-extrabold tracking-[0.2em] uppercase rounded-full ${className}`}
    >
      {children}
    </Badge>
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

interface GradePopoverProps {
  grade?: GradeJournalDto;
  attendance?: AttendanceJournalDto;
  studentId: number;
  lessonInstanceId: number;
  academicPeriodId: number;
  gradeType: string;
  gradeWeight: number;
}

function GradePopover({
  grade,
  attendance,
  studentId,
  lessonInstanceId,
  academicPeriodId,
  gradeType,
  gradeWeight,
}: GradePopoverProps) {
  const [open, setOpen] = useState(false);
  const { mutate: createGrade, isPending: isCreating } = useCreateGrade();
  const { mutate: deleteGrade, isPending: isDeleting } = useDeleteGrade();

  const attLabel = attendance
    ? (ATTENDANCE_LABEL[attendance.status] ?? "")
    : "";
  const attStyle = ATTENDANCE_STYLE[attLabel] ?? "";

  const handleGradeClick = (value: number) => {
    createGrade(
      {
        studentId,
        lessonInstanceId,
        academicPeriodId,
        value,
        weight: gradeWeight,
        gradeType,
      },
      { onSuccess: () => setOpen(false) }
    );
  };

  const handleDelete = () => {
    if (!grade?.gradeId) return;
    deleteGrade(grade.gradeId, { onSuccess: () => setOpen(false) });
  };

  const isLoading = isCreating || isDeleting;

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <div className="w-full h-full flex flex-col items-center justify-center gap-1.5 py-1 cursor-pointer group">
          {grade ? (
            <span
              className={`w-[32px] h-[32px] rounded-[10px] flex items-center justify-center font-serif text-[16px] font-bold ring-1 ring-black/[0.06] transition-transform group-hover:scale-110 ${GRADE_STYLE[grade.value] ?? "bg-gray-50 text-gray-500"
                }`}
            >
              {grade.value}
            </span>
          ) : (
            <span className="w-[32px] h-[32px] rounded-[10px] flex items-center justify-center ring-1 ring-black/[0.06] bg-black/[0.02] opacity-0 group-hover:opacity-100 transition-opacity text-black/20 text-[11px] font-bold">
              +
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
      </PopoverTrigger>

      <PopoverContent
        className="w-auto p-3 rounded-2xl shadow-2xl border border-black/[0.06] bg-white/95 backdrop-blur-xl"
        side="top"
        align="center"
      >
        {grade ? (
          <div className="flex flex-col items-center gap-2">
            <p className="text-[10px] font-extrabold uppercase tracking-[0.2em] text-black/30">
              Оценка
            </p>
            <span
              className={`w-[40px] h-[40px] rounded-[12px] flex items-center justify-center font-serif text-[20px] font-bold ring-1 ring-black/[0.06] ${GRADE_STYLE[grade.value] ?? "bg-gray-50 text-gray-500"
                }`}
            >
              {grade.value}
            </span>
            <p className="text-[10px] text-black/40 font-medium">
              {GRADE_TYPES.find((t) => t.value === grade.gradeType)?.label ??
                grade.gradeType}
            </p>
            <button
              onClick={handleDelete}
              disabled={isLoading}
              className="mt-1 flex items-center gap-1.5 text-[11px] font-bold text-red-400 hover:text-red-600 transition-colors px-2 py-1 rounded-lg hover:bg-red-50 disabled:opacity-50"
            >
              <Trash2 className="w-3 h-3" />
              {isDeleting ? "Удаление..." : "Удалить"}
            </button>
          </div>
        ) : (
          <div className="flex flex-col gap-2">
            <p className="text-[10px] font-extrabold uppercase tracking-[0.2em] text-black/30 text-center">
              Выставить оценку
            </p>
            <div className="flex gap-1.5">
              {[2, 3, 4, 5].map((val) => (
                <button
                  key={val}
                  onClick={() => handleGradeClick(val)}
                  disabled={isLoading}
                  className={`w-[36px] h-[36px] rounded-[10px] flex items-center justify-center font-serif text-[16px] font-bold ring-1 ring-black/[0.06] transition-all hover:scale-110 active:scale-95 disabled:opacity-50 ${GRADE_STYLE[val] ?? "bg-gray-50 text-gray-500"
                    }`}
                >
                  {val}
                </button>
              ))}
            </div>
            <p className="text-[9px] text-black/30 text-center mt-1">
              Вес: {gradeWeight} •{" "}
              {GRADE_TYPES.find((t) => t.value === gradeType)?.label}
            </p>
          </div>
        )}
      </PopoverContent>
    </Popover>
  );
}

function StatsStrip({ data }: { data: any }) {
  const totalGrades = data.studentsJournal.reduce(
    (s: number, e: any) => s + e.grades.length,
    0
  );
  const totalAbsent = data.studentsJournal.reduce(
    (s: number, e: any) =>
      s + e.attendances.filter((a: any) => a.status === "ABSENT").length,
    0
  );
  const allGrades = data.studentsJournal.flatMap((e: any) =>
    e.grades.map((g: any) => g.value)
  );

  const periodAvg = allGrades.length
    ? parseFloat(
      (allGrades.reduce((a: number, b: number) => a + b, 0) / allGrades.length).toFixed(2)
    ).toString()
    : "—";

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8 anim-in">
      {[
        {
          icon: Users,
          label: "Учеников",
          val: data.students.length,
          sub: data.academicPeriod?.name ?? "...",
        },
        {
          icon: BookOpen,
          label: "Уроков",
          val: data.lessonInstances.length,
          sub: `Оценок: ${totalGrades}`,
        },
        {
          icon: TrendingUp,
          label: "Ср. балл",
          val: periodAvg,
          sub: `Пропусков: ${totalAbsent}`,
        },
      ].map(({ icon: Icon, label, val, sub }) => (
        <div
          key={label}
          className="glass-card rounded-[22px] p-6 flex items-center gap-5"
        >
          <div className="w-12 h-12 rounded-[14px] bg-[var(--navy-light)]/40 flex items-center justify-center flex-shrink-0">
            <Icon className="w-5 h-5 text-[var(--navy)]" />
          </div>
          <div>
            <p className="text-[10px] font-extrabold uppercase tracking-[0.2em] text-black/30 mb-1">
              {label}
            </p>
            <p className="font-serif text-[1.8rem] font-black text-[var(--navy)] leading-none mb-1">
              {val}
            </p>
            <p className="text-[11px] font-medium text-black/40">{sub}</p>
          </div>
        </div>
      ))}
    </div>
  );
}

export default function TeacherJournal() {
  const teacherId = 17;
  const academicPeriodId = 4;
  const { data: assignments } = useTeachingAssignmentDetail(teacherId);
  const [selectedAssignmentId, setSelectedAssignmentId] = useState<string>("");
  const [selectedGradeType, setSelectedGradeType] = useState<string>("TEST");
  const [selectedWeight, setSelectedWeight] = useState<string>("1");

  const tableContainerRef = useRef<HTMLDivElement | null>(null);
  useHorizontalScrollDrag(tableContainerRef);

  useEffect(() => {
    if (assignments && assignments.length > 0 && !selectedAssignmentId) {
      setSelectedAssignmentId(assignments[0].teachingAssignmentId.toString());
    }
  }, [assignments, selectedAssignmentId]);

  const { data, isLoading } = useTeacherJournal(
    selectedAssignmentId ? parseInt(selectedAssignmentId) : 0,
    academicPeriodId
  );

  const sortedLessons = useMemo(() => {
    if (!data?.lessonInstances) return [];
    const sorted = [...data.lessonInstances].sort((a, b) =>
      a.lessonDate.localeCompare(b.lessonDate)
    );
    const uniqueDates = new Set();
    return sorted.filter((lesson) => {
      if (uniqueDates.has(lesson.lessonDate)) return false;
      uniqueDates.add(lesson.lessonDate);
      return true;
    });
  }, [data]);

  const sortedStudents = useMemo(
    () =>
      [...(data?.students ?? [])].sort((a, b) =>
        a.lastName.localeCompare(b.lastName, "ru")
      ),
    [data]
  );

  const journalMap = useMemo(() => {
    const m: Record<number, StudentJournalEntry> = {};
    data?.studentsJournal.forEach((e) => {
      m[e.studentId] = e;
    });
    return m;
  }, [data]);

  const currentAssignment = assignments?.find(
    (a) => a.teachingAssignmentId.toString() === selectedAssignmentId
  );

  return (
    <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
      {/* 1. TOP FIXED HEADER */}
      <header className="mb-5 top-0 left-0 right-0 z-[100]">
        <div className="max-w-[1400px] mx-auto px-4 md:px-10 pt-6">
          <div className="glass-card rounded-[24px] px-6 h-16 flex items-center justify-between border-none shadow-lg backdrop-blur-md">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-[14px] bg-[var(--red-light)]/60 flex items-center justify-center ring-1 ring-[var(--red)]/10">
                <GraduationCap className="w-5 h-5 text-[var(--red)]" />
              </div>
              <span className="font-serif font-black text-[1.2rem] text-[var(--navy)] tracking-tight">
                Дневник
              </span>
            </div>
            <nav className="hidden lg:flex items-center gap-2">
              <NavItem to="/teacher/journal" label="Табель" />
              <NavItem to="/teacher/homework" label="Добавить ДЗ" />
              <NavItem to="/teacher/classes" label="Мои классы" />
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

      {/* 2. STICKY JOURNAL HEADER */}
      <header className="sticky top-5 z-40 max-w-[1400px] mx-auto mb-10">
        <div className="glass-card rounded-[24px] p-5 flex flex-col xl:flex-row xl:items-center gap-5 border-none shadow-lg backdrop-blur-md ring-1 ring-black/[0.04]">
          <div className="flex-1 min-w-0 flex items-center gap-4">
            <div className="hidden sm:flex w-10 h-10 rounded-[14px] bg-[var(--red-light)]/60 items-center justify-center flex-shrink-0 ring-1 ring-[var(--red)]/10">
              <BookOpen className="w-5 h-5 text-[var(--red)]" />
            </div>
            <div className="truncate">
              <div className="flex items-center gap-2 text-[10px] font-extrabold tracking-[0.2em] text-[var(--red)] uppercase mb-0.5">
                <span>{data?.academicPeriod?.schoolYear ?? "2025–2026"}</span>
                <span className="w-1 h-1 rounded-full bg-[var(--red)]" />
                <span className="truncate">
                  {currentAssignment?.schoolClassName ?? "..."}
                </span>
              </div>
              <h1 className="font-serif font-black text-[1.8rem] xl:text-[2.2rem] text-[var(--navy)] leading-tight tracking-tight truncate">
                Табель <em className="not-italic text-[var(--red)]">успеваемости</em>
              </h1>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3 xl:gap-4">
            <div className="flex items-center gap-2">
              <Select value={selectedWeight} onValueChange={setSelectedWeight}>
                <SelectTrigger className="glass-pill h-10 px-4 text-[12px] font-bold rounded-2xl text-[var(--navy)] border-0 shadow-sm gap-2">
                  <Scale className="w-4 h-4 text-[var(--red)]" />
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="rounded-2xl border-none shadow-2xl bg-white/95 backdrop-blur-xl">
                  {WEIGHT_OPTIONS.map((w) => (
                    <SelectItem
                      key={w.value}
                      value={w.value}
                      className="font-bold text-[13px] py-3 rounded-xl cursor-pointer"
                    >
                      {w.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>

              <Select value={selectedGradeType} onValueChange={setSelectedGradeType}>
                <SelectTrigger className="glass-pill h-10 px-4 text-[12px] font-bold rounded-2xl text-[var(--navy)] border-0 shadow-sm gap-2">
                  <BookCheck className="w-4 h-4 text-[var(--red)]" />
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="rounded-2xl border-none shadow-2xl bg-white/95 backdrop-blur-xl">
                  {GRADE_TYPES.map((t) => (
                    <SelectItem
                      key={t.value}
                      value={t.value}
                      className="font-bold text-[13px] py-3 rounded-xl cursor-pointer"
                    >
                      {t.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="hidden xl:block w-px h-8 bg-black/[0.06]" />

            <Select value={selectedAssignmentId} onValueChange={setSelectedAssignmentId}>
              <SelectTrigger className="glass-pill h-10 px-5 text-[12px] font-bold rounded-2xl text-[var(--navy)] border-0 shadow-sm gap-2 min-w-[200px]">
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
                    <span className="text-[var(--red)] mr-1">{p.schoolClassName}</span> ·{" "}
                    {p.subjectName}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
      </header>

      {/* 3. CONTENT AREA */}
      <div className="max-w-[1400px] mx-auto">
        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
            {[0, 1, 2].map((i) => (
              <Skeleton key={i} className="h-32 rounded-[22px]" />
            ))}
          </div>
        ) : (
          data && <StatsStrip data={data} />
        )}

        <div className="glass-card rounded-[22px] overflow-hidden anim-in border-none shadow-xl">
          {/* Контейнер с горизонтальным драг-скроллом */}
          <div
            ref={tableContainerRef}
            className="overflow-x-auto custom-scrollbar cursor-grab"
          >
            <table className="w-full border-collapse">
              <thead>
                <tr>
                  <th className="sticky left-0 z-20 bg-slate-50/95 text-left px-4 py-6 border-b border-r border-black/[0.05] w-[180px] min-w-[180px]">
                    <Chip className="border-[var(--navy)]/20 text-[var(--navy)]">
                      Ученик
                    </Chip>
                  </th>
                  {sortedLessons.map((l) => (
                    <th
                      key={l.id}
                      className="min-w-[64px] text-center align-middle py-4 border-b border-black/[0.05]"
                    >
                      <div className="flex flex-col items-center gap-0.5">
                        <span className="text-[12px] font-extrabold text-black/30 uppercase">
                          {formatColDay(l.lessonDate)}
                        </span>
                        <span className="text-[12px] font-bold text-[var(--navy)]">
                          {formatColDate(l.lessonDate)}
                        </span>
                      </div>
                    </th>
                  ))}
                  <th className="sticky right-0 z-20 bg-slate-50/95 text-center px-4 border-b border-l border-black/[0.05] w-[70px]">
                    <Chip className="border-amber-200 text-amber-600 bg-amber-50/50">
                      Ср.б
                    </Chip>
                  </th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr>
                    <td colSpan={sortedLessons.length + 2} className="p-10">
                      <Skeleton className="h-20 w-full" />
                    </td>
                  </tr>
                ) : (
                  sortedStudents.map((student) => {
                    const entry = journalMap[student.id];
                    const avg = calcAvg(entry?.grades ?? []);
                    return (
                      <tr
                        key={student.id}
                        className="group hover:bg-slate-50/80 transition-colors border-b border-black/[0.03]"
                      >
                        <td className="sticky left-0 z-10 bg-white/95 group-hover:bg-slate-50/95 transition-colors px-4 py-3 border-r border-black/[0.05]">
                          <div className="truncate">
                            <p className="text-[13px] font-bold text-[var(--navy)] leading-tight">
                              {student.lastName} {student.firstName}
                            </p>
                          </div>
                        </td>
                        {sortedLessons.map((lesson) => (
                          <td key={lesson.id} className="h-[70px] p-0 text-center">
                            <GradePopover
                              grade={entry?.grades.find(
                                (g) => g.lessonInstanceId === lesson.id
                              )}
                              attendance={entry?.attendances.find(
                                (a) => a.lessonInstanceId === lesson.id
                              )}
                              studentId={student.id}
                              lessonInstanceId={lesson.id}
                              academicPeriodId={academicPeriodId}
                              gradeType={selectedGradeType}
                              gradeWeight={parseInt(selectedWeight)}
                            />
                          </td>
                        ))}
                        <td className="sticky right-0 z-10 bg-white/95 group-hover:bg-slate-50/95 transition-colors text-center border-l border-black/[0.05]">
                          <span className={`font-serif text-[16px] ${avgStyle(avg)}`}>
                            {avg}
                          </span>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}