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
