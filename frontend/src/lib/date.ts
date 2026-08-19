import { format, parse } from "date-fns";
import { ru } from "date-fns/locale";

function toDate(value: Date | string): Date {
  if (value instanceof Date) return value;
  if (/^\d{4}-\d{2}-\d{2}$/.test(value)) return parse(value, "yyyy-MM-dd", new Date());
  return new Date(value);
}

export function formatRuDate(date: Date | string): string {
  return format(toDate(date), "d MMMM", { locale: ru });
}

export function formatRuDateTime(date: Date | string): string {
  return `${formatRuDate(date)} · ${format(toDate(date), "HH:mm")}`;
}

export function formatRuDateShort(date: Date | string): string {
  return format(toDate(date), "dd.MM.yyyy");
}

export function formatRuMonthDay(date: Date | string): string {
  return format(toDate(date), "d MMM", { locale: ru });
}

export function capitalizeFirst(str: string): string {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

export function toISODate(date: Date): string {
  return format(date, "yyyy-MM-dd");
}


export function getCurrentWeekString(): string {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    d.setDate(d.getDate() + 4 - (d.getDay() || 7));
    const yearStart = new Date(d.getFullYear(), 0, 1);
    const weekNo = Math.ceil((((d.getTime() - yearStart.getTime()) / 86400000) + 1) / 7);
    return `${d.getFullYear()}-W${String(weekNo).padStart(2, '0')}`;
}

export function getMondayFromWeekString(weekStr: string): string {
    if (!weekStr || !weekStr.includes("-W")) return toISODate(new Date());
    const [yearStr, weekNumStr] = weekStr.split("-W");
    const year = parseInt(yearStr, 10);
    const week = parseInt(weekNumStr, 10);

    const simple = new Date(year, 0, 4);
    const dayOfWeek = simple.getDay() || 7;
    const monday = new Date(simple);
    monday.setDate(simple.getDate() - dayOfWeek + 1 + (week - 1) * 7);

    return toISODate(monday);
}