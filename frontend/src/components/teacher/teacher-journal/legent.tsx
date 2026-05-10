import { ATTENDANCE_LEGEND_ITEMS, LEGEND_ITEMS } from "@/constants/component-constants";

export default function Legend() {
  return (
    <div className="mt-6 glass-card rounded-[22px] p-5 flex flex-wrap gap-x-8 gap-y-4 items-center justify-center text-[11px] font-extrabold text-black/40 uppercase tracking-[0.1em] border-none shadow-sm backdrop-blur-md">
      {LEGEND_ITEMS.map(({ bg, ring, color, label, desc, serif }) => (
        <div key={label} className="flex items-center gap-2">
          <span
            className={`w-4 h-4 rounded ${bg} ring-1 ${ring} flex items-center justify-center ${color} ${serif ? "font-serif" : ""} text-[12px]`}
          >
            {label}
          </span>
          <span>{desc}</span>
        </div>
      ))}
      <div className="w-px h-4 bg-black/10 hidden md:block" />
      {ATTENDANCE_LEGEND_ITEMS.map(({ bg, ring, color, label, desc }) => (
        <div key={label} className="flex items-center gap-2">
          <span
            className={`w-4 h-4 rounded ${bg} ring-1 ${ring} flex items-center justify-center ${color} text-[9px]`}
          >
            {label}
          </span>
          <span>{desc}</span>
        </div>
      ))}
    </div>
  );
}