export const WEEK_DAY_LABELS = ["Пн", "Вт", "Ср", "Чт", "Пт", "Сб"] as const;
export const WEEK_DAYS_COUNT = WEEK_DAY_LABELS.length;

export function toISODate(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, "0");
    const d = String(date.getDate()).padStart(2, "0");
    return `${y}-${m}-${d}`;
}

export function addDays(date: Date, days: number): Date {
    const d = new Date(date);
    d.setDate(d.getDate() + days);
    return d;
}

export function getWeekStart(date: Date): Date {
    const d = new Date(date);
    d.setHours(0, 0, 0, 0);
    const day = d.getDay();
    const diffToMonday = day === 0 ? -6 : 1 - day;
    d.setDate(d.getDate() + diffToMonday);
    return d;
}

export function getWeekRange(date: Date): { start: Date; end: Date } {
    const start = getWeekStart(date);
    const nextWeekStart = addDays(start, WEEK_DAYS_COUNT);
    const end = addDays(nextWeekStart, -1);
    return { start, end };
}

export function formatWeekRangeLabel(start: Date, end: Date): string {
    const sameMonth = start.getMonth() === end.getMonth();
    const startLabel = start.toLocaleDateString("ru-RU", {
        day: "numeric",
        month: sameMonth ? undefined : "long",
    });
    const endLabel = end.toLocaleDateString("ru-RU", { day: "numeric", month: "long" });
    return `${startLabel} – ${endLabel}`;
}

const DAY_OF_WEEK_KEYS = [
    "SUNDAY",
    "MONDAY",
    "TUESDAY",
    "WEDNESDAY",
    "THURSDAY",
    "FRIDAY",
    "SATURDAY",
] as const;

export function getDayOfWeekKey(date: Date): string {
    return DAY_OF_WEEK_KEYS[date.getDay()];
}

export function isSameDay(a: Date, b: Date): boolean {
    return (
        a.getFullYear() === b.getFullYear() &&
        a.getMonth() === b.getMonth() &&
        a.getDate() === b.getDate()
    );
}