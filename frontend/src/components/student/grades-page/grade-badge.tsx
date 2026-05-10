export default function GradeBadge({ grade, size = "md" }: { grade?: number; size?: "sm" | "md" }) {
  if (!grade) {
    return (
      <div className="w-full flex justify-center items-center h-[32px]">
        <span className="text-black/10 font-serif text-lg">·</span>
      </div>
    );
  }

  const styles: Record<number, string> = {
    5: "bg-emerald-50 text-emerald-600",
    4: "bg-amber-50   text-amber-500",
    3: "bg-red-50     text-red-500",
    2: "bg-red-50     text-red-600",
  };

  const sizeClass = size === "md" ? "w-[36px] h-[36px] text-[17px]" : "w-[28px] h-[28px] text-[14px]";

  return (
    <span className={`${sizeClass} ${styles[grade] ?? "bg-gray-50 text-gray-500"} rounded-[10px] flex items-center justify-center font-serif font-bold flex-shrink-0 ring-1 ring-black/[0.06] transition-all duration-200 cursor-default active:scale-90`}>
      {grade}
    </span>
  );
}