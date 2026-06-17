import type { GradeLessonDto } from "@/services/grade-service";

export const avgColor = (avg: number | null) => {
    if (!avg) return "text-black/20";
    if (avg >= 4.5) return "text-emerald-600";
    if (avg >= 3.5) return "text-amber-500";
    if (avg >= 2.5) return "text-orange-500";
    return "text-red-500";
};

export const formatDate = (dateStr: string) => {
    const [, month, day] = dateStr.split("-");
    return `${day}.${month}`;
};

export const calculateWeightedAvg = (grades: GradeLessonDto[]) => {
    if (grades.length === 0) return 0;
    const totalValue = grades.reduce((acc, g) => acc + g.value * g.weight, 0);
    const totalWeight = grades.reduce((acc, g) => acc + g.weight, 0);
    return totalValue / totalWeight;
};

export const formatAvg = (val: number) => (val === 0 ? "—" : parseFloat(val.toFixed(2)).toString());
