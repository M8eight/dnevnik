import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { type ViewMode, GRADE_STYLE, ATTENDANCE_STYLE, ATTENDANCE_LABEL, ATTENDANCE_OPTIONS, GRADE_TYPES } from "@/constants/component-constants";
import { useCreateAttendance, useDeleteAttendance } from "@/hooks/use-attendance";
import { useCreateGrade, useDeleteGrade } from "@/hooks/use-grade";
import { useJournalAccess } from "@/hooks/use-journal-access";
import { cn } from "@/lib/utils";
import type { GradeJournalDto, AttendanceJournalDto } from "@/services/teacher-journal-service";
import { useState } from "react";
import { X } from "lucide-react";

interface GradePopoverProps {
  grades: GradeJournalDto[];
  attendances: AttendanceJournalDto[];
  studentId: number;
  lessonInstanceId: number;
  academicPeriodId: number;
  gradeType: string;
  gradeWeight: number;
  viewMode: ViewMode;
}

// Сколько "значков" (оценка/отметка вместе) показываем в самой ячейке,
// прежде чем свернуть остальное в бейдж "+N". Подобрано под текущий
// размер ячейки (min-w-16 / h-17.5) с уменьшенными кружками 22px.
const MAX_VISIBLE_BADGES = 3;

export default function GradePopover({
  grades,
  attendances,
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

  const showGrades = (viewMode === "ALL" || viewMode === "GRADES") ? grades : [];
  const showAttendances = (viewMode === "ALL" || viewMode === "ATTENDANCE") ? attendances : [];
  const isEmpty = showGrades.length === 0 && showAttendances.length === 0;

  // Единый список значков для отображения в ячейке: сначала оценки, потом посещаемость.
  // При превышении лимита — обрезаем и считаем остаток для бейджа "+N".
  const allBadges = [
    ...showGrades.map((g) => ({ kind: "grade" as const, data: g })),
    ...showAttendances.map((a) => ({ kind: "attendance" as const, data: a })),
  ];
  const visibleBadges = allBadges.slice(0, MAX_VISIBLE_BADGES);
  const hiddenCount = allBadges.length - visibleBadges.length;

  const close = () => setOpen(false);

  const handleGradeClick = (value: number) => {
    // Всегда добавляет новую оценку в список, не заменяет существующие.
    createGrade(
      { studentId, lessonInstanceId, academicPeriodId, value, weight: gradeWeight, gradeType },
      { onSuccess: close }
    );
  };

  const handleAttendanceClick = (status: string) => {
    createAttendance({ studentId, lessonInstanceId, status }, { onSuccess: close });
  };

  // Компактный размер значков, когда их больше одного — чтобы 2-3 уместились в ряд без переполнения ячейки.
  const badgeSizeClass = visibleBadges.length > 1 ? "w-[22px] h-[22px] text-[12px]" : "w-[30px] h-[30px] text-[15px]";

  return (
    <Popover open={open && !isReadOnly} onOpenChange={(newOpen) => {
      if (isReadOnly) {
        setOpen(false);
      } else {
        setOpen(newOpen);
      }
    }}>
      <PopoverTrigger asChild>
        <div className="w-full h-full flex flex-wrap items-center justify-center gap-1 py-1 px-0.5 cursor-pointer group">
          {visibleBadges.map((badge) =>
            badge.kind === "grade" ? (
              <span
                key={`g-${badge.data.gradeId}`}
                className={cn(
                  "rounded-[8px] flex items-center justify-center font-serif font-bold ring-1 ring-black/6 transition-transform group-hover:scale-110 shadow-sm",
                  badgeSizeClass,
                  GRADE_STYLE[badge.data.value] ?? "bg-gray-50"
                )}
              >
                {badge.data.value}
              </span>
            ) : (
              <span
                key={`a-${badge.data.attendanceId}`}
                className={cn(
                  "rounded-[8px] flex items-center justify-center font-extrabold ring-1 ring-black/6 shadow-sm",
                  badgeSizeClass,
                  ATTENDANCE_STYLE[ATTENDANCE_LABEL[badge.data.status]]
                )}
              >
                {ATTENDANCE_LABEL[badge.data.status]}
              </span>
            )
          )}
          {hiddenCount > 0 && (
            <span
              className={cn(
                "rounded-[8px] flex items-center justify-center font-extrabold text-[11px] ring-1 ring-black/6 shadow-sm bg-(--navy)/10 text-(--navy)",
                badgeSizeClass
              )}
            >
              +{hiddenCount}
            </span>
          )}
          {isEmpty && (
            <span className="w-7.5 h-7.5 rounded-[8px] flex items-center justify-center ring-1 ring-black/6 bg-black/2 opacity-0 group-hover:opacity-100 transition-opacity text-black/20 text-[11px] font-bold">
              +
            </span>
          )}
        </div>
      </PopoverTrigger>

      {!isReadOnly && (
        <PopoverContent className="w-57.5 p-3 rounded-2xl shadow-2xl border border-black/6 bg-white/95 backdrop-blur-xl flex flex-col gap-3">
          {/* Что уже стоит на этом уроке — оценки и посещаемость вместе, как чипы с крестиком на удаление */}
          {!isEmpty && (
            <div className="space-y-1.5">
              <p className="text-[9px] font-extrabold uppercase tracking-[0.2em] text-black/30">
                На этом уроке
              </p>
              <div className="flex flex-wrap gap-1.5">
                {grades.map((g) => (
                  <button
                    key={`g-${g.gradeId}`}
                    onClick={() => deleteGrade(g.gradeId, { onSuccess: close })}
                    disabled={isLoading}
                    title="Удалить оценку"
                    className={cn(
                      "group/chip h-7 pl-2.5 pr-1.5 rounded-full flex items-center gap-1 font-serif text-[13px] font-bold border border-black/5 transition-all hover:pr-1.5 active:scale-95 disabled:opacity-50",
                      GRADE_STYLE[g.value] ?? "bg-gray-50"
                    )}
                  >
                    {g.value}
                    <X className="w-3 h-3 opacity-40 group-hover/chip:opacity-80 transition-opacity" />
                  </button>
                ))}
                {attendances.map((a) => (
                  <button
                    key={`a-${a.attendanceId}`}
                    onClick={() => deleteAttendance(a.attendanceId, { onSuccess: close })}
                    disabled={isLoading}
                    title="Удалить отметку"
                    className={cn(
                      "group/chip h-7 pl-2.5 pr-1.5 rounded-full flex items-center gap-1 font-extrabold text-[11px] border border-black/5 transition-all active:scale-95 disabled:opacity-50",
                      ATTENDANCE_STYLE[ATTENDANCE_LABEL[a.status]]
                    )}
                  >
                    {ATTENDANCE_LABEL[a.status]}
                    <X className="w-3 h-3 opacity-40 group-hover/chip:opacity-80 transition-opacity" />
                  </button>
                ))}
              </div>
            </div>
          )}

          {!isEmpty && <div className="h-px bg-black/6" />}

          {/* Добавить ещё запись на этот урок — оценки и посещаемость в одном ряду */}
          <div className="space-y-1.5">
            <p className="text-[9px] font-extrabold uppercase tracking-[0.2em] text-black/30">
              {isEmpty ? "Поставить" : "Добавить ещё"}
            </p>
            <div className="flex flex-wrap gap-1.5">
              {[2, 3, 4, 5].map((val) => (
                <button
                  key={val}
                  onClick={() => handleGradeClick(val)}
                  disabled={isLoading}
                  className={cn(
                    "w-9 h-9 rounded-lg flex items-center justify-center font-serif text-[14px] font-bold border border-black/5 transition-all hover:scale-105 active:scale-95 disabled:opacity-50 bg-white",
                    GRADE_STYLE[val]
                  )}
                >
                  {val}
                </button>
              ))}
              <div className="w-px h-9 bg-black/6 mx-0.5" />
              {ATTENDANCE_OPTIONS.map((opt) => (
                <button
                  key={opt.value}
                  onClick={() => handleAttendanceClick(opt.value)}
                  disabled={isLoading}
                  className={cn(
                    "w-9 h-9 rounded-lg flex items-center justify-center font-extrabold text-[11px] border border-black/5 transition-all hover:scale-105 active:scale-95 disabled:opacity-50 bg-white",
                    ATTENDANCE_STYLE[opt.label]
                  )}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          </div>

          <p className="text-[8px] text-black/30 text-center italic">
            Вес: {gradeWeight} • {GRADE_TYPES.find((t) => t.value === gradeType)?.label}
          </p>
        </PopoverContent>
      )}
    </Popover>
  );
}