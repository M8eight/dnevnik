import { useMemo, useState, useEffect } from "react";
import { Skeleton } from "@/components/ui/skeleton";
import { BookOpen, Users, BookCheck, Scale, TrendingUp, Star } from "lucide-react";
import { useTeacherJournal } from "@/hooks/use-teacher-journal";
import { useTeachingAssignmentDetail } from "@/hooks/use-teaching-assignment";
import { useStudentPeriodGradesWithAverage } from "@/hooks/use-period-grade";
import type { StudentAverageResponse } from "@/services/period-grade-service";
import type { StudentJournalEntry } from "@/services/teacher-journal-service";
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

const TEACHER_ID = 17;
const ACADEMIC_PERIOD_ID = 4;
const DEFAULT_GRADE_TYPE = "TEST";
const DEFAULT_GRADE_WEIGHT = 1;

type Tab = "journal" | "period" | "final";

function TabSwitcher({ active, onChange }: { active: Tab; onChange: (t: Tab) => void }) {
  return (
    <div className="flex items-center gap-1 glass-pill rounded-2xl p-1 w-fit mb-6">
      {([
        { id: "journal" as Tab, label: "Журнал" },
        { id: "period"  as Tab, label: "Четвертные" },
        { id: "final"   as Tab, label: "Итоговые" },
      ] as const).map((tab) => (
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

function PeriodGradesView({
  teachingAssignmentId,
  academicPeriodId,
}: {
  teachingAssignmentId: number;
  academicPeriodId: number;
}) {
  const { data, isLoading } = useStudentPeriodGradesWithAverage(teachingAssignmentId, academicPeriodId);
  const entries: StudentAverageResponse[] = Array.isArray(data) ? data : [];

  const graded = entries.filter((e) => e.periodGrade !== null).length;
  const withAvg = entries.filter((e) => e.average !== null);
  const avgAll = withAvg.length
    ? withAvg.reduce((s, e) => s + (e.average ?? 0), 0) / withAvg.length
    : 0;

  const avgColor = (avg: number | null) => {
    if (!avg) return "text-black/25";
    if (avg >= 4.5) return "text-emerald-600";
    if (avg >= 3.5) return "text-amber-500";
    if (avg >= 2.5) return "text-orange-500";
    return "text-red-600";
  };

  return (
    <>
      <div className="grid grid-cols-3 gap-4 mb-6">
        {[
          { icon: Users,      label: "Учеников",    val: entries.length,                   sub: "в классе" },
          { icon: Star,       label: "Выставлено",   val: `${graded} / ${entries.length}`,  sub: "четвертных оценок" },
          { icon: TrendingUp, label: "Средний балл", val: avgAll ? avgAll.toFixed(2) : "—", sub: "по классу" },
        ].map(({ icon: Icon, label, val, sub }) => (
          <div key={label} className="glass-card rounded-[22px] p-5 flex items-center gap-4">
            <div className="w-11 h-11 rounded-[13px] bg-[var(--navy-light)]/40 flex items-center justify-center flex-shrink-0">
              <Icon className="w-5 h-5 text-[var(--navy)]" />
            </div>
            <div>
              <p className="text-[10px] font-extrabold uppercase tracking-[0.2em] text-black/30 mb-0.5">{label}</p>
              <p className="font-serif text-[1.6rem] font-black text-[var(--navy)] leading-none">{val}</p>
              <p className="text-[11px] font-medium text-black/40 mt-0.5">{sub}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="glass-card rounded-[22px] overflow-hidden border-none shadow-xl">
        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-black/[0.05]">
          <Chip className="border-[var(--navy)]/20 text-[var(--navy)]">Четвертные оценки</Chip>
          <span className="text-[10px] font-bold text-black/20 uppercase tracking-widest">
            {graded} / {entries.length} выставлено
          </span>
        </div>

        <table className="w-full border-collapse">
          <thead>
            <tr className="bg-slate-50/80 border-b border-black/[0.05]">
              <th className="text-left px-6 py-4 border-r border-black/[0.05]">
                <Chip className="border-[var(--navy)]/20 text-[var(--navy)]">Ученик</Chip>
              </th>
              <th className="text-center px-4 py-4 border-r border-black/[0.05] w-[120px]">
                <span className="text-[9px] font-extrabold uppercase tracking-[0.2em] text-black/30">Ср. балл</span>
              </th>
              <th className="text-center px-4 py-4 border-r border-black/[0.05] w-[130px]">
                <span className="text-[9px] font-extrabold uppercase tracking-[0.2em] text-black/30">Рекомендация</span>
              </th>
              <th className="text-center px-4 py-4 w-[120px]">
                <Chip className="border-amber-200 text-amber-600 bg-amber-50/50">Четвертная</Chip>
              </th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={4} className="p-10">
                  <Skeleton className="h-20 w-full" />
                </td>
              </tr>
            ) : entries.length === 0 ? (
              <tr>
                <td colSpan={4} className="p-10 text-center text-black/30 font-bold text-sm">
                  Нет данных
                </td>
              </tr>
            ) : (
              entries.map((entry) => {
                const avg = entry.average;
                const recommended = avg ? Math.round(avg) : null;
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
                    <td className="text-center px-4 border-r border-black/[0.05]">
                      <span className={`font-serif text-[18px] font-black ${avgColor(avg)}`}>
                        {avg ? avg.toFixed(2) : "—"}
                      </span>
                    </td>
                    <td className="text-center px-4 border-r border-black/[0.05]">
                      {recommended ? (
                        <span className={cn(
                          "inline-flex w-9 h-9 rounded-[10px] items-center justify-center font-serif text-[16px] font-black ring-1 ring-black/[0.06]",
                          GRADE_STYLE[recommended]
                        )}>
                          {recommended}
                        </span>
                      ) : (
                        <span className="text-black/20 font-bold text-sm">—</span>
                      )}
                    </td>
                    <td className="p-0 h-[64px] text-center">
                      <PeriodGradePopover
                        periodGrade={entry.periodGrade}
                        studentId={entry.user.id}
                        teachingAssignmentId={teachingAssignmentId}
                        academicPeriodId={academicPeriodId}
                      />
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </>
  );
}

function FinalGradesView() {
  return (
    <div className="glass-card rounded-[22px] p-10 text-center border-none shadow-xl">
      <p className="font-serif text-2xl font-black text-[var(--navy)] mb-2">Итоговые оценки</p>
      <p className="text-sm text-black/30 font-medium">Будет доступно после закрытия всех четвертей</p>
    </div>
  );
}

export default function TeacherJournal() {
  const { data: assignments } = useTeachingAssignmentDetail(TEACHER_ID);

  const [activeTab, setActiveTab] = useState<Tab>("journal");
  const [selectedAssignmentId, setSelectedAssignmentId] = useState<string>("");
  const [selectedGradeType, setSelectedGradeType] = useState<string>(DEFAULT_GRADE_TYPE);
  const [selectedWeight, setSelectedWeight] = useState<number>(DEFAULT_GRADE_WEIGHT);
  const [viewMode, setViewMode] = useState<ViewMode>("ALL");
  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    if (assignments?.length) {
      setSelectedAssignmentId((prev) => prev || assignments[0].teachingAssignmentId.toString());
    }
  }, [assignments]);

  const assignmentId = selectedAssignmentId ? parseInt(selectedAssignmentId) : 0;
  const { data, isLoading } = useTeacherJournal(assignmentId, ACADEMIC_PERIOD_ID);

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
                <span>{data?.academicPeriod?.schoolYear ?? "2025–2026"}</span>
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
              academicPeriodId={ACADEMIC_PERIOD_ID}
              viewMode={viewMode}
            />
            <Legend />
          </>
        )}

        {activeTab === "period" && (
          <PeriodGradesView
            teachingAssignmentId={assignmentId}
            academicPeriodId={ACADEMIC_PERIOD_ID}
          />
        )}

        {activeTab === "final" && <FinalGradesView />}
      </div>
    </div>
  );
}