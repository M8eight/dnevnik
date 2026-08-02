interface ClassColor {
    bg: string;
    text: string;
    border: string;
    dot: string;
}

const CLASS_COLOR_PALETTE: ClassColor[] = [
    { bg: "bg-sky-50", text: "text-sky-700", border: "border-sky-200", dot: "bg-sky-400" },
    { bg: "bg-emerald-50", text: "text-emerald-700", border: "border-emerald-200", dot: "bg-emerald-400" },
    { bg: "bg-violet-50", text: "text-violet-700", border: "border-violet-200", dot: "bg-violet-400" },
    { bg: "bg-amber-50", text: "text-amber-700", border: "border-amber-200", dot: "bg-amber-400" },
    { bg: "bg-rose-50", text: "text-rose-700", border: "border-rose-200", dot: "bg-rose-400" },
    { bg: "bg-teal-50", text: "text-teal-700", border: "border-teal-200", dot: "bg-teal-400" },
    { bg: "bg-indigo-50", text: "text-indigo-700", border: "border-indigo-200", dot: "bg-indigo-400" },
    { bg: "bg-fuchsia-50", text: "text-fuchsia-700", border: "border-fuchsia-200", dot: "bg-fuchsia-400" },
];

export function getClassColor(classId: number): ClassColor {
    const index = ((classId % CLASS_COLOR_PALETTE.length) + CLASS_COLOR_PALETTE.length) % CLASS_COLOR_PALETTE.length;
    return CLASS_COLOR_PALETTE[index];
}