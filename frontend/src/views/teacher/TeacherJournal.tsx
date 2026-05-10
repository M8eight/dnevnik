import { useMemo, useState, useEffect } from "react";
import { Skeleton } from "@/components/ui/skeleton";
import {
  BookOpen,
  Users,
  BookCheck,
  Scale,
} from "lucide-react";
import { useTeacherJournal } from "@/hooks/use-teacher-journal";
import type {
  StudentJournalEntry,
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
  GRADE_TYPES,
  WEIGHT_OPTIONS,
  type ViewMode,
} from "@/constants/component-constants";
import TeacherNavbar from "@/templates/navbars/TeacherNavbar";
import StatsStrip from "@/components/teacher/teacher-journal/stats-strip";
import ToolbarPanel from "@/components/teacher/teacher-journal/toolbar-panel";
import JournalTable from "@/components/teacher/teacher-journal/journal-table";
import Legend from "@/components/teacher/teacher-journal/legent";


const TEACHER_ID = 17;
const ACADEMIC_PERIOD_ID = 4;
const DEFAULT_GRADE_TYPE = "TEST";
const DEFAULT_GRADE_WEIGHT = 1;

export default function TeacherJournal() {
  const { data: assignments } = useTeachingAssignmentDetail(TEACHER_ID);

  const [selectedAssignmentId, setSelectedAssignmentId] = useState<string>("");
  const [selectedGradeType, setSelectedGradeType] = useState<string>(DEFAULT_GRADE_TYPE);
  const [selectedWeight, setSelectedWeight] = useState<number>(DEFAULT_GRADE_WEIGHT);
  const [viewMode, setViewMode] = useState<ViewMode>("ALL");
  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    if (assignments?.length) {
      setSelectedAssignmentId((prev) =>
        prev ? prev : assignments[0].teachingAssignmentId.toString()
      );
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
    data?.studentsJournal.forEach((e) => {
      m[e.studentId] = e;
    });
    return m;
  }, [data]);

  const currentAssignment = assignments?.find(
    (a) => a.teachingAssignmentId.toString() === selectedAssignmentId
  );

  const handleExport = () => {
    console.log("Export triggered");
  };

  const handleWeightChange = (val: string) => setSelectedWeight(parseInt(val));

  return (
    <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">

      {/* Navbar */}
      <TeacherNavbar />

      {/* Sticky header с панелью управления */}
      <header className="sticky top-5 z-50 max-w-[1400px] mx-auto mb-6">
        <div className="glass-card rounded-[24px] p-5 flex flex-col xl:flex-row xl:items-center gap-5 border-none shadow-lg backdrop-blur-md ring-1 ring-black/[0.04]">
          {/* Title */}
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

          {/* Выбор типа оценок */}
          <div className="flex flex-wrap items-center gap-3">
            <Select value={selectedWeight.toString()} onValueChange={handleWeightChange}>
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

            <div className="hidden xl:block w-px h-8 bg-black/[0.06]" />

            {/* Выбор связи */}
            <Select value={selectedAssignmentId} onValueChange={setSelectedAssignmentId}>
              <SelectTrigger className="glass-pill h-10 px-5 text-[12px] font-bold rounded-2xl text-[var(--navy)] border-0 shadow-sm gap-2 min-w-[180px]">
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

      {/* Content */}
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
          gradeWeight={selectedWeight}
          academicPeriodId={ACADEMIC_PERIOD_ID}
          viewMode={viewMode}
        />

        <Legend />

      </div>
    </div>
  );
}