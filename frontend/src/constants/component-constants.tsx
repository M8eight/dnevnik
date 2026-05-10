import type { UserRole } from "@/services/user-service";
import {
    GraduationCap,
    BookUser,
    UserRound,
} from "lucide-react";


{/* Admin */}
export const ACCENT_PALETTE = [
    { light: "bg-[var(--red-light)]/60", dot: "bg-[var(--red)]", text: "text-[var(--red)]" },
    { light: "bg-blue-50/70", dot: "bg-blue-500", text: "text-blue-600" },
    { light: "bg-emerald-50/70", dot: "bg-emerald-500", text: "text-emerald-600" },
    { light: "bg-violet-50/70", dot: "bg-violet-500", text: "text-violet-600" },
    { light: "bg-amber-50/70", dot: "bg-amber-500", text: "text-amber-600" },
    { light: "bg-pink-50/70", dot: "bg-pink-500", text: "text-pink-600" },
];

export const ROLES: {
    value: UserRole;
    label: string;
    icon: React.ReactNode;
    color: string;
    iconBg: string;
}[] = [
        {
            value: "STUDENT",
            label: "Ученик",
            icon: <GraduationCap className="w-4 h-4" />,
            color: "text-blue-600",
            iconBg: "bg-blue-50/70",
        },
        {
            value: "PARENT",
            label: "Родитель",
            icon: <UserRound className="w-4 h-4" />,
            color: "text-violet-600",
            iconBg: "bg-violet-50/70",
        },
        {
            value: "TEACHER",
            label: "Учитель",
            icon: <BookUser className="w-4 h-4" />,
            color: "text-emerald-600",
            iconBg: "bg-emerald-50/70",
        },
    ];


{/* Teacher */}
export type ViewMode = "ALL" | "GRADES" | "ATTENDANCE";

export const WEEKDAYS = ["Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"];

export const ATTENDANCE_LABEL: Record<string, string> = {
  ABSENT: "Н",
  LATE: "ОП",
  EXCUSED: "УП",
};

export const ATTENDANCE_STYLE: Record<string, string> = {
  Н: "bg-red-50 text-red-500 ring-red-100",
  ОП: "bg-amber-50 text-amber-500 ring-amber-100",
  УП: "bg-violet-50 text-violet-500 ring-violet-100",
};

export const GRADE_STYLE: Record<number, string> = {
  5: "bg-emerald-50 text-emerald-600 ring-emerald-100",
  4: "bg-amber-50 text-amber-500 ring-amber-100",
  3: "bg-orange-50 text-orange-500 ring-orange-100",
  2: "bg-red-50 text-red-600 ring-red-100",
};

export const GRADE_TYPES = [
  { value: "TEST", label: "Тест" },
  { value: "CONTROL", label: "Контрольная" },
  { value: "HOMEWORK", label: "Домашняя" },
];

export const WEIGHT_OPTIONS = [
  { value: "1", label: "Вес: 1" },
  { value: "2", label: "Вес: 2" },
  { value: "3", label: "Вес: 3" },
  { value: "4", label: "Вес: 4" },
];

export const ATTENDANCE_OPTIONS = [
  { value: "ABSENT", label: "Н" },
  { value: "LATE", label: "ОП" },
  { value: "EXCUSED", label: "УП" },
];

export const VIEW_MODE_OPTIONS: { id: ViewMode; label: string }[] = [
  { id: "ALL", label: "Всё" },
  { id: "GRADES", label: "Оценки" },
  { id: "ATTENDANCE", label: "Посещаемость" },
];

export const LEGEND_ITEMS = [
  { bg: "bg-emerald-50", ring: "ring-emerald-100", color: "text-emerald-600", label: "5", desc: "Отлично", serif: true },
  { bg: "bg-amber-50", ring: "ring-amber-100", color: "text-amber-500", label: "4", desc: "Хорошо", serif: true },
  { bg: "bg-orange-50", ring: "ring-orange-100", color: "text-orange-500", label: "3", desc: "Удовлетв.", serif: true },
  { bg: "bg-red-50", ring: "ring-red-100", color: "text-red-600", label: "2", desc: "Неудовлетв.", serif: true },
];

export const ATTENDANCE_LEGEND_ITEMS = [
  { bg: "bg-red-50", ring: "ring-red-100", color: "text-red-500", label: "Н", desc: "Не был" },
  { bg: "bg-amber-50", ring: "ring-amber-100", color: "text-amber-500", label: "ОП", desc: "Опоздал" },
  { bg: "bg-violet-50", ring: "ring-violet-100", color: "text-violet-500", label: "УП", desc: "Уваж. причина" },
];


{/* Student */}
export const DAYS_MAP = [
  { key: "MONDAY",    label: "Пн" },
  { key: "TUESDAY",   label: "Вт" },
  { key: "WEDNESDAY", label: "Ср" },
  { key: "THURSDAY",  label: "Чт" },
  { key: "FRIDAY",    label: "Пт" },
];

export const RUSSIAN_DAYS: Record<string, string> = {
  MONDAY:    "Понедельник",
  TUESDAY:   "Вторник",
  WEDNESDAY: "Среда",
  THURSDAY:  "Четверг",
  FRIDAY:    "Пятница",
  SATURDAY:  "Суббота",
  SUNDAY:    "Воскресенье",
};