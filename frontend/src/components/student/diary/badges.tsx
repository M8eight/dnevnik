export function AttendanceBadge({ status }: { status: string }) {
  if (!status) return null;
  
  const styles: Record<string, string> = {
    "Н":  "bg-red-50     text-red-500   ring-red-100",
    "ОП": "bg-amber-50   text-amber-500 ring-amber-100",
    "Б":  "bg-emerald-50 text-emerald-600 ring-emerald-100",
  };
  
  return (
    <span className={`w-[34px] h-[34px] rounded-[10px] flex items-center justify-center font-serif text-[15px] font-bold flex-shrink-0 ring-1 ring-black/[0.06] ${styles[status] ?? "bg-gray-50 text-gray-500"}`}>
      {status}
    </span>
  );
}

export function GradeBadge({ grade, size = "md" }: { grade?: number | null; size?: "sm" | "md" }) {
  if (!grade) {
    return (
      <div className="w-full flex justify-center items-center h-8">
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

  // Определяем классы размеров
  const sizeClass = size === "md" ? "w-[36px] h-[36px] text-[17px]" : "w-[28px] h-[28px] text-[14px]";

  return (
    <span className={`${sizeClass} rounded-[10px] flex items-center justify-center font-serif font-bold shrink-0 ring-1 ring-black/[0.06] transition-all duration-200 cursor-default active:scale-90 ${styles[grade] ?? "bg-gray-50 text-gray-500"}`}>
      {grade}
    </span>
  );
}