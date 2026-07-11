import { useMemo } from "react";
import { Skeleton } from "@/components/ui/skeleton";
import { Users, Star } from "lucide-react";
import { useFinalGradesByAssignment } from "@/hooks/use-final-grade";
import { usePeriodGradesByAssignment } from "@/hooks/use-period-grade";
import { useGetAcademicPeriods } from "@/hooks/use-academic-period";
import type { FinalGradeResponse } from "@/services/final-grade-service";
import type { PeriodGradeResponse } from "@/services/period-grade-service";
import { GRADE_STYLE } from "@/constants/component-constants";
import { cn } from "@/lib/utils";
import FinalGradePopover from "@/components/teacher/teacher-journal/final-grade-popover";
import Chip from "@/components/student/chip";

export interface Student {
  id: number;
  firstName: string;
  lastName: string;
}

export default function FinalGradesView({
  teachingAssignmentId,
  academicYearId,
  students,
  currentAcademicPeriodId,
}: {
  teachingAssignmentId: number;
  academicYearId: number;
  students: Student[];
  currentAcademicPeriodId: number;
}) {
  const { data: finalGradesData = [], isLoading: isFinalLoading } =
    useFinalGradesByAssignment(teachingAssignmentId, academicYearId);

  const { data: periodEntries = [], isLoading: isPeriodLoading } =
    usePeriodGradesByAssignment(teachingAssignmentId, currentAcademicPeriodId, academicYearId);

  const { data: academicPeriods = [] } = useGetAcademicPeriods();

  const isLoading = isFinalLoading || isPeriodLoading;
  const totalCols = 1 + academicPeriods.length + 1 + 1;

  const finalGradeByStudentId = useMemo(() => {
    const map = new Map<number, FinalGradeResponse>();
    if (finalGradesData && Array.isArray(finalGradesData)) {
      finalGradesData.forEach((item) => {
        if (item.finalGrades && item.finalGrades.length > 0) {
          map.set(item.user.id, item.finalGrades[0]);
        }
      });
    }
    return map;
  }, [finalGradesData]);

  const periodGradeMap = useMemo(() => {
    const map = new Map<number, Map<number, PeriodGradeResponse>>();
    if (periodEntries && Array.isArray(periodEntries)) {
      periodEntries.forEach((entry) => {
        const byPeriod = new Map<number, PeriodGradeResponse>();
        if (entry.periodGrades && Array.isArray(entry.periodGrades)) {
          entry.periodGrades.forEach((pg) => {
            if (!byPeriod.has(pg.academicPeriodId)) {
              byPeriod.set(pg.academicPeriodId, pg);
            }
          });
        }
        map.set(entry.user.id, byPeriod);
      });
    }
    return map;
  }, [periodEntries]);

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
          <Chip className="border-(--navy)/20 text-(--navy)">
            Годовые результаты и история периодов
          </Chip>
          <span className="text-[10px] font-bold text-black/20 uppercase tracking-widest">
            {gradedCount} / {students.length} заполнено
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
                  <th key={period.id} className="text-center px-2 py-4 border-r border-black/5 w-15">
                    <span className="text-[9px] font-extrabold uppercase tracking-wider text-black/40">
                      {period.name}
                    </span>
                  </th>
                ))}
                <th className="text-center px-4 py-4 border-r border-black/5 w-25">
                  <span className="text-[9px] font-extrabold uppercase tracking-[0.15em] text-black/40">
                    Ср. четвертей
                  </span>
                </th>
                <th className="text-center px-4 py-4 w-32.5">
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
                      className="group hover:bg-slate-50/80 transition-colors border-b border-b-black/3"
                    >
                      <td className="px-6 py-4 border-r border-black/5">
                        <p className="text-[13px] font-bold text-(--navy) leading-tight">
                          {student.lastName} {student.firstName}
                        </p>
                      </td>

                      {academicPeriods.map((period) => {
                        const grade = periodsByPeriodId?.get(period.id) ?? null;

                        if (grade !== null) {
                          return (
                            <td key={period.id} className="text-center px-2 border-r border-black/5 h-16">
                              <span className={cn(
                                "inline-flex w-7 h-7 rounded-lg items-center justify-center font-serif text-[13px] font-bold ring-1 ring-black/4 shadow-sm",
                                GRADE_STYLE[grade.value]
                              )}>
                                {grade.value}
                              </span>
                            </td>
                          );
                        }

                        return (
                          <td key={period.id} className="text-center px-2 border-r border-black/5 h-16">
                            <span className="text-black/15 text-[12px] font-bold">—</span>
                          </td>
                        );
                      })}

                      <td className="text-center px-4 border-r border-black/5">
                        <span className={`font-serif text-[15px] font-black ${avgColor(periodAvg)}`}>
                          {periodAvg !== null ? periodAvg.toFixed(2) : "—"}
                        </span>
                      </td>

                      <td className="p-0 h-16 text-center">
                        <FinalGradePopover
                          finalGrade={finalGrade}
                          studentId={student.id}
                          teachingAssignmentId={teachingAssignmentId}
                          academicYearId={academicYearId}
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