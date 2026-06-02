import React, { useState, useEffect, useMemo } from "react";
import { Button } from "@/components/ui/button";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import { Download, Loader2, CalendarDays, TrendingUp, BookOpen, Star, Award, BarChart2 } from "lucide-react";
import { useGradesLessonsByStudentId } from "@/hooks/use-grade";
import type { GradeLessonDto } from "@/services/grade-service";
import { useGetAcademicPeriods } from "@/hooks/use-academic-period";
import { useStudentPeriodGrades } from "@/hooks/use-period-grade";
import StatCard from "@/components/student/grades-page/stat-card";
import Chip from "@/components/student/grades-page/chip";
import GradeBadge from "@/components/student/grades-page/grade-badge";
import StudentNavbar from "@/templates/navbars/StudentNavbar";
import { cn } from "@/lib/utils";

// ─── helpers ───────────────────────────────────────────────────────────────

const formatDate = (dateStr: string) => {
  const [, month, day] = dateStr.split("-");
  return `${day}.${month}`;
};

const calculateWeightedAvg = (grades: GradeLessonDto[]) => {
  if (grades.length === 0) return 0;
  const totalValue = grades.reduce((acc, g) => acc + g.value * g.weight, 0);
  const totalWeight = grades.reduce((acc, g) => acc + g.weight, 0);
  return totalValue / totalWeight;
};

const formatAvg = (val: number) => val === 0 ? "—" : parseFloat(val.toFixed(2)).toString();

const avgColor = (avg: number) =>
  avg >= 4.5 ? "text-emerald-600"
    : avg >= 3.5 ? "text-amber-500"
      : avg > 0 ? "text-red-500"
        : "text-black/20";

type Tab = "current" | "period";

// ─── Tab switcher ──────────────────────────────────────────────────────────

function TabSwitcher({ active, onChange }: { active: Tab; onChange: (t: Tab) => void }) {
  return (
    <div className="flex items-center gap-1 glass-pill rounded-2xl p-1 w-fit">
      {([
        { id: "current" as Tab, label: "Текущие оценки" },
        { id: "period"  as Tab, label: "Итоговые за четверть" },
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

// ─── Period grades view ────────────────────────────────────────────────────

function PeriodGradesView({ studentId }: { studentId: number }) {
  const { data, isLoading, isError } = useStudentPeriodGrades(studentId);
  const { data: periods, isLoading: isLoadingPeriods } = useGetAcademicPeriods();

  const allPeriods = useMemo(() => {
    if (!periods) return [];
    return [...periods].sort((a, b) => a.id - b.id);
  }, [periods]);

  const allGrades = useMemo(() => (data ? Object.values(data).flat() : []), [data]);
  const overallAvg = allGrades.length
    ? allGrades.reduce((s, g) => s + g.value, 0) / allGrades.length
    : 0;
  const fivesCount = allGrades.filter((g) => g.value === 5).length;
  const subjects = data ? Object.keys(data) : [];
  const schoolYear = allPeriods[0]?.schoolYear ?? "—";

  if (isLoading || isLoadingPeriods) {
    return (
      <div className="flex h-60 items-center justify-center">
        <div className="glass-card rounded-[28px] p-10 flex flex-col items-center gap-4">
          <Loader2 className="w-8 h-8 animate-spin text-[var(--red)]" />
          <p className="text-[11px] font-bold uppercase tracking-widest text-black/30">Загрузка итоговых…</p>
        </div>
      </div>
    );
  }

  if (isError || !data) {
    return (
      <div className="flex h-60 items-center justify-center">
        <div className="glass-card rounded-[28px] p-10 text-center">
          <p className="font-serif text-xl text-[var(--navy)]">Ошибка загрузки итоговых оценок</p>
        </div>
      </div>
    );
  }

  const shortPeriod = (name: string) =>
    ({ "Первая  ": "Q1", "Вторая четверть": "Q2", "Третья четверть": "Q3", "Четвертая четверть": "Q4" }[name] ?? name);

  return (
    <>
      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
        <StatCard icon={TrendingUp} label="Средний балл" value={overallAvg ? parseFloat(overallAvg.toFixed(2)).toString() : "—"} sub="за все четверти" accent="var(--red)" delay="anim-delay-1" />
        <StatCard icon={Star} label="Пятёрок" value={fivesCount.toString()} sub={`из ${allGrades.length} оценок`} accent="var(--gold)" delay="anim-delay-2" />
        <StatCard icon={BookOpen} label="Предметов" value={subjects.length.toString()} sub="в этом году" accent="var(--navy)" delay="anim-delay-3" />
        <StatCard icon={BarChart2} label="Четвертей" value={allPeriods.length.toString()} sub="всего в году" accent="var(--green)" delay="anim-delay-4" />
      </div>

      {/* Table */}
      <div className="glass-card rounded-[28px] overflow-hidden anim-in anim-delay-5">
        <div className="flex items-center justify-between px-7 pt-6 pb-4 border-b border-black/[0.05]">
          <Chip className="border-[var(--navy)]/20 text-[var(--navy)] bg-[var(--navy-light)]/30">
            Итоги по четвертям
          </Chip>
          <span className="text-[10px] font-bold text-black/20 uppercase tracking-widest">
            {schoolYear}
          </span>
        </div>

        <ScrollArea className="w-full whitespace-nowrap">
          <Table className="border-collapse min-w-full">
            <TableHeader>
              <TableRow className="hover:bg-transparent border-black/[0.05] bg-black/[0.015]">
                <TableHead className="w-[200px] pl-7 h-12 sticky left-0 bg-white/70 backdrop-blur-sm z-30 border-r border-black/[0.05] font-extrabold text-[9px] uppercase tracking-[0.22em] text-black/30">
                  Предмет
                </TableHead>
                {allPeriods.map((p) => (
                  <TableHead key={p.id} className="text-center min-w-[110px] px-2 font-bold text-[10px] text-black/25 border-r border-black/[0.04]">
                    <div className="font-extrabold text-[11px] text-[var(--navy)]/60">{shortPeriod(p.name)}</div>
                    <div className="text-[9px] text-black/25 font-normal normal-case tracking-normal">{p.name}</div>
                  </TableHead>
                ))}
                <TableHead className="w-[88px] sticky right-0 z-30 bg-white/80 backdrop-blur-sm border-l border-black/[0.07] text-center font-extrabold text-[9px] uppercase tracking-[0.18em] text-[var(--red)]">
                  Среднее
                </TableHead>
              </TableRow>
            </TableHeader>

            <TableBody>
              {subjects.map((subject) => {
                const grades = data[subject];
                const subAvg = grades.length
                  ? grades.reduce((s, g) => s + g.value, 0) / grades.length
                  : 0;

                return (
                  <TableRow key={subject} className="border-black/[0.04] transition-colors group h-[58px] hover:bg-black/[0.015]">
                    <TableCell className="pl-7 py-0 font-bold text-[13px] text-[var(--navy)] sticky left-0 bg-white/60 backdrop-blur-sm z-20 border-r border-black/[0.05] group-hover:bg-amber-50/50 transition-colors">
                      {subject}
                    </TableCell>

                    {allPeriods.map((p) => {
                      const g = grades.find((gr) => gr.academicPeriod.id === p.id);
                      return (
                        <TableCell key={p.id} className="p-0 text-center border-r border-black/[0.03] min-w-[110px]">
                          <div className="flex items-center justify-center h-[58px] px-1">
                            <GradeBadge grade={g?.value} size="md" />
                          </div>
                        </TableCell>
                      );
                    })}

                    <TableCell className="sticky right-0 z-20 border-l border-black/[0.07] p-0 bg-white/70 backdrop-blur-sm group-hover:bg-amber-50/60 transition-colors">
                      <div className="flex items-center justify-center h-[58px]">
                        <span className={`font-serif text-[20px] font-black ${avgColor(subAvg)}`}>
                          {subAvg ? parseFloat(subAvg.toFixed(1)).toString() : "—"}
                        </span>
                      </div>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
          <ScrollBar orientation="horizontal" className="h-2 mx-2 mb-2 rounded-full bg-black/[0.03]" />
        </ScrollArea>
      </div>
    </>
  );
}

// ─── Main page ─────────────────────────────────────────────────────────────

export default function GradesPage() {
  const [activeTab, setActiveTab] = useState<Tab>("current");

  const { data: periods, isLoading: isLoadingPeriods } = useGetAcademicPeriods();
  const [selectedPeriodId, setSelectedPeriodId] = useState<string>("");

  useEffect(() => {
    if (periods && periods.length > 0 && !selectedPeriodId) {
      const active = periods.find((p) => !p.isClosed) ?? periods[periods.length - 1];
      setSelectedPeriodId(active.id.toString());
    }
  }, [periods, selectedPeriodId]);

  const periodIdToFetch = selectedPeriodId ? parseInt(selectedPeriodId, 10) : 4;
  const { data: response, isLoading: isLoadingGrades, isError } = useGradesLessonsByStudentId(27, periodIdToFetch);

  const isLoadingCurrent = isLoadingPeriods || isLoadingGrades || !selectedPeriodId;

  // Grades stats (current tab)
  const today = new Date();
  const localTodayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;
  const filteredDates = response?.dates.filter((date) => {
    if (date <= localTodayStr) return true;
    return response.subjects.some((sub) => sub.grades.some((g) => g.date === date));
  }) ?? [];
  const allCurrentGrades = response?.subjects.flatMap((s) => s.grades) ?? [];
  const overallAvg = allCurrentGrades.length
    ? allCurrentGrades.reduce((a, g) => a + g.value, 0) / allCurrentGrades.length
    : 0;
  const excellentCount = allCurrentGrades.filter((g) => g.value === 5).length;

  return (
    <div className="relative z-10 min-h-screen px-6 md:px-10 pt-2 pb-14 max-w-[1600px] mx-auto">
      <StudentNavbar />

      {/* ── Header ── */}
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-8 pb-6 border-b border-black/[0.08] anim-in">
        <div>
          <p className="text-[10px] font-extrabold tracking-[0.25em] text-[var(--red)] uppercase mb-2 flex items-center gap-2">
            <span className="inline-block w-4 h-[2px] bg-[var(--red)] rounded-full" />
            Учебный процесс {response?.academicPeriod.schoolYear ?? "2025–2026"}
          </p>
          <h1 className="font-serif font-black text-[clamp(2rem,4vw,3rem)] text-[var(--navy)] leading-[0.95]">
            Успеваемость{" "}
            <em className="not-italic relative">
              <span className="relative z-10 text-[var(--red)]">ученика</span>
              <span className="absolute bottom-0 left-0 right-0 h-[5px] rounded-full opacity-15 bg-[var(--red)]" />
            </em>
          </h1>
        </div>

        <div className="flex gap-3 items-center flex-wrap">
          {/* Период только для текущих оценок */}
          {activeTab === "current" && (
            <Select value={selectedPeriodId} onValueChange={setSelectedPeriodId}>
              <SelectTrigger className="glass-pill w-[240px] h-11 font-bold text-[13px] rounded-2xl text-[var(--navy)] px-4 border-0 shadow-none">
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
                    {p.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}

          <Button
            variant="outline"
            className="glass-pill h-11 border-0 text-[11px] font-extrabold uppercase tracking-widest rounded-2xl text-[var(--navy)] px-5 hover:scale-[1.02] transition-transform"
          >
            <Download className="mr-2 h-4 w-4 text-[var(--red)]" /> PDF
          </Button>
        </div>
      </header>

      {/* ── Tab switcher ── */}
      <div className="mb-6 anim-in anim-delay-1">
        <TabSwitcher active={activeTab} onChange={setActiveTab} />
      </div>

      {/* ── Current grades tab ── */}
      {activeTab === "current" && (
        <>
          {isLoadingCurrent ? (
            <div className="flex h-60 items-center justify-center">
              <div className="glass-card rounded-[28px] p-10 flex flex-col items-center gap-4">
                <Loader2 className="w-8 h-8 animate-spin text-[var(--red)]" />
                <p className="text-[11px] font-bold uppercase tracking-widest text-black/30">Загрузка журнала…</p>
              </div>
            </div>
          ) : isError || !response ? (
            <div className="flex h-60 items-center justify-center">
              <div className="glass-card rounded-[28px] p-10 text-center">
                <p className="font-serif text-xl text-[var(--navy)]">Ошибка загрузки электронного журнала</p>
              </div>
            </div>
          ) : (
            <>
              {/* Stats */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
                <StatCard icon={TrendingUp} label="Средний балл" value={overallAvg ? parseFloat(overallAvg.toFixed(2)).toString() : "—"} sub="за четверть" accent="var(--red)" delay="anim-delay-1" />
                <StatCard icon={Star} label="Пятёрок" value={excellentCount.toString()} sub={`из ${allCurrentGrades.length} оценок`} accent="var(--gold)" delay="anim-delay-2" />
                <StatCard icon={BookOpen} label="Предметов" value={response.subjects.length.toString()} sub="в этой четверти" accent="var(--navy)" delay="anim-delay-3" />
                <StatCard icon={Award} label="Дней занятий" value={filteredDates.length.toString()} sub="в журнале" accent="var(--green)" delay="anim-delay-4" />
              </div>

              {/* Table */}
              <div className="glass-card rounded-[28px] overflow-hidden anim-in anim-delay-5">
                <div className="flex items-center justify-between px-7 pt-6 pb-4 border-b border-black/[0.05]">
                  <Chip className="border-[var(--navy)]/20 text-[var(--navy)] bg-[var(--navy-light)]/30">
                    Электронный журнал
                  </Chip>
                  <span className="text-[10px] font-bold text-black/20 uppercase tracking-widest">
                    {response.academicPeriod.schoolYear}
                  </span>
                </div>

                <ScrollArea className="w-full whitespace-nowrap">
                  <Table className="border-collapse min-w-full">
                    <TableHeader>
                      <TableRow className="hover:bg-transparent border-black/[0.05] bg-black/[0.015]">
                        <TableHead className="w-[200px] pl-7 h-12 sticky left-0 bg-white/70 backdrop-blur-sm z-30 border-r border-black/[0.05] font-extrabold text-[9px] uppercase tracking-[0.22em] text-black/30">
                          Предмет
                        </TableHead>
                        {filteredDates.map((date) => (
                          <TableHead key={date} className="text-center min-w-[76px] px-1 font-bold text-[10px] text-black/25 border-r border-black/[0.04]">
                            {formatDate(date)}
                          </TableHead>
                        ))}
                        <TableHead className="w-[88px] sticky right-0 z-30 bg-white/80 backdrop-blur-sm border-l border-black/[0.07] text-center font-extrabold text-[9px] uppercase tracking-[0.18em] text-[var(--red)]">
                          Средний
                        </TableHead>
                      </TableRow>
                    </TableHeader>

                    <TableBody>
                      {response.subjects.map((sub) => {
                        const weightedAvg = calculateWeightedAvg(sub.grades);
                        const avgNum = parseFloat(weightedAvg.toFixed(2));

                        return (
                          <TableRow key={sub.subject} className="border-black/[0.04] transition-colors group h-[58px] hover:bg-black/[0.015]">
                            <TableCell className="pl-7 py-0 font-bold text-[13px] text-[var(--navy)] sticky left-0 bg-white/60 backdrop-blur-sm z-20 border-r border-black/[0.05] group-hover:bg-amber-50/50 transition-colors">
                              {sub.subject}
                            </TableCell>

                            {filteredDates.map((date) => {
                              const dayGrades = sub.grades.filter((g) => g.date === date);
                              return (
                                <TableCell key={date} className="p-0 text-center border-r border-black/[0.03] min-w-[76px]">
                                  <div className="flex items-center justify-center h-[58px] px-1">
                                    {dayGrades.length > 0 ? (
                                      <div className={`flex items-center justify-center ${dayGrades.length > 1 ? "gap-0.5" : ""}`}>
                                        {dayGrades.map((g, idx) => (
                                          <React.Fragment key={g.gradeId}>
                                            <GradeBadge grade={g.value} size={dayGrades.length > 1 ? "sm" : "md"} />
                                            {dayGrades.length > 1 && idx === 0 && (
                                              <span className="text-black/15 font-light mx-[-1px] select-none">/</span>
                                            )}
                                          </React.Fragment>
                                        ))}
                                      </div>
                                    ) : (
                                      <GradeBadge />
                                    )}
                                  </div>
                                </TableCell>
                              );
                            })}

                            <TableCell className="sticky right-0 z-20 border-l border-black/[0.07] p-0 bg-white/70 backdrop-blur-sm group-hover:bg-amber-50/60 transition-colors">
                              <div className="flex items-center justify-center h-[58px]">
                                <span className={`font-serif text-[20px] font-black ${avgColor(avgNum)}`}>
                                  {formatAvg(weightedAvg)}
                                </span>
                              </div>
                            </TableCell>
                          </TableRow>
                        );
                      })}
                    </TableBody>
                  </Table>
                  <ScrollBar orientation="horizontal" className="h-2 mx-2 mb-2 rounded-full bg-black/[0.03]" />
                </ScrollArea>
              </div>
            </>
          )}
        </>
      )}

      {/* ── Period grades tab ── */}
      {activeTab === "period" && <PeriodGradesView studentId={27} />}

      {/* ── Легенда ── */}
      <div className="mt-6 flex flex-wrap gap-6 anim-in anim-delay-5">
        {[
          { color: "#16a34a", bg: "#f0fdf4", label: "Отлично (5)" },
          { color: "#d97706", bg: "#fffbeb", label: "Хорошо (4)" },
          { color: "#dc2626", bg: "#fef2f2", label: "Удовл. (3)" },
        ].map((item) => (
          <div key={item.label} className="flex items-center gap-2 text-[9px] font-extrabold uppercase tracking-[0.22em] text-black/35">
            <span className="w-5 h-5 rounded-[6px] ring-1 ring-black/[0.06] flex items-center justify-center shrink-0" style={{ background: item.bg }}>
              <span className="font-serif font-black text-[11px]" style={{ color: item.color }}>
                {item.label[item.label.indexOf("(") + 1]}
              </span>
            </span>
            {item.label}
          </div>
        ))}
      </div>
    </div>
  );
}