import { format } from "date-fns";
import { ru } from "date-fns/locale";
import { capitalizeFirst } from "@/lib/date";

export function CurrentDate() {
  const now = new Date();
  const dayName = format(now, "EEEE", { locale: ru });
  const day = format(now, "d");
  const monthYear = format(now, "LLLL yyyy", { locale: ru });
  const monthYearCap = capitalizeFirst(monthYear);

  return (
    <div className="text-right text-[10px] font-extrabold text-black/30 uppercase tracking-[0.2em]">
      {dayName}
      <strong className="block font-serif text-[2rem] font-black text-[var(--navy)] normal-case tracking-normal leading-tight">
        {day}
      </strong>
      {monthYearCap}
    </div>
  );
}

