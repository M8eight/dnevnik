import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { useCreateFinalGrade, useDeleteFinalGrade } from "@/hooks/use-final-grade";
import { GRADE_STYLE } from "@/constants/component-constants";
import { cn } from "@/lib/utils";
import { useState } from "react";
import type { FinalGradeResponse } from "@/services/final-grade-service";

interface FinalGradePopoverProps {
  finalGrade?: FinalGradeResponse | null;
  studentId: number;
  teachingAssignmentId: number;
  schoolYear: string;
}

export default function FinalGradePopover({
  finalGrade,
  studentId,
  teachingAssignmentId,
  schoolYear,
}: FinalGradePopoverProps) {
  const [open, setOpen] = useState(false);
  const { mutate: create, isPending: isCreating } = useCreateFinalGrade();
  const { mutate: remove, isPending: isDeleting } = useDeleteFinalGrade();
  const isPending = isCreating || isDeleting;

  const close = () => setOpen(false);

  const handleGradeClick = (value: number) => {
    if (finalGrade?.id) remove(finalGrade.id);
    create(
      { 
        value, 
        description: "Итоговая оценка за год", 
        teachingAssignmentId, 
        studentId, 
        schoolYear 
      },
      { onSuccess: close }
    );
  };

  const handleDelete = () => {
    if (finalGrade?.id) remove(finalGrade.id, { onSuccess: close });
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <div className="w-full h-full flex items-center justify-center cursor-pointer group">
          {finalGrade ? (
            <span className={cn(
              "w-[34px] h-[34px] rounded-[10px] flex items-center justify-center font-serif text-[16px] font-bold ring-1 ring-black/[0.06] transition-transform group-hover:scale-110 shadow-sm",
              GRADE_STYLE[finalGrade.value]
            )}>
              {finalGrade.value}
            </span>
          ) : (
            <span className="w-[34px] h-[34px] rounded-[10px] flex items-center justify-center ring-1 ring-black/[0.06] bg-black/[0.02] opacity-0 group-hover:opacity-100 transition-opacity text-black/20 text-[13px] font-bold">
              +
            </span>
          )}
        </div>
      </PopoverTrigger>

      <PopoverContent className="w-[160px] p-3 rounded-2xl shadow-2xl border border-black/[0.06] bg-white/95 backdrop-blur-xl flex flex-col gap-3">
        <p className="text-[9px] font-extrabold uppercase tracking-[0.2em] text-black/30 text-center">
          Годовая оценка
        </p>

        <div className="grid grid-cols-4 gap-1.5">
          {[2, 3, 4, 5].map((val) => (
            <button
              key={val}
              onClick={() => handleGradeClick(val)}
              disabled={isPending}
              className={cn(
                "h-9 rounded-lg flex items-center justify-center font-serif text-[14px] font-bold border border-black/[0.05] transition-all hover:scale-105 active:scale-95 disabled:opacity-50",
                finalGrade?.value === val ? "ring-2 ring-[var(--navy)] ring-offset-1" : "",
                GRADE_STYLE[val]
              )}
            >
              {val}
            </button>
          ))}
        </div>

        {finalGrade && (
          <button
            onClick={handleDelete}
            disabled={isPending}
            className="w-full py-1.5 text-[10px] font-bold text-red-400 hover:text-red-600 transition-colors bg-red-50/50 rounded-lg disabled:opacity-50"
          >
            Удалить оценку
          </button>
        )}
      </PopoverContent>
    </Popover>
  );
}