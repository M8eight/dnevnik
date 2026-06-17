export function CurrentDate() {
  const now = new Date();
  const dayName = now.toLocaleDateString("ru-RU", { weekday: "long" });
  const day = now.getDate();
  const monthYear = now.toLocaleDateString("ru-RU", { month: "long", year: "numeric" });
  const monthYearCap = monthYear.charAt(0).toUpperCase() + monthYear.slice(1);

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

