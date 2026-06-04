import { useMemo, useState } from "react";
import { Skeleton } from "@/components/ui/skeleton";
import { BookOpen, Users, BookCheck, Scale, Star, TrendingUp } from "lucide-react";
import { useTeacherJournal } from "@/hooks/use-teacher-journal";
import { useTeachingAssignmentDetail } from "@/hooks/use-teaching-assignment";
import { useFinalGradesByAssignment } from "@/hooks/use-final-grade";
import { usePeriodGradesByAssignment } from "@/hooks/use-period-grade";
import { useGetAcademicPeriods } from "@/hooks/use-academic-period";
import type { StudentJournalEntry } from "@/services/teacher-journal-service";
import type { FinalGradeResponse } from "@/services/final-grade-service";
import type { PeriodGradeResponse } from "@/services/period-grade-service";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import { GRADE_TYPES, WEIGHT_OPTIONS, GRADE_STYLE, type ViewMode } from "@/constants/component-constants";
import { cn } from "@/lib/utils";
import TeacherNavbar from "@/templates/navbars/TeacherNavbar";
import StatsStrip from "@/components/teacher/teacher-journal/stats-strip";
import ToolbarPanel from "@/components/teacher/teacher-journal/toolbar-panel";
import JournalTable from "@/components/teacher/teacher-journal/journal-table";
import Legend from "@/components/teacher/teacher-journal/legent";
import Chip from "@/components/teacher/teacher-journal/chip";
import PeriodGradePopover from "@/components/teacher/teacher-journal/period-grade-popover";
import FinalGradePopover from "@/components/teacher/teacher-journal/final-grade-popover";

const TEACHER_ID = 17;
const CURRENT_ACADEMIC_PERIOD_ID = 4;
const DEFAULT_GRADE_TYPE = "TEST";
const DEFAULT_GRADE_WEIGHT = 1;
const SCHOOL_YEAR = "2025-2026";

type Tab = "journal" | "period" | "final";

// ---------------------------------------------------------------------------

function TabSwitcher({ active, onChange }: { active: Tab; onChange: (t: Tab) => void }) {
  const tabs = [
    { id: "journal" as Tab, label: "Журнал" },
    { id: "period" as Tab, label: "Четвертные" },
    { id: "final" as Tab, label: "Итоговые" },
  ];

  return (
    <div className="flex items-center gap-1 glass-pill rounded-2xl p-1 w-fit mb-6">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          onClick={() => onChange(tab.id)}
          className={cn(
            "px-5 h-9 rounded-xl text-[12px] font-extrabold uppercase tracking-wider transition-all duration-200",
            active === tab.id
              ? "bg-white/70 text-[var(--navy)] shadow-sm"
              : "text-black/30 hover:text-[var(--navy)] hover:bg-white/20"
          )}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
}

// ---------------------------------------------------------------------------

interface Student {
  id: number;
  firstName: string;
  lastName: string;
}

// ---------------------------------------------------------------------------

interface PeriodGradesViewProps {
  teachingAssignmentId: number;
  academicPeriodId: number;
  schoolYear: string;
}

const getAvgColorClass = (avg: number | null): string => {
  if (avg === null) return "text-black/25";
  if (avg >= 4.5) return "text-emerald-600";
  if (avg >= 3.5) return "text-amber-500";
  if (avg >= 2.5) return "text-orange-500";
  return "text-red-600";
};

export function PeriodGradesView({
  teachingAssignmentId,
  academicPeriodId, // ID текущей активной четверти для фильтрации статистики
  schoolYear,
}: PeriodGradesViewProps) {
  const { data: entries = [], isLoading: isEntriesLoading } = usePeriodGradesByAssignment(
    teachingAssignmentId,
    academicPeriodId,
    schoolYear
  );

  // Подтягиваем все периоды, чтобы построить динамические колонки (как в FinalGradesView)
  const { data: academicPeriods = [], isLoading: isPeriodsLoading } = useGetAcademicPeriods();

  const isLoading = isEntriesLoading || isPeriodsLoading;
  const totalCols = 3 + academicPeriods.length;

  // Мемоизируем расчеты статистики для текущей выбранной четверти
  const { gradedCount, classAverage } = useMemo(() => {
    const graded = entries.filter((e) =>
      e.periodGrades.some((pg) => pg.academicPeriodId === academicPeriodId)
    ).length;

    const studentsWithAvg = entries.filter((e) => e.currentAverage !== null);
    const avg = studentsWithAvg.length > 0
      ? studentsWithAvg.reduce((sum, e) => sum + (e.currentAverage ?? 0), 0) / studentsWithAvg.length
      : null;

    return { gradedCount: graded, classAverage: avg };
  }, [entries, academicPeriodId]);

  const stats = [
    { icon: Users, label: "Учеников", value: entries.length, sub: "в классе" },
    { icon: Star, label: "Выставлено", value: `${gradedCount} / ${entries.length}`, sub: "четвертных оценок" },
    { icon: TrendingUp, label: "Средний балл", value: classAverage?.toFixed(2) ?? "—", sub: "по классу" },
  ];

  return (
    <>
      <div className="grid grid-cols-3 gap-4 mb-6">
        {stats.map(({ icon: Icon, label, value, sub }) => (
          <div key={label} className="glass-card rounded-[22px] p-5 flex items-center gap-4">
            <div className="w-11 h-11 rounded-[13px] bg-[var(--navy-light)]/40 flex items-center justify-center flex-shrink-0">
              <Icon className="w-5 h-5 text-[var(--navy)]" />
            </div>
            <div>
              <p className="text-[10px] font-extrabold uppercase tracking-[0.2em] text-black/30 mb-0.5">{label}</p>
              <p className="font-serif text-[1.6rem] font-black text-[var(--navy)] leading-none">{value}</p>
              <p className="text-[11px] font-medium text-black/40 mt-0.5">{sub}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="glass-card rounded-[22px] overflow-hidden border-none shadow-xl">
        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-black/[0.05]">
          <Chip className="border-[var(--navy)]/20 text-[var(--navy)]">Четвертные оценки</Chip>
          <span className="text-[10px] font-bold text-black/20 uppercase tracking-widest">
            {gradedCount} / {entries.length} выставлено в текущем периоде
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-slate-50/80 border-b border-black/[0.05]">
                <th className="text-left px-6 py-4 border-r border-black/[0.05] min-w-[200px]">
                  <Chip className="border-[var(--navy)]/20 text-[var(--navy)]">Ученик</Chip>
                </th>

                {/* Динамические колонки для каждой четверти */}
                {academicPeriods.map((period) => (
                  <th key={period.id} className="text-center px-2 py-4 border-r border-black/[0.05] w-[100px] last:border-r-0">
                    <Chip className={cn(
                      "border-black/[0.08] text-black/60 bg-black/[0.01]",
                      period.id === academicPeriodId && "border-amber-200 text-amber-600 bg-amber-50/50"
                    )}>
                      {period.name}
                    </Chip>
                  </th>
                ))}

                <th className="text-center px-4 py-4 border-r border-black/[0.05] w-[100px]">
                  <span className="text-[9px] font-extrabold uppercase tracking-[0.2em] text-black/30"> за тек. период </span>
                </th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan={totalCols} className="p-10">
                    <Skeleton className="h-20 w-full" />
                  </td>
                </tr>
              ) : entries.length === 0 ? (
                <tr>
                  <td colSpan={totalCols} className="p-10 text-center text-black/30 font-bold text-sm">
                    Нет данных
                  </td>
                </tr>
              ) : (
                entries.map((entry) => {
                  const avg = entry.currentAverage;

                  return (
                    <tr
                      key={entry.user.id}
                      className="group hover:bg-slate-50/80 transition-colors border-b border-black/[0.03]"
                    >
                      <td className="px-6 py-4 border-r border-black/[0.05]">
                        <p className="text-[13px] font-bold text-[var(--navy)] leading-tight">
                          {entry.user.lastName} {entry.user.firstName}
                        </p>
                      </td>



                      {/* Рендерим поповер для каждого периода индивидуально */}
                      {academicPeriods.map((period) => {
                        const targetGrade = entry.periodGrades.find(
                          (pg) => pg.academicPeriodId === period.id
                        ) || null;

                        return (
                          <td
                            key={period.id}
                            className="p-0 h-[64px] text-center border-r border-black/[0.05] last:border-r-0 w-[100px]"
                          >
                            <PeriodGradePopover
                              periodGrade={targetGrade}
                              studentId={entry.user.id}
                              teachingAssignmentId={teachingAssignmentId}
                              academicPeriodId={period.id}
                            />
                          </td>
                        );
                      })}

                      <td className="text-center px-4 border-r border-black/[0.05]">
                        <span className={`font-serif text-[18px] font-black ${getAvgColorClass(avg)}`}>
                          {avg !== null ? avg.toFixed(2) : "—"}
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
    </>
  );
}

// ---------------------------------------------------------------------------

function FinalGradesView({
  teachingAssignmentId,
  schoolYear,
  students,
  currentAcademicPeriodId,
}: {
  teachingAssignmentId: number;
  schoolYear: string;
  students: Student[];
  currentAcademicPeriodId: number;
}) {
  // finalGradesData теперь имеет тип FinalGradeTeacherResponse[]
  const { data: finalGradesData = [], isLoading: isFinalLoading } =
    useFinalGradesByAssignment(teachingAssignmentId, schoolYear);

  const { data: periodEntries = [], isLoading: isPeriodLoading } =
    usePeriodGradesByAssignment(teachingAssignmentId, currentAcademicPeriodId, schoolYear);

  const { data: academicPeriods = [] } = useGetAcademicPeriods();

  const isLoading = isFinalLoading || isPeriodLoading;
  const totalCols = 1 + academicPeriods.length + 1 + 1;

  // ИСПРАВЛЕНИЕ: Правильно собираем Map из вложенной структуры
  // studentId → итоговая оценка
  const finalGradeByStudentId = new Map<number, FinalGradeResponse>();
  finalGradesData.forEach((item) => {
    // Берем первую итоговую оценку из массива (обычно она одна за год по предмету)
    if (item.finalGrades && item.finalGrades.length > 0) {
      finalGradeByStudentId.set(item.user.id, item.finalGrades[0]);
    }
  });

  // studentId → (academicPeriodId → PeriodGradeResponse)
  const periodGradeMap = new Map<number, Map<number, PeriodGradeResponse>>();
  periodEntries.forEach((entry) => {
    const byPeriod = new Map<number, PeriodGradeResponse>();
    entry.periodGrades.forEach((pg) => {
      if (!byPeriod.has(pg.academicPeriodId)) {
        byPeriod.set(pg.academicPeriodId, pg);
      }
    });
    periodGradeMap.set(entry.user.id, byPeriod);
  });

  const getPeriodAverage = (studentId: number): number | null => {
    const byPeriod = periodGradeMap.get(studentId);
    if (!byPeriod || byPeriod.size === 0) return null;
    const values = [...byPeriod.values()].map((pg) => pg.value);
    return values.reduce((sum, v) => sum + v, 0) / values.length;
  };

  const avgColor = (avg: number | null): string => {
    if (avg === null) return "text-black/25";
    if (avg >= 4.5) return "text-emerald-600";
    if (avg >= 3.5) return "text-amber-500";
    if (avg >= 2.5) return "text-orange-500";
    return "text-red-600";
  };

  // ИСПРАВЛЕНИЕ: Считаем реальное количество выставленных оценок из Map
  const gradedCount = finalGradeByStudentId.size;

  const stats = [
    { icon: Users, label: "Учеников", value: students.length, sub: "в классе" },
    { icon: Star, label: "Выставлено", value: `${gradedCount} / ${students.length}`, sub: "итоговых оценок" },
  ];

  return (
    <>
      <div className="grid grid-cols-3 gap-4 mb-6">
        {stats.map(({ icon: Icon, label, value, sub }) => (
          <div key={label} className="glass-card rounded-[22px] p-5 flex items-center gap-4">
            <div className="w-11 h-11 rounded-[13px] bg-[var(--navy-light)]/40 flex items-center justify-center flex-shrink-0">
              <Icon className="w-5 h-5 text-[var(--navy)]" />
            </div>
            <div>
              <p className="text-[10px] font-extrabold uppercase tracking-[0.2em] text-black/30 mb-0.5">{label}</p>
              <p className="font-serif text-[1.6rem] font-black text-[var(--navy)] leading-none">{value}</p>
              <p className="text-[11px] font-medium text-black/40 mt-0.5">{sub}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="glass-card rounded-[22px] overflow-hidden border-none shadow-xl">
        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-black/[0.05]">
          <Chip className="border-[var(--navy)]/20 text-[var(--navy)]">
            Годовые результаты и история периодов
          </Chip>
          <span className="text-[10px] font-bold text-black/20 uppercase tracking-widest">
            {gradedCount} / {students.length} заполнено
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-slate-50/80 border-b border-black/[0.05]">
                <th className="text-left px-6 py-4 border-r border-black/[0.05] min-w-[200px]">
                  <Chip className="border-[var(--navy)]/20 text-[var(--navy)]">Ученик</Chip>
                </th>
                {academicPeriods.map((period) => (
                  <th key={period.id} className="text-center px-2 py-4 border-r border-black/[0.05] w-[60px]">
                    <span className="text-[9px] font-extrabold uppercase tracking-wider text-black/40">
                      {period.name}
                    </span>
                  </th>
                ))}
                <th className="text-center px-4 py-4 border-r border-black/[0.05] w-[100px]">
                  <span className="text-[9px] font-extrabold uppercase tracking-[0.15em] text-black/40">
                    Ср. четвертей
                  </span>
                </th>
                <th className="text-center px-4 py-4 w-[130px]">
                  <Chip className="border-red-200 text-red-600 bg-red-50/50">Итоговая</Chip>
                </th>
              </tr>
            </thead>

            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan={totalCols} className="p-10">
                    <Skeleton className="h-20 w-full" />
                  </td>
                </tr>
              ) : students.length === 0 ? (
                <tr>
                  <td colSpan={totalCols} className="p-10 text-center text-black/30 font-bold text-sm">
                    Нет учеников в выбранной группе
                  </td>
                </tr>
              ) : (
                students.map((student) => {
                  const finalGrade = finalGradeByStudentId.get(student.id) ?? null;
                  const periodAvg = getPeriodAverage(student.id);
                  const periodsByPeriodId = periodGradeMap.get(student.id);

                  return (
                    <tr
                      key={student.id}
                      className="group hover:bg-slate-50/80 transition-colors border-b border-b-black/[0.03]"
                    >
                      <td className="px-6 py-4 border-r border-black/[0.05]">
                        <p className="text-[13px] font-bold text-[var(--navy)] leading-tight">
                          {student.lastName} {student.firstName}
                        </p>
                      </td>

                      {academicPeriods.map((period) => {
                        const grade = periodsByPeriodId?.get(period.id) ?? null;

                        if (grade !== null) {
                          return (
                            <td key={period.id} className="text-center px-2 border-r border-black/[0.05] h-[64px]">
                              <span className={cn(
                                "inline-flex w-[28px] h-[28px] rounded-lg items-center justify-center font-serif text-[13px] font-bold ring-1 ring-black/[0.04] shadow-sm",
                                GRADE_STYLE[grade.value]
                              )}>
                                {grade.value}
                              </span>
                            </td>
                          );
                        }

                        return (
                          <td key={period.id} className="text-center px-2 border-r border-black/[0.05] h-[64px]">
                            <span className="text-black/15 text-[12px] font-bold">—</span>
                          </td>
                        );
                      })}

                      <td className="text-center px-4 border-r border-black/[0.05]">
                        <span className={`font-serif text-[15px] font-black ${avgColor(periodAvg)}`}>
                          {periodAvg !== null ? periodAvg.toFixed(2) : "—"}
                        </span>
                      </td>

                      <td className="p-0 h-[64px] text-center">
                        <FinalGradePopover
                          finalGrade={finalGrade}
                          studentId={student.id}
                          teachingAssignmentId={teachingAssignmentId}
                          schoolYear={schoolYear}
                        />
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
}

// ---------------------------------------------------------------------------

export default function TeacherJournal() {
  const { data: assignments } = useTeachingAssignmentDetail(TEACHER_ID);

  const [activeTab, setActiveTab] = useState<Tab>("journal");
  const [selectedAssignmentId, setSelectedAssignmentId] = useState<string>("");
  const [selectedGradeType, setSelectedGradeType] = useState<string>(DEFAULT_GRADE_TYPE);
  const [selectedWeight, setSelectedWeight] = useState<number>(DEFAULT_GRADE_WEIGHT);
  const [viewMode, setViewMode] = useState<ViewMode>("ALL");
  const [searchQuery, setSearchQuery] = useState("");

  const assignmentId = selectedAssignmentId ? parseInt(selectedAssignmentId) : (assignments?.[0]?.teachingAssignmentId ?? 0);

  const { data, isLoading } = useTeacherJournal(assignmentId, CURRENT_ACADEMIC_PERIOD_ID);
  // Инициализируем первый assignment при загрузке
  // useState(() => {
  //   if (assignments?.length) {
  //     setSelectedAssignmentId((prev) => prev || assignments[0].teachingAssignmentId.toString());
  //   }
  // });

  // useEffect(() => {
  //   if (assignments?.length && !selectedAssignmentId) {
  //     setSelectedAssignmentId(assignments[0].teachingAssignmentId.toString());
  //   }
  // }, [assignments, selectedAssignmentId]);

  // const assignmentId = selectedAssignmentId ? parseInt(selectedAssignmentId) : 0;
  // const { data, isLoading } = useTeacherJournal(assignmentId, CURRENT_ACADEMIC_PERIOD_ID);

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

  return (
    <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
      <TeacherNavbar />

      <header className="sticky top-5 z-50 max-w-[1400px] mx-auto mb-6">
        <div className="glass-card rounded-[24px] p-5 flex flex-col xl:flex-row xl:items-center gap-5 border-none shadow-lg backdrop-blur-md ring-1 ring-black/[0.04]">
          <div className="flex-1 min-w-0 flex items-center gap-4">
            <div className="hidden sm:flex w-10 h-10 rounded-[14px] bg-[var(--red-light)]/60 items-center justify-center flex-shrink-0 ring-1 ring-[var(--red)]/10">
              <BookOpen className="w-5 h-5 text-[var(--red)]" />
            </div>
            <div className="truncate">
              <div className="flex items-center gap-2 text-[10px] font-extrabold tracking-[0.2em] text-[var(--red)] uppercase mb-0.5">
                <span>{data?.academicPeriod?.schoolYear ?? SCHOOL_YEAR}</span>
                <span className="w-1 h-1 rounded-full bg-[var(--red)]" />
                <span className="truncate">{currentAssignment?.schoolClassName ?? "..."}</span>
              </div>
              <h1 className="font-serif font-black text-[1.8rem] xl:text-[2.2rem] text-[var(--navy)] leading-tight tracking-tight truncate">
                Табель <em className="not-italic text-[var(--red)]">успеваемости</em>
              </h1>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            {activeTab === "journal" && (
              <>
                <Select value={selectedWeight.toString()} onValueChange={(v) => setSelectedWeight(parseInt(v))}>
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
              </>
            )}

            <Select
              value={selectedAssignmentId || assignments?.[0]?.teachingAssignmentId.toString() || ""}
              onValueChange={setSelectedAssignmentId}
            >
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

      <div className="max-w-[1400px] mx-auto">
        <TabSwitcher active={activeTab} onChange={setActiveTab} />

        {activeTab === "journal" && (
          <>
            {isLoading ? (
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
                {[0, 1, 2].map((i) => <Skeleton key={i} className="h-32 rounded-[22px]" />)}
              </div>
            ) : (
              data && <StatsStrip data={data} />
            )}
            <ToolbarPanel
              searchQuery={searchQuery}
              onSearchChange={setSearchQuery}
              viewMode={viewMode}
              onViewModeChange={setViewMode}
              onExport={() => console.log("export")}
            />
            <JournalTable
              sortedStudents={sortedStudents}
              sortedLessons={sortedLessons}
              journalMap={journalMap}
              isLoading={isLoading}
              gradeType={selectedGradeType}
              gradeWeight={selectedWeight}
              academicPeriodId={CURRENT_ACADEMIC_PERIOD_ID}
              viewMode={viewMode}
            />
            <Legend />
          </>
        )}

        {activeTab === "period" && (
          <PeriodGradesView
            teachingAssignmentId={assignmentId}
            academicPeriodId={CURRENT_ACADEMIC_PERIOD_ID}
            schoolYear={SCHOOL_YEAR}
          />
        )}

        {activeTab === "final" && (
          <FinalGradesView
            teachingAssignmentId={assignmentId}
            schoolYear={SCHOOL_YEAR}
            students={sortedStudents}
            currentAcademicPeriodId={CURRENT_ACADEMIC_PERIOD_ID}
          />
        )}
      </div>
    </div>
  );
}