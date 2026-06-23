import type { GradeJournalDto } from "@/services/teacher-journal-service";
import { format } from "date-fns/format";
import { ru } from "date-fns/locale";
import { useEffect, type RefObject } from "react";

export const formatColDate = (dateStr: string) =>
  format(new Date(dateStr), "dd MMM", { locale: ru });

export const formatColDay = (dateStr: string) =>
  format(new Date(dateStr), "EEEEEE", { locale: ru }).toUpperCase();

export const calcAvg = (grades: GradeJournalDto[]): number | null => {
  if (!grades.length) return null;
  const avg = grades.reduce((s, g) => s + g.value, 0) / grades.length;
  return parseFloat(avg.toFixed(1));
};

export const avgStyle = (avg: number | null): string => {
  if (avg === null || avg === undefined) return "text-black/25";
  if (avg >= 4.5) return "text-emerald-600 font-black";
  if (avg >= 3.5) return "text-amber-500 font-black";
  if (avg >= 2.5) return "text-orange-500 font-bold";
  return "text-red-600 font-bold";
};



/**
 * Универсальный drag-to-scroll по горизонтали (мышь + touch).
 * Навешивается на скроллящийся контейнер (overflow-x: auto / scroll).
 *
 * - Игнорирует начало драга на интерактивных элементах (button, a, input,
 *   select, radix-поповеры/диалоги), чтобы не мешать обычным кликам.
 * - Гасит "фантомный" click после реального перетаскивания (hasMoved),
 *   иначе отпускание мыши после драга может восприняться как клик по ячейке.
 * - Touch: ось (горизонталь/вертикаль) определяется по первому движению,
 *   чтобы не блокировать обычный вертикальный скролл страницы пальцем.
 */
export function useHorizontalScrollDrag(ref: RefObject<HTMLDivElement | null>) {
    useEffect(() => {
        const el = ref.current;
        if (!el) return;

        let isDragging = false;
        let startX = 0;
        let scrollLeft = 0;
        let hasMoved = false;

        const isInteractive = (target: EventTarget | null) =>
            !!(target as HTMLElement)?.closest(
                "button, a, input, select, [role='dialog'], [data-radix-popper-content-wrapper]"
            );

        const onMouseDown = (e: MouseEvent) => {
            if (isInteractive(e.target)) return;
            isDragging = true;
            hasMoved = false;
            startX = e.clientX;
            scrollLeft = el.scrollLeft;
            el.style.cursor = "grabbing";
            el.style.userSelect = "none";
        };

        const onMouseMove = (e: MouseEvent) => {
            if (!isDragging) return;
            const dx = e.clientX - startX;
            if (Math.abs(dx) > 3) hasMoved = true;
            el.scrollLeft = scrollLeft - dx;
        };

        const stopDrag = () => {
            if (!isDragging) return;
            isDragging = false;
            el.style.cursor = "grab";
            el.style.userSelect = "";
        };

        const onClickCapture = (e: MouseEvent) => {
            if (hasMoved) e.stopPropagation();
        };

        let touchStartX = 0;
        let touchStartY = 0;
        let touchScrollLeft = 0;
        let touchAxis: "h" | "v" | null = null;

        const onTouchStart = (e: TouchEvent) => {
            touchStartX = e.touches[0].clientX;
            touchStartY = e.touches[0].clientY;
            touchScrollLeft = el.scrollLeft;
            touchAxis = null;
        };

        const onTouchMove = (e: TouchEvent) => {
            const dx = e.touches[0].clientX - touchStartX;
            const dy = e.touches[0].clientY - touchStartY;
            if (!touchAxis) touchAxis = Math.abs(dx) > Math.abs(dy) ? "h" : "v";
            if (touchAxis === "h") {
                e.preventDefault();
                el.scrollLeft = touchScrollLeft - dx;
            }
        };

        el.addEventListener("mousedown", onMouseDown);
        el.addEventListener("mousemove", onMouseMove);
        el.addEventListener("mouseup", stopDrag);
        el.addEventListener("mouseleave", stopDrag);
        el.addEventListener("click", onClickCapture, true);
        el.addEventListener("touchstart", onTouchStart, { passive: true });
        el.addEventListener("touchmove", onTouchMove, { passive: false });
        window.addEventListener("mouseup", stopDrag);

        return () => {
            el.removeEventListener("mousedown", onMouseDown);
            el.removeEventListener("mousemove", onMouseMove);
            el.removeEventListener("mouseup", stopDrag);
            el.removeEventListener("mouseleave", stopDrag);
            el.removeEventListener("click", onClickCapture, true);
            el.removeEventListener("touchstart", onTouchStart);
            el.removeEventListener("touchmove", onTouchMove);
            window.removeEventListener("mouseup", stopDrag);
        };
    }, [ref]);
}