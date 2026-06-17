import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { useCreatePeriodGrade, useDeletePeriodGrade } from "@/hooks/use-period-grade";
import { useJournalAccess } from "@/hooks/use-journal-access";
import { GRADE_STYLE } from "@/constants/component-constants";
import { cn } from "@/lib/utils";
import { useState } from "react";
import type { PeriodGradeResponse } from "@/services/period-grade-service";

interface PeriodGradePopoverProps {
  periodGrade?: PeriodGradeResponse | null;
  studentId: number;
  teachingAssignmentId: number;
  academicPeriodId: number;
}

export default function PeriodGradePopover({
  periodGrade,
  studentId,
  teachingAssignmentId,
  academicPeriodId
}: PeriodGradePopoverProps) {
  const [open, setOpen] = useState(false);
  const { isReadOnly } = useJournalAccess();
  const { mutate: create, isPending: isCreating } = useCreatePeriodGrade();
  const { mutate: remove, isPending: isDeleting } = useDeletePeriodGrade();
  const isPending = isCreating || isDeleting;

  const close = () => setOpen(false);

  const handleGradeClick = (value: number) => {
    if (periodGrade?.id) remove(periodGrade.id);
    create(
      { value, description: null, teachingAssignmentId, studentId, academicPeriodId },
      { onSuccess: close }
    );
  };

  const handleDelete = () => {
    if (periodGrade?.id) remove(periodGrade.id, { onSuccess: close });
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
        <div className="w-full h-full flex items-center justify-center cursor-pointer group">
          {periodGrade ? (
            <span className={cn(
              "w-[34px] h-[34px] rounded-[10px] flex items-center justify-center font-serif text-[16px] font-bold ring-1 ring-black/[0.06] transition-transform group-hover:scale-110 shadow-sm",
              GRADE_STYLE[periodGrade.value]
            )}>
              {periodGrade.value}
            </span>
          ) : (
            <span className="w-[34px] h-[34px] rounded-[10px] flex items-center justify-center ring-1 ring-black/[0.06] bg-black/[0.02] opacity-0 group-hover:opacity-100 transition-opacity text-black/20 text-[13px] font-bold">
              +
            </span>
          )}
        </div>
      </PopoverTrigger>

      {!isReadOnly && (
        <PopoverContent className="w-[160px] p-3 rounded-2xl shadow-2xl border border-black/[0.06] bg-white/95 backdrop-blur-xl flex flex-col gap-3">
          <p className="text-[9px] font-extrabold uppercase tracking-[0.2em] text-black/30 text-center">
            Четвертная оценка
          </p>

          <div className="grid grid-cols-4 gap-1.5">
            {[2, 3, 4, 5].map((val) => (
              <button
                key={val}
                onClick={() => handleGradeClick(val)}
                disabled={isPending}
                className={cn(
                  "h-9 rounded-lg flex items-center justify-center font-serif text-[14px] font-bold border border-black/[0.05] transition-all hover:scale-105 active:scale-95 disabled:opacity-50",
                  periodGrade?.value === val ? "ring-2 ring-[var(--navy)] ring-offset-1" : "",
                  GRADE_STYLE[val]
                )}
              >
                {val}
              </button>
            ))}
          </div>

          {periodGrade && (
            <button
              onClick={handleDelete}
              disabled={isPending}
              className="w-full py-1.5 text-[10px] font-bold text-red-400 hover:text-red-600 transition-colors bg-red-50/50 rounded-lg disabled:opacity-50"
            >
              Удалить оценку
            </button>
          )}
        </PopoverContent>
      )}
    </Popover>
  );
}