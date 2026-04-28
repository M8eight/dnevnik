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
  Scale,
  GraduationCap,
  Download,
  Search,
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
import { useCreateAttendance, useDeleteAttendance } from "@/hooks/use-attendance";

// ─── Types ────────────────────────────────────────────────────────────────────

type ViewMode = "ALL" | "GRADES" | "ATTENDANCE";

// ─── Constants ────────────────────────────────────────────────────────────────

const ATTENDANCE_LABEL: Record<string, string> = {
  ABSENT: "Н",
  LATE: "ОП",
  EXCUSED: "УП",
};

const ATTENDANCE_STYLE: Record<string, string> = {
  Н: "bg-red-50 text-red-500 ring-red-100",
  ОП: "bg-amber-50 text-amber-500 ring-amber-100",
  УП: "bg-violet-50 text-violet-500 ring-violet-100",
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

const ATTENDANCE_OPTIONS = [
  { value: "ABSENT", label: "Н" },
  { value: "LATE", label: "ОП" },
  { value: "EXCUSED", label: "УП" },
];

const VIEW_MODE_OPTIONS: { id: ViewMode; label: string }[] = [
  { id: "ALL", label: "Всё" },
  { id: "GRADES", label: "Оценки" },
  { id: "ATTENDANCE", label: "Посещаемость" },
];

const LEGEND_ITEMS = [
  { bg: "bg-emerald-50", ring: "ring-emerald-100", color: "text-emerald-600", label: "5", desc: "Отлично", serif: true },
  { bg: "bg-amber-50", ring: "ring-amber-100", color: "text-amber-500", label: "4", desc: "Хорошо", serif: true },
  { bg: "bg-orange-50", ring: "ring-orange-100", color: "text-orange-500", label: "3", desc: "Удовлетв.", serif: true },
  { bg: "bg-red-50", ring: "ring-red-100", color: "text-red-600", label: "2", desc: "Неудовлетв.", serif: true },
];

const ATTENDANCE_LEGEND_ITEMS = [
  { bg: "bg-red-50", ring: "ring-red-100", color: "text-red-500", label: "Н", desc: "Не был" },
  { bg: "bg-amber-50", ring: "ring-amber-100", color: "text-amber-500", label: "ОП", desc: "Опоздал" },
  { bg: "bg-violet-50", ring: "ring-violet-100", color: "text-violet-500", label: "УП", desc: "Уваж. причина" },
];

// ─── Helpers ──────────────────────────────────────────────────────────────────

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

// ─── Hooks ────────────────────────────────────────────────────────────────────

function useHorizontalScrollDrag(ref: React.RefObject<HTMLDivElement | null>) {
  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    // ── Mouse drag ──────────────────────────────────────────────────────────
    let isDragging = false;
    let startX = 0;
    let scrollLeft = 0;
    // track if moved to suppress click
    let hasMoved = false;

    const isInteractive = (target: EventTarget | null) =>
      !!(target as HTMLElement)?.closest("button, a, input, select, [role='dialog'], [data-radix-popper-content-wrapper]");

    const onMouseDown = (e: MouseEvent) => {
      if (isInteractive(e.target)) return;
      isDragging = true;
      hasMoved = false;
      startX = e.clientX;
      scrollLeft = el.scrollLeft;
      el.style.cursor = "grabbing";
      el.style.userSelect = "none";
    };

    const onMouseMove = (e: MouseEvent) => {
      if (!isDragging) return;
      const dx = e.clientX - startX;
      if (Math.abs(dx) > 3) hasMoved = true;
      el.scrollLeft = scrollLeft - dx;
    };

    const stopDrag = () => {
      if (!isDragging) return;
      isDragging = false;
      el.style.cursor = "grab";
      el.style.userSelect = "";
    };

    // Suppress click after drag so popovers don't open
    const onClickCapture = (e: MouseEvent) => {
      if (hasMoved) e.stopPropagation();
    };

    // ── Touch drag (mobile) ─────────────────────────────────────────────────
    let touchStartX = 0;
    let touchScrollLeft = 0;

    let touchStartY = 0;
    let touchAxis: "h" | "v" | null = null;

    const onTouchStart = (e: TouchEvent) => {
      touchStartX = e.touches[0].clientX;
      touchStartY = e.touches[0].clientY;
      touchScrollLeft = el.scrollLeft;
      touchAxis = null;
    };

    const onTouchMove = (e: TouchEvent) => {
      const dx = e.touches[0].clientX - touchStartX;
      const dy = e.touches[0].clientY - touchStartY;
      if (!touchAxis) touchAxis = Math.abs(dx) > Math.abs(dy) ? "h" : "v";
      if (touchAxis === "h") {
        e.preventDefault();
        el.scrollLeft = touchScrollLeft - dx;
      }
    };

    el.addEventListener("mousedown", onMouseDown);
    el.addEventListener("mousemove", onMouseMove);
    el.addEventListener("mouseup", stopDrag);
    el.addEventListener("mouseleave", stopDrag);
    el.addEventListener("click", onClickCapture, true);
    el.addEventListener("touchstart", onTouchStart, { passive: true });
    el.addEventListener("touchmove", onTouchMove, { passive: false });
    window.addEventListener("mouseup", stopDrag);

    return () => {
      el.removeEventListener("mousedown", onMouseDown);
      el.removeEventListener("mousemove", onMouseMove);
      el.removeEventListener("mouseup", stopDrag);
      el.removeEventListener("mouseleave", stopDrag);
      el.removeEventListener("click", onClickCapture, true);
      el.removeEventListener("touchstart", onTouchStart);
      el.removeEventListener("touchmove", onTouchMove);
      window.removeEventListener("mouseup", stopDrag);
    };
  }, [ref]);
}

// ─── Small UI components ───────────────────────────────────────────────────────

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

// ─── GradePopover ─────────────────────────────────────────────────────────────

interface GradePopoverProps {
  grade?: GradeJournalDto;
  attendance?: AttendanceJournalDto;
  studentId: number;
  lessonInstanceId: number;
  academicPeriodId: number;
  gradeType: string;
  gradeWeight: number;
  viewMode: ViewMode;
}

function GradePopover({
  grade,
  attendance,
  studentId,
  lessonInstanceId,
  academicPeriodId,
  gradeType,
  gradeWeight,
  viewMode,
}: GradePopoverProps) {
  const [open, setOpen] = useState(false);

  const { mutate: createGrade, isPending: isCreatingGrade } = useCreateGrade();
  const { mutate: deleteGrade, isPending: isDeletingGrade } = useDeleteGrade();
  const { mutate: createAttendance, isPending: isCreatingAtt } = useCreateAttendance();
  const { mutate: deleteAttendance, isPending: isDeletingAtt } = useDeleteAttendance();

  const isLoading = isCreatingGrade || isDeletingGrade || isCreatingAtt || isDeletingAtt;

  const showGrade = (viewMode === "ALL" || viewMode === "GRADES") && grade;
  const showAttendance = (viewMode === "ALL" || viewMode === "ATTENDANCE") && attendance;
  const isEmpty = !showGrade && !showAttendance;

  const close = () => setOpen(false);

  const handleGradeClick = (value: number) => {
    createGrade(
      { studentId, lessonInstanceId, academicPeriodId, value, weight: gradeWeight, gradeType },
      { onSuccess: close }
    );
  };

  const handleAttendanceClick = (status: string) => {
    createAttendance({ studentId, lessonInstanceId, status }, { onSuccess: close });
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <div className="w-full h-full flex flex-col items-center justify-center gap-1 py-1 cursor-pointer group">
          {showGrade && (
            <span className={cn(
              "w-[30px] h-[30px] rounded-[8px] flex items-center justify-center font-serif text-[15px] font-bold ring-1 ring-black/[0.06] transition-transform group-hover:scale-110 shadow-sm",
              GRADE_STYLE[grade.value] || "bg-gray-50"
            )}>
              {grade.value}
            </span>
          )}
          {showAttendance && (
            <span className={cn(
              "w-[30px] h-[30px] rounded-[8px] flex items-center justify-center font-extrabold text-[12px] ring-1 ring-black/[0.06] shadow-sm",
              ATTENDANCE_STYLE[ATTENDANCE_LABEL[attendance.status]]
            )}>
              {ATTENDANCE_LABEL[attendance.status]}
            </span>
          )}
          {isEmpty && (
            <span className="w-[30px] h-[30px] rounded-[8px] flex items-center justify-center ring-1 ring-black/[0.06] bg-black/[0.02] opacity-0 group-hover:opacity-100 transition-opacity text-black/20 text-[11px] font-bold">
              +
            </span>
          )}
        </div>
      </PopoverTrigger>

      <PopoverContent className="w-[200px] p-3 rounded-2xl shadow-2xl border border-black/[0.06] bg-white/95 backdrop-blur-xl flex flex-col gap-4">
        {/* Grades section */}
        <div className="space-y-2">
          <p className="text-[9px] font-extrabold uppercase tracking-[0.2em] text-black/30 text-center">Оценка</p>
          <div className="grid grid-cols-4 gap-1.5">
            {[2, 3, 4, 5].map((val) => (
              <button
                key={val}
                onClick={() => handleGradeClick(val)}
                disabled={isLoading}
                className={cn(
                  "h-9 rounded-lg flex items-center justify-center font-serif text-[14px] font-bold border border-black/[0.05] transition-all hover:scale-105 active:scale-95 disabled:opacity-50",
                  grade?.value === val ? "ring-2 ring-[var(--navy)] ring-offset-1" : "bg-white",
                  GRADE_STYLE[val]
                )}
              >
                {val}
              </button>
            ))}
          </div>
          {grade && (
            <button
              onClick={() => deleteGrade(grade.gradeId, { onSuccess: close })}
              className="w-full py-1.5 text-[10px] font-bold text-red-400 hover:text-red-600 transition-colors bg-red-50/50 rounded-lg"
            >
              Удалить оценку
            </button>
          )}
        </div>

        <div className="h-px bg-black/[0.06]" />

        {/* Attendance section */}
        <div className="space-y-2">
          <p className="text-[9px] font-extrabold uppercase tracking-[0.2em] text-black/30 text-center">Посещаемость</p>
          <div className="grid grid-cols-4 gap-1.5">
            {/* Empty 4th slot for alignment */}
            {ATTENDANCE_OPTIONS.map((opt) => (
              <button
                key={opt.value}
                onClick={() => handleAttendanceClick(opt.value)}
                disabled={isLoading}
                className={cn(
                  "h-9 rounded-lg flex items-center justify-center font-extrabold text-[11px] border border-black/[0.05] transition-all hover:scale-105 active:scale-95 disabled:opacity-50",
                  attendance?.status === opt.value ? "ring-2 ring-[var(--navy)] ring-offset-1" : "bg-white",
                  ATTENDANCE_STYLE[opt.label]
                )}
              >
                {opt.label}
              </button>
            ))}
          </div>
          {attendance && (
            <button
              onClick={() => deleteAttendance(attendance.attendanceId, { onSuccess: close })}
              className="w-full py-1.5 text-[10px] font-bold text-red-400 hover:text-red-600 transition-colors bg-red-50/50 rounded-lg"
            >
              Удалить отметку
            </button>
          )}
        </div>

        {!grade && (
          <p className="text-[8px] text-black/30 text-center italic">
            Вес: {gradeWeight} • {GRADE_TYPES.find((t) => t.value === gradeType)?.label}
          </p>
        )}
      </PopoverContent>
    </Popover>
  );
}

// ─── StatsStrip ───────────────────────────────────────────────────────────────

function StatsStrip({ data }: { data: any }) {
  const totalGrades = data.studentsJournal.reduce((s: number, e: any) => s + e.grades.length, 0);
  const totalAbsent = data.studentsJournal.reduce(
    (s: number, e: any) => s + e.attendances.filter((a: any) => a.status === "ABSENT").length,
    0
  );
  const allGrades = data.studentsJournal.flatMap((e: any) => e.grades.map((g: any) => g.value));
  const periodAvg = allGrades.length
    ? parseFloat((allGrades.reduce((a: number, b: number) => a + b, 0) / allGrades.length).toFixed(2)).toString()
    : "—";

  const stats = [
    { icon: Users, label: "Учеников", val: data.students.length, sub: data.academicPeriod?.name ?? "..." },
    { icon: BookOpen, label: "Уроков", val: data.lessonInstances.length, sub: `Оценок: ${totalGrades}` },
    { icon: TrendingUp, label: "Ср. балл", val: periodAvg, sub: `Пропусков: ${totalAbsent}` },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8 anim-in">
      {stats.map(({ icon: Icon, label, val, sub }) => (
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

// ─── JournalTable ─────────────────────────────────────────────────────────────

interface JournalTableProps {
  sortedStudents: any[];
  sortedLessons: any[];
  journalMap: Record<number, StudentJournalEntry>;
  isLoading: boolean;
  gradeType: string;
  gradeWeight: number;
  academicPeriodId: number;
  viewMode: ViewMode;
}

function JournalTable({
  sortedStudents,
  sortedLessons,
  journalMap,
  isLoading,
  gradeType,
  gradeWeight,
  academicPeriodId,
  viewMode,
}: JournalTableProps) {
  const tableContainerRef = useRef<HTMLDivElement | null>(null);
  useHorizontalScrollDrag(tableContainerRef);

  return (
    <div className="glass-card rounded-[22px] overflow-hidden anim-in border-none shadow-xl">
      <div
        ref={tableContainerRef}
        className="overflow-x-auto cursor-grab select-none"
        style={{ WebkitOverflowScrolling: "touch" }}
      >
        <table className="w-full border-collapse">
          <thead>
            <tr>
              <th className="sticky left-0 z-40 bg-slate-50/95 text-left px-4 py-6 border-b border-r border-black/[0.05] w-[180px] min-w-[180px] shadow-sm backdrop-blur-md">
                <Chip className="border-[var(--navy)]/20 text-[var(--navy)]">Ученик</Chip>
              </th>
              {sortedLessons.map((l) => (
                <th key={l.id} className="z-30 bg-slate-50/95 min-w-[64px] text-center align-middle py-4 border-b border-r border-black/[0.05] shadow-sm backdrop-blur-md">
                  <div className="flex flex-col items-center gap-0.5">
                    <span className="text-[12px] font-extrabold text-black/30 uppercase">{formatColDay(l.lessonDate)}</span>
                    <span className="text-[12px] font-bold text-[var(--navy)]">{formatColDate(l.lessonDate)}</span>
                  </div>
                </th>
              ))}
              <th className="sticky right-0 z-40 bg-slate-50/95 text-center px-4 border-b border-l border-black/[0.05] w-[70px] shadow-sm backdrop-blur-md">
                <Chip className="border-amber-200 text-amber-600 bg-amber-50/50">Ср.б</Chip>
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
            ) : sortedStudents.length === 0 ? (
              <tr>
                <td colSpan={sortedLessons.length + 2} className="p-10 text-center text-black/30 font-bold text-sm">
                  Ученики не найдены
                </td>
              </tr>
            ) : (
              sortedStudents.map((student) => {
                const entry = journalMap[student.id];
                const avg = calcAvg(entry?.grades ?? []);
                return (
                  <tr key={student.id} className="group hover:bg-slate-50/80 transition-colors border-b border-black/[0.03]">
                    <td className="sticky left-0 z-10 bg-white/95 group-hover:bg-slate-50/95 transition-colors px-4 py-3 border-r border-black/[0.05]">
                      <p className="text-[13px] font-bold text-[var(--navy)] leading-tight truncate">
                        {student.lastName} {student.firstName}
                      </p>
                    </td>
                    {sortedLessons.map((lesson) => (
                      <td key={lesson.id} className="h-[70px] p-0 text-center border-r border-black/[0.05]">
                        <GradePopover
                          grade={entry?.grades.find((g) => g.lessonInstanceId === lesson.id)}
                          attendance={entry?.attendances.find((a) => a.lessonInstanceId === lesson.id)}
                          studentId={student.id}
                          lessonInstanceId={lesson.id}
                          academicPeriodId={academicPeriodId}
                          gradeType={gradeType}
                          gradeWeight={gradeWeight}
                          viewMode={viewMode}
                        />
                      </td>
                    ))}
                    <td className="sticky right-0 z-10 bg-white/95 group-hover:bg-slate-50/95 transition-colors text-center border-l border-black/[0.05]">
                      <span className={`font-serif text-[16px] ${avgStyle(avg)}`}>{avg}</span>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

// ─── ToolbarPanel ─────────────────────────────────────────────────────────────

interface ToolbarPanelProps {
  searchQuery: string;
  onSearchChange: (v: string) => void;
  viewMode: ViewMode;
  onViewModeChange: (v: ViewMode) => void;
  onExport: () => void;
}

function ToolbarPanel({ searchQuery, onSearchChange, viewMode, onViewModeChange, onExport }: ToolbarPanelProps) {
  return (
    <div className="glass-card rounded-[22px] px-5 py-4 flex items-center gap-4 border-none shadow-md backdrop-blur-md mb-6 anim-in">
      {/* Search — занимает свободное место слева */}
      <div className="relative flex items-center w-[360px] shrink-0">
        <Search className="w-4 h-4 text-[var(--navy)]/40 absolute left-3 pointer-events-none" />
        <input
          type="text"
          placeholder="Поиск ученика..."
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          className="glass-pill h-10 w-full pl-9 pr-4 text-[12px] font-bold rounded-2xl text-[var(--navy)] border-0 shadow-sm outline-none focus:ring-2 focus:ring-[var(--navy)]/20 transition-all placeholder:text-[var(--navy)]/30"
        />
      </div>

      {/* Spacer — толкает тогл в центр */}
      <div className="flex-1" />

      {/* View mode toggle — по центру */}
      <div className="flex items-center bg-black/[0.04] p-1 rounded-[18px] shrink-0">
        {VIEW_MODE_OPTIONS.map((mode) => (
          <button
            key={mode.id}
            onClick={() => onViewModeChange(mode.id)}
            className={cn(
              "px-5 py-2 text-[12px] font-bold rounded-2xl transition-all duration-300",
              viewMode === mode.id
                ? "bg-white text-[var(--navy)] shadow-sm"
                : "text-black/40 hover:text-black/70"
            )}
          >
            {mode.label}
          </button>
        ))}
      </div>

      {/* Spacer — симметрично */}
      <div className="flex-1" />

      {/* Export */}
      <button
        onClick={onExport}
        className="glass-pill h-10 px-5 flex items-center gap-2 text-[12px] font-bold rounded-2xl text-[var(--navy)] border-0 shadow-sm hover:bg-white/60 transition active:scale-95 shrink-0"
      >
        <Download className="w-4 h-4 text-[var(--red)]" />
        <span>Экспорт</span>
      </button>
    </div>
  );
}

// ─── Legend ───────────────────────────────────────────────────────────────────

function Legend() {
  return (
    <div className="mt-6 glass-card rounded-[22px] p-5 flex flex-wrap gap-x-8 gap-y-4 items-center justify-center text-[11px] font-extrabold text-black/40 uppercase tracking-[0.1em] border-none shadow-sm backdrop-blur-md">
      {LEGEND_ITEMS.map(({ bg, ring, color, label, desc, serif }) => (
        <div key={label} className="flex items-center gap-2">
          <span className={`w-4 h-4 rounded ${bg} ring-1 ${ring} flex items-center justify-center ${color} ${serif ? "font-serif" : ""} text-[12px]`}>
            {label}
          </span>
          <span>{desc}</span>
        </div>
      ))}
      <div className="w-px h-4 bg-black/10 hidden md:block" />
      {ATTENDANCE_LEGEND_ITEMS.map(({ bg, ring, color, label, desc }) => (
        <div key={label} className="flex items-center gap-2">
          <span className={`w-4 h-4 rounded ${bg} ring-1 ${ring} flex items-center justify-center ${color} text-[9px]`}>
            {label}
          </span>
          <span>{desc}</span>
        </div>
      ))}
    </div>
  );
}

// ─── Main page ────────────────────────────────────────────────────────────────

const TEACHER_ID = 17;
const ACADEMIC_PERIOD_ID = 4;

export default function TeacherJournal() {
  const { data: assignments } = useTeachingAssignmentDetail(TEACHER_ID);
  const [selectedAssignmentId, setSelectedAssignmentId] = useState<string>("");
  const [selectedGradeType, setSelectedGradeType] = useState<string>("TEST");
  const [selectedWeight, setSelectedWeight] = useState<string>("1");
  const [viewMode, setViewMode] = useState<ViewMode>("ALL");
  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    if (assignments?.length && !selectedAssignmentId) {
      setSelectedAssignmentId(assignments[0].teachingAssignmentId.toString());
    }
  }, [assignments, selectedAssignmentId]);

  const { data, isLoading } = useTeacherJournal(
    selectedAssignmentId ? parseInt(selectedAssignmentId) : 0,
    ACADEMIC_PERIOD_ID
  );

  const sortedLessons = useMemo(() => {
    if (!data?.lessonInstances) return [];
    const seen = new Set<string>();
    return [...data.lessonInstances]
      .sort((a, b) => a.lessonDate.localeCompare(b.lessonDate))
      .filter((l) => {
        if (seen.has(l.lessonDate)) return false;
        seen.add(l.lessonDate);
        return true;
      });
  }, [data]);

  const sortedStudents = useMemo(() => {
    const students = [...(data?.students ?? [])].sort((a, b) =>
      a.lastName.localeCompare(b.lastName, "ru")
    );
    if (!searchQuery.trim()) return students;
    const q = searchQuery.toLowerCase();
    return students.filter((s) =>
      `${s.lastName} ${s.firstName}`.toLowerCase().includes(q)
    );
  }, [data, searchQuery]);

  const journalMap = useMemo(() => {
    const m: Record<number, StudentJournalEntry> = {};
    data?.studentsJournal.forEach((e) => { m[e.studentId] = e; });
    return m;
  }, [data]);

  const currentAssignment = assignments?.find(
    (a) => a.teachingAssignmentId.toString() === selectedAssignmentId
  );

  const handleExport = () => {
    // TODO: реализовать экспорт
    console.log("Export triggered");
  };

  return (
    <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
      {/* Top header */}
      <header className="mb-5 top-0 left-0 right-0 z-[100]">
        <div className="max-w-[1400px] mx-auto px-4 md:px-10 pt-6">
          <div className="glass-card rounded-[24px] px-6 h-16 flex items-center justify-between border-none shadow-lg backdrop-blur-md">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-[14px] bg-[var(--red-light)]/60 flex items-center justify-center ring-1 ring-[var(--red)]/10">
                <GraduationCap className="w-5 h-5 text-[var(--red)]" />
              </div>
              <span className="font-serif font-black text-[1.2rem] text-[var(--navy)] tracking-tight">Дневник</span>
            </div>
            <nav className="hidden lg:flex items-center gap-2">
              <NavItem to="/teacher/journal" label="Табель" />
              <NavItem to="/teacher/homework" label="Добавить ДЗ" />
              <NavItem to="/teacher/classes" label="Мои классы" />
            </nav>
            <div className="flex items-center gap-4">
              <div className="text-right hidden sm:block">
                <p className="text-[13px] font-black text-[var(--navy)] leading-none mb-1">Алексей</p>
                <p className="text-[9px] font-extrabold tracking-[0.2em] uppercase text-black/25">Преподаватель</p>
              </div>
              <div className="w-11 h-11 rounded-[15px] bg-[var(--navy-light)]/40 ring-1 ring-black/[0.05] flex items-center justify-center shadow-inner">
                <span className="font-serif font-black text-[15px] text-[var(--navy)]">А</span>
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Sticky journal controls header */}
      <header className="sticky top-5 z-50 max-w-[1400px] mx-auto mb-6">
        <div className="glass-card rounded-[24px] p-5 flex flex-col xl:flex-row xl:items-center gap-5 border-none shadow-lg backdrop-blur-md ring-1 ring-black/[0.04]">
          {/* Title block */}
          <div className="flex-1 min-w-0 flex items-center gap-4">
            <div className="hidden sm:flex w-10 h-10 rounded-[14px] bg-[var(--red-light)]/60 items-center justify-center flex-shrink-0 ring-1 ring-[var(--red)]/10">
              <BookOpen className="w-5 h-5 text-[var(--red)]" />
            </div>
            <div className="truncate">
              <div className="flex items-center gap-2 text-[10px] font-extrabold tracking-[0.2em] text-[var(--red)] uppercase mb-0.5">
                <span>{data?.academicPeriod?.schoolYear ?? "2025–2026"}</span>
                <span className="w-1 h-1 rounded-full bg-[var(--red)]" />
                <span className="truncate">{currentAssignment?.schoolClassName ?? "..."}</span>
              </div>
              <h1 className="font-serif font-black text-[1.8rem] xl:text-[2.2rem] text-[var(--navy)] leading-tight tracking-tight truncate">
                Табель <em className="not-italic text-[var(--red)]">успеваемости</em>
              </h1>
            </div>
          </div>

          {/* Grade controls: weight, type, group */}
          <div className="flex flex-wrap items-center gap-3">
            <Select value={selectedWeight} onValueChange={setSelectedWeight}>
              <SelectTrigger className="glass-pill h-10 px-4 text-[12px] font-bold rounded-2xl text-[var(--navy)] border-0 shadow-sm gap-2">
                <Scale className="w-4 h-4 text-[var(--red)]" />
                <SelectValue />
              </SelectTrigger>
              <SelectContent className="rounded-2xl border-none shadow-2xl bg-white/95 backdrop-blur-xl">
                {WEIGHT_OPTIONS.map((w) => (
                  <SelectItem key={w.value} value={w.value} className="font-bold text-[13px] py-3 rounded-xl cursor-pointer">
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
                  <SelectItem key={t.value} value={t.value} className="font-bold text-[13px] py-3 rounded-xl cursor-pointer">
                    {t.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <div className="hidden xl:block w-px h-8 bg-black/[0.06]" />

            <Select value={selectedAssignmentId} onValueChange={setSelectedAssignmentId}>
              <SelectTrigger className="glass-pill h-10 px-5 text-[12px] font-bold rounded-2xl text-[var(--navy)] border-0 shadow-sm gap-2 min-w-[180px]">
                <Users className="w-4 h-4 text-[var(--red)]" />
                <SelectValue placeholder="Выберите группу" />
              </SelectTrigger>
              <SelectContent className="rounded-2xl border-none shadow-2xl bg-white/95 backdrop-blur-xl max-h-[350px]">
                {assignments?.map((p) => (
                  <SelectItem key={p.teachingAssignmentId} value={p.teachingAssignmentId.toString()} className="font-bold text-[13px] py-3 rounded-xl cursor-pointer">
                    <span className="text-[var(--red)] mr-1">{p.schoolClassName}</span> · {p.subjectName}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
      </header>

      {/* Content */}
      <div className="max-w-[1400px] mx-auto">
        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
            {[0, 1, 2].map((i) => <Skeleton key={i} className="h-32 rounded-[22px]" />)}
          </div>
        ) : (
          data && <StatsStrip data={data} />
        )}

        {/* Toolbar: search + view mode + export */}
        <ToolbarPanel
          searchQuery={searchQuery}
          onSearchChange={setSearchQuery}
          viewMode={viewMode}
          onViewModeChange={setViewMode}
          onExport={handleExport}
        />

        <JournalTable
          sortedStudents={sortedStudents}
          sortedLessons={sortedLessons}
          journalMap={journalMap}
          isLoading={isLoading}
          gradeType={selectedGradeType}
          gradeWeight={parseInt(selectedWeight)}
          academicPeriodId={ACADEMIC_PERIOD_ID}
          viewMode={viewMode}
        />

        <Legend />
      </div>
    </div>
  );
}