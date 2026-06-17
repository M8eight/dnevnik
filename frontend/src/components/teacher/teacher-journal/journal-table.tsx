import { Skeleton } from "@/components/ui/skeleton";
import { useRef } from "react";
import type { ViewMode } from "@/constants/component-constants";
import { formatColDay, formatColDate, avgStyle, useHorizontalScrollDrag } from "@/helpers/teacher-helpers";
import type { StudentJournalEntry } from "@/services/teacher-journal-service";
import { useJournalAccess } from "@/hooks/use-journal-access";
import GradePopover from "./grade-popover";
import Chip from "@/components/student/chip";

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

export default function JournalTable({
  sortedStudents,
  sortedLessons,
  journalMap,
  isLoading,
  gradeType,
  gradeWeight,
  academicPeriodId,
  viewMode,
}: JournalTableProps) {
  const { isReadOnly } = useJournalAccess();
  const tableContainerRef = useRef<HTMLDivElement>(null);
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
                <th
                  key={l.id}
                  className="z-30 bg-slate-50/95 min-w-[64px] text-center align-middle py-4 border-b border-r border-black/[0.05] shadow-sm backdrop-blur-md"
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
                <td
                  colSpan={sortedLessons.length + 2}
                  className="p-10 text-center text-black/30 font-bold text-sm"
                >
                  Ученики не найдены
                </td>
              </tr>
            ) : (
              sortedStudents.map((student) => {
                const entry = journalMap[student.id];
                return (
                  <tr
                    key={student.id}
                    className="group hover:bg-slate-50/80 transition-colors border-b border-black/[0.03]"
                  >
                    <td className="sticky left-0 z-10 bg-white/95 group-hover:bg-slate-50/95 transition-colors px-4 py-3 border-r border-black/[0.05]">
                      <p className="text-[13px] font-bold text-[var(--navy)] leading-tight truncate">
                        {student.lastName} {student.firstName}
                      </p>
                    </td>
                    {sortedLessons.map((lesson) => {
                      const grade = entry?.grades.find((g) => g.lessonInstanceId === lesson.id);
                      const attendance = entry?.attendances.find((a) => a.lessonInstanceId === lesson.id);
                      
                      return (
                        <td key={lesson.id} className="h-[70px] p-0 text-center border-r border-black/[0.05]">
                          {isReadOnly ? (
                            <div className="flex items-center justify-center w-full h-full text-[13px] font-bold text-[var(--navy)]/60 select-none">
                              {grade?.value ?? attendance?.status ?? ""}
                            </div>
                          ) : (
                            <GradePopover
                              grade={grade}
                              attendance={attendance}
                              studentId={student.id}
                              lessonInstanceId={lesson.id}
                              academicPeriodId={academicPeriodId}
                              gradeType={gradeType}
                              gradeWeight={gradeWeight}
                              viewMode={viewMode}
                            />
                          )}
                        </td>
                      );
                    })}
                    <td className="sticky right-0 z-10 bg-white/95 group-hover:bg-slate-50/95 transition-colors text-center border-l border-black/[0.05]">
                      <span className={`font-serif text-[16px] ${avgStyle(entry?.gradesAverage)}`}>
                        {entry?.gradesAverage}
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
  );
}