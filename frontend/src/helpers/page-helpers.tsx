import { format } from "date-fns";

export function toDateKey(date: Date) {
    return format(date, "yyyy-MM-dd");
}