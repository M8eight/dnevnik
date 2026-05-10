export default function GradeBadge({ grade }: { grade: number }) {
  const styles: Record<number, string> = {
    5: "bg-emerald-50 text-emerald-600 ring-emerald-100",
    4: "bg-amber-50   text-amber-500   ring-amber-100",
    3: "bg-red-50     text-red-500     ring-red-100",
    2: "bg-red-50     text-red-600     ring-red-100",
  };
  return (
    <span className={`w-[36px] h-[36px] rounded-[10px] flex items-center justify-center font-serif text-[17px] font-bold flex-shrink-0 ring-1 ring-black/[0.06] ${styles[grade] ?? "bg-gray-50 text-gray-500"}`}>
      {grade}
    </span>
  );
}
