import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { type ViewMode, GRADE_STYLE, ATTENDANCE_STYLE, ATTENDANCE_LABEL, ATTENDANCE_OPTIONS, GRADE_TYPES } from "@/constants/component-constants";
import { useCreateAttendance, useDeleteAttendance } from "@/hooks/use-attendance";
import { useCreateGrade, useDeleteGrade } from "@/hooks/use-grade";
import { useJournalAccess } from "@/hooks/use-journal-access";
import { cn } from "@/lib/utils";
import type { GradeJournalDto, AttendanceJournalDto } from "@/services/teacher-journal-service";
import { useState } from "react";

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

export default function GradePopover({
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
  const { isReadOnly } = useJournalAccess();

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
    <Popover open={open && !isReadOnly} onOpenChange={(newOpen) => {
      if (isReadOnly) {
        setOpen(false);
      } else {
        setOpen(newOpen);
      }
    }}>
      <PopoverTrigger asChild>
        <div className="w-full h-full flex flex-col items-center justify-center gap-1 py-1 cursor-pointer group">
          {showGrade && (
            <span
              className={cn(
                "w-[30px] h-[30px] rounded-[8px] flex items-center justify-center font-serif text-[15px] font-bold ring-1 ring-black/[0.06] transition-transform group-hover:scale-110 shadow-sm",
                GRADE_STYLE[grade.value] ?? "bg-gray-50"
              )}
            >
              {grade.value}
            </span>
          )}
          {showAttendance && (
            <span
              className={cn(
                "w-[30px] h-[30px] rounded-[8px] flex items-center justify-center font-extrabold text-[12px] ring-1 ring-black/[0.06] shadow-sm",
                ATTENDANCE_STYLE[ATTENDANCE_LABEL[attendance.status]]
              )}
            >
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

      {!isReadOnly && (
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
      )}
    </Popover>
  );
}