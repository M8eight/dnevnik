import { useMemo } from "react";
import { Skeleton } from "@/components/ui/skeleton";
import { Users, Star, TrendingUp } from "lucide-react";
import { usePeriodGradesByAssignment } from "@/hooks/use-period-grade";
import { useGetAcademicPeriods } from "@/hooks/use-academic-period";
import { cn } from "@/lib/utils";
import PeriodGradePopover from "@/components/teacher/teacher-journal/period-grade-popover";
import Chip from "@/components/student/chip";

const getAvgColorClass = (avg: number | null): string => {
  if (avg === null) return "text-black/25";
  if (avg >= 4.5) return "text-emerald-600";
  if (avg >= 3.5) return "text-amber-500";
  if (avg >= 2.5) return "text-orange-500";
  return "text-red-600";
};

interface PeriodGradesViewProps {
  teachingAssignmentId: number;
  academicPeriodId: number;
  academicYearId: number;
}

export default function PeriodGradesView({
  teachingAssignmentId,
  academicPeriodId,
  academicYearId,
}: PeriodGradesViewProps) {
  const { data: entries = [], isLoading: isEntriesLoading } = usePeriodGradesByAssignment(
    teachingAssignmentId,
    academicPeriodId,
    academicYearId
  );

  const { data: academicPeriods = [], isLoading: isPeriodsLoading } = useGetAcademicPeriods();

  const isLoading = isEntriesLoading || isPeriodsLoading;
  const totalCols = 3 + academicPeriods.length;

  const { gradedCount, classAverage } = useMemo(() => {
    const graded = entries.filter((e) =>
      e.periodGrades && e.periodGrades.some((pg) => pg.academicPeriodId === academicPeriodId)
    ).length;

    const studentsWithAvg = entries.filter((e) => e.currentAverage !== null);
    const avg = studentsWithAvg.length > 0
      ? studentsWithAvg.reduce((sum, e) => sum + (e.currentAverage ?? 0), 0) / studentsWithAvg.length
      : null;

    return { gradedCount: graded, classAverage: avg };
  }, [entries, academicPeriodId]); // ← Зависимости: пересчитаем если они изменились

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
            <div className="w-11 h-11 rounded-[13px] bg-(--navy-light)/40 flex items-center justify-center shrink-0">
              <Icon className="w-5 h-5 text-(--navy)" />
            </div>
            <div>
              <p className="text-[10px] font-extrabold uppercase tracking-[0.2em] text-black/30 mb-0.5">{label}</p>
              <p className="font-serif text-[1.6rem] font-black text-(--navy) leading-none">{value}</p>
              <p className="text-[11px] font-medium text-black/40 mt-0.5">{sub}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="glass-card rounded-[22px] overflow-hidden border-none shadow-xl">
        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-black/5">
          <Chip className="border-(--navy)/20 text-(--navy)">Четвертные оценки</Chip>
          <span className="text-[10px] font-bold text-black/20 uppercase tracking-widest">
            {gradedCount} / {entries.length} выставлено в текущем периоде
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-slate-50/80 border-b border-black/5">
                <th className="text-left px-6 py-4 border-r border-black/5 min-w-50">
                  <Chip className="border-(--navy)/20 text-(--navy)">Ученик</Chip>
                </th>

                {academicPeriods.map((period) => (
                  <th key={period.id} className="text-center px-2 py-4 border-r border-black/5 w-25 last:border-r-0">
                    <Chip className={cn(
                      "border-black/8 text-black/60 bg-black/1",
                      period.id === academicPeriodId && "border-amber-200 text-amber-600 bg-amber-50/50"
                    )}>
                      {period.name}
                    </Chip>
                  </th>
                ))}

                <th className="text-center px-4 py-4 border-r border-black/5 w-25">
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
                      className="group hover:bg-slate-50/80 transition-colors border-b border-black/3"
                    >
                      <td className="px-6 py-4 border-r border-black/5">
                        <p className="text-[13px] font-bold text-(--navy) leading-tight">
                          {entry.user.lastName} {entry.user.firstName}
                        </p>
                      </td>

                      {academicPeriods.map((period) => {
                        const targetGrade = entry.periodGrades.find(
                          (pg) => pg.academicPeriodId === period.id
                        ) || null;

                        return (
                          <td
                            key={period.id}
                            className="p-0 h-16 text-center border-r border-black/5 last:border-r-0 w-25"
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

                      <td className="text-center px-4 border-r border-black/5">
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