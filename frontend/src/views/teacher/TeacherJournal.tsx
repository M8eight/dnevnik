import { useMemo, useState } from "react";
import { Skeleton } from "@/components/ui/skeleton";
import { BookOpen, Users, BookCheck, Scale, CalendarDays, Lock, CalendarClock } from "lucide-react";
import { useTeacherJournal } from "@/hooks/use-teacher-journal";
import { useTeachingAssignmentDetail } from "@/hooks/use-teaching-assignment";
import { useGetAcademicPeriodsByAcademicYear } from "@/hooks/use-academic-period";
import type { StudentJournalEntry } from "@/services/teacher-journal-service";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import { GRADE_TYPES, WEIGHT_OPTIONS, type ViewMode } from "@/constants/component-constants";
import { cn } from "@/lib/utils";
import TeacherNavbar from "@/components/layout/navbars/TeacherNavbar";
import StatsStrip from "@/components/teacher/teacher-journal/stats-strip";
import ToolbarPanel from "@/components/teacher/teacher-journal/toolbar-panel";
import JournalTable from "@/components/teacher/teacher-journal/journal-table";
import Legend from "@/components/teacher/teacher-journal/legend";
import { useGetAcademicYears } from "@/hooks/use-academic-year";
import FinalGradesView from "./TeacherJournalFinalGradeTab";
import PeriodGradesView from "./TeacherJournalPeriodTab";
import { JournalAccessProvider } from "@/hooks/use-journal-access";
import ClosedPeriodAlert from "@/components/teacher/teacher-journal/closed-period-alert";

const TEACHER_ID = 17;
const DEFAULT_GRADE_TYPE = "TEST";
const DEFAULT_GRADE_WEIGHT = 1;

type Tab = "journal" | "period" | "final";

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

export default function TeacherJournal() {
  const { data: assignments } = useTeachingAssignmentDetail(TEACHER_ID);
  const { data: academicYears } = useGetAcademicYears();

  const [selectedAcademicYearId, setSelectedAcademicYearId] = useState<string>("");
  const [selectedPeriodId, setSelectedPeriodId] = useState<string>("");

  const defaultAcademicYearId = useMemo(() => {
    if (!academicYears?.length) return "";
    return academicYears[0].id.toString();
  }, [academicYears]);

  const resolvedAcademicYearId = selectedAcademicYearId || defaultAcademicYearId;
  const currentAcademicYear = useMemo(() => {
    return academicYears?.find(year => year.id.toString() === resolvedAcademicYearId);
  }, [academicYears, resolvedAcademicYearId]);

  const { data: periods } = useGetAcademicPeriodsByAcademicYear(parseInt(resolvedAcademicYearId, 10));

  const defaultPeriodId = useMemo(() => {
    if (!periods?.length) return "";
    const activePeriod = periods.find((p) => !p.isClosed) ?? periods[periods.length - 1];
    return activePeriod.id.toString();
  }, [periods]);

  const resolvedPeriodId = selectedPeriodId || defaultPeriodId;
  const academicPeriodId = resolvedPeriodId ? parseInt(resolvedPeriodId, 10) : 0;

  const currentSelectedPeriod = useMemo(() => {
    return periods?.find(p => p.id === academicPeriodId);
  }, [periods, academicPeriodId]);

  const [activeTab, setActiveTab] = useState<Tab>("journal");
  const [selectedAssignmentId, setSelectedAssignmentId] = useState<string>("");
  const [selectedGradeType, setSelectedGradeType] = useState<string>(DEFAULT_GRADE_TYPE);
  const [selectedWeight, setSelectedWeight] = useState<number>(DEFAULT_GRADE_WEIGHT);
  const [viewMode, setViewMode] = useState<ViewMode>("ALL");
  const [searchQuery, setSearchQuery] = useState("");

  const assignmentId = selectedAssignmentId ? parseInt(selectedAssignmentId) : (assignments?.[0]?.teachingAssignmentId ?? 0);

  const { data, isLoading } = useTeacherJournal(assignmentId, academicPeriodId);

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
    <JournalAccessProvider currentPeriod={currentSelectedPeriod} currentYear={currentAcademicYear}>
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
                  <span>{data?.academicPeriod?.academicYear.name}</span>
                  <span className="w-1 h-1 rounded-full bg-[var(--red)]" />
                  <span className="truncate">{currentAssignment?.schoolClassName ?? "..."}</span>
                </div>
                <h1 className="font-serif font-black text-[1.8rem] xl:text-[2.2rem] text-[var(--navy)] leading-tight tracking-tight truncate">
                  Табель <em className="not-italic text-[var(--red)]">успеваемости</em>
                </h1>
              </div>
            </div>

            <Select
              value={selectedAcademicYearId || academicYears?.[0]?.id.toString() || ""}
              onValueChange={setSelectedAcademicYearId}
            >
              <SelectTrigger className="glass-pill h-10 px-5 text-[12px] font-bold rounded-2xl text-[var(--navy)] border-0 shadow-sm gap-2 min-w-[180px]">
                <CalendarClock className="w-4 h-4 text-[var(--red)]" />
                <SelectValue placeholder="Выберите год" />
              </SelectTrigger>
              <SelectContent className="rounded-2xl border-none shadow-2xl bg-white/95 backdrop-blur-xl max-h-[350px]">
                {academicYears?.map((academicYear) => (
                  <SelectItem key={academicYear.id} value={academicYear.id.toString()} className="font-bold text-[13px] py-3 rounded-xl cursor-pointer">
                    {academicYear.name} {!academicYear.isActive && "(Архив)"}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

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

                  <Select value={resolvedPeriodId} onValueChange={setSelectedPeriodId}>
                  <SelectTrigger className={cn(
                    "glass-pill w-[240px] h-11 font-bold text-[13px] rounded-2xl px-4 border-0 shadow-none transition-all text-[var(--navy)]"
                  )}>
                    <div className="flex items-center gap-2">
                      <CalendarDays className="w-4 h-4 text-[var(--red)] shrink-0" />
                      <SelectValue placeholder="Выберите четверть" />
                    </div>
                  </SelectTrigger>
                  <SelectContent className="rounded-2xl border border-white/60 shadow-2xl p-1 bg-white/90 backdrop-blur-2xl">
                    {periods?.map((p) => (
                      <SelectItem
                        key={p.id}
                        value={p.id.toString()}
                        className="font-bold text-[13px] text-[var(--navy)] py-2.5 px-3 rounded-xl cursor-pointer"
                      >
                        <div className="flex items-center gap-2">
                          {p.name}
                          {p.isClosed && <Lock className='w-3 h-3 text-[var(--red)]' />}
                        </div>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                </>
              )}

              {/* {(activeTab === "journal" || activeTab === "period") && (
                
              )} */}

              {activeTab === "journal" && <div className="hidden xl:block w-px h-8 bg-black/[0.06]" />}

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

          <ClosedPeriodAlert 
            periodName={currentSelectedPeriod?.name} 
            yearName={currentAcademicYear?.name} 
          />

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
                academicPeriodId={academicPeriodId}
                viewMode={viewMode}
              />
              <Legend />
            </>
          )}

          {activeTab === "period" && (
            <PeriodGradesView
              teachingAssignmentId={assignmentId}
              academicPeriodId={academicPeriodId}
              academicYearId={parseInt(resolvedAcademicYearId, 10) || 0}
            />
          )}

          {activeTab === "final" && (
            <FinalGradesView
              teachingAssignmentId={assignmentId}
              academicYearId={parseInt(resolvedAcademicYearId, 10) || 0}
              students={sortedStudents}
              currentAcademicPeriodId={academicPeriodId}
            />
          )}
        </div>
      </div>
    </JournalAccessProvider>
  );
}