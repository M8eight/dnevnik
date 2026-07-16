import { useState } from "react";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { GradeBadge } from "@/components/student/diary/badges";
import { useGradeDetail } from "@/hooks/use-grade";
import { Calendar } from "lucide-react";

const GRADE_TYPE_LABELS: Record<string, string> = {
  HOMEWORK: "Домашняя работа",
  TEST: "Работа на уроке",
  CONTROL: "Контрольная работа",
};

const formatDateTimeLabel = (dateStr: string) => {
  const d = new Date(dateStr);
  const date = d.toLocaleDateString("ru-RU", { day: "numeric", month: "long" });
  const time = d.toLocaleTimeString("ru-RU", { hour: "2-digit", minute: "2-digit" });
  return `${date} · ${time}`;
};

const initials = (firstName: string, lastName: string) =>
  `${firstName?.[0] ?? ""}${lastName?.[0] ?? ""}`.toUpperCase();

// Обновленные стили, синхронизированные с GradeBadge
const gradeStyles: Record<number, { bg: string; text: string; ring: string }> = {
  5: { bg: "bg-emerald-50", text: "text-emerald-600", ring: "ring-emerald-600/15" },
  4: { bg: "bg-amber-50",   text: "text-amber-500",   ring: "ring-amber-500/15" },
  3: { bg: "bg-red-50",     text: "text-red-500",     ring: "ring-red-500/15" },
  2: { bg: "bg-red-50",     text: "text-red-600",     ring: "ring-red-600/15" },
};

// Функция для получения стилей (с дефолтным значением на случай непредвиденной оценки)
const getGradeStyle = (value: number) => 
  gradeStyles[value] || { bg: "bg-gray-50", text: "text-gray-500", ring: "ring-black/6" };

// Добавлен проп size
export function GradePopover({ gradeId, value, size = "md" }: { gradeId: number; value: number; size?: "sm" | "md" }) {
  const [open, setOpen] = useState(false);
  const { data, isLoading } = useGradeDetail(gradeId, open);
  const colors = getGradeStyle(value);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild onClick={(e) => e.stopPropagation()}>
        <button type="button" className="cursor-pointer">
          {/* Прокидываем size в сам Badge */}
          <GradeBadge grade={value} size={size} />
        </button>
      </PopoverTrigger>

      <PopoverContent
        align="end"
        sideOffset={8}
        className="w-72 rounded-[18px] border border-black/6 bg-white p-0 shadow-[0_8px_30px_rgba(30,42,68,0.12)] overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        {isLoading || !data ? (
          <div className="p-5 flex flex-col gap-3 animate-pulse">
            <div className="h-10 w-full rounded-[12px] bg-black/6" />
            <div className="h-3.5 w-40 rounded bg-black/6" />
            <div className="h-3.5 w-32 rounded bg-black/6" />
          </div>
        ) : (
          <>
            {/* Оценка + тип */}
            <div className={`flex items-center gap-3 px-5 py-4 ${colors.bg}`}>
              <span
                className={`flex items-center justify-center w-10 h-10 rounded-[12px] text-[18px] font-black bg-white ring-1 shrink-0 ${colors.text} ${colors.ring}`}
              >
                {data.value}
              </span>
              <div className="min-w-0">
                <p className="font-bold text-[13px] text-(--navy) leading-tight">
                  {GRADE_TYPE_LABELS[data.type] ?? data.type}
                </p>
                <p className="text-[11px] text-black/40 mt-0.5">Вес оценки: {data.weight}</p>
              </div>
            </div>

            {/* Учитель и дата */}
            <div className="px-5 py-4 flex flex-col gap-3.5">
              <div className="flex items-center gap-2.5">
                <span className="flex items-center justify-center w-7 h-7 rounded-full bg-(--navy-light)/40 text-[10px] font-bold text-(--navy) shrink-0">
                  {initials(data.teacher.firstName, data.teacher.lastName)}
                </span>
                <div className="min-w-0">
                  <p className="text-[13px] font-semibold text-(--navy) leading-tight truncate">
                    {data.teacher.firstName} {data.teacher.lastName}
                  </p>
                  <p className="text-[11px] text-black/35">Преподаватель</p>
                </div>
              </div>

              <div className="flex items-center gap-2.5">
                <span className="flex items-center justify-center w-7 h-7 rounded-full bg-black/4 shrink-0">
                  <Calendar className="w-3.5 h-3.5 text-black/40" />
                </span>
                <p className="text-[12px] text-black/50">{formatDateTimeLabel(data.date)}</p>
              </div>
            </div>
          </>
        )}
      </PopoverContent>
    </Popover>
  );
}