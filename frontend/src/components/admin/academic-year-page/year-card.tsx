import { ACCENT_PALETTE } from "@/constants/component-constants";
import { cn } from "@/lib/utils";
import type { AcademicYearResponse } from "@/services/academic-year-service";
import { Loader2, Trash2, CalendarDays, CheckCircle2 } from "lucide-react";
import { useState } from "react";

function formatDate(iso: string) {
    return new Date(iso).toLocaleDateString("ru-RU", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
    });
}

export default function YearCard({
    year,
    index,
    onDelete,
    onToggleActive,
    isDeleting,
    isSettingActive,
}: {
    year: AcademicYearResponse;
    index: number;
    onDelete?: (id: number) => void;
    onToggleActive?: (year: AcademicYearResponse) => void;
    isDeleting: boolean;
    isSettingActive: boolean;
}) {
    const accent = ACCENT_PALETTE[index % ACCENT_PALETTE.length];
    const [confirmDelete, setConfirmDelete] = useState(false);

    const handleDeleteClick = () => {
        if (!onDelete) return;

        if (confirmDelete) {
            onDelete(year.id);
        } else {
            setConfirmDelete(true);
            setTimeout(() => setConfirmDelete(false), 3000);
        }
    };

    return (
        <div className="group glass-card rounded-[24px] p-5 backdrop-blur-md border border-white/40 hover:border-white/60 transition-all hover:shadow-xl hover:-translate-y-0.5">
            <div className="flex items-start gap-4">
                {/* Icon */}
                <div
                    className={cn(
                        "w-11 h-11 rounded-[14px] flex items-center justify-center shrink-0 ring-1 ring-black/5",
                        accent.light
                    )}
                >
                    <CalendarDays className={cn("w-5 h-5")} />
                </div>

                {/* Info */}
                <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                        <p className="font-black text-[var(--navy)] text-[15px] leading-snug truncate">
                            {year.name}
                        </p>
                        {/* Status badge */}
                        <span
                            className={cn(
                                "text-[10px] font-extrabold px-2.5 py-0.5 rounded-full tracking-wider uppercase",
                                !year.isActive
                                    ? "bg-black/5 text-black/35"
                                    : "bg-emerald-50 text-emerald-600 ring-1 ring-emerald-200"
                            )}
                        >
                            {year.isActive ? "Активен" : "Архив"}
                        </span>
                    </div>

                    <div className="flex items-center gap-3 mt-1.5 flex-wrap">
                        <span className="text-[11px] font-semibold text-black/30">
                            {formatDate(year.startDate)} — {formatDate(year.endDate)}
                        </span>
                    </div>
                </div>

                {/* Actions */}
                {(onToggleActive || onDelete) && (
                    <div className="flex items-center gap-1.5 shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
                        
                        {/* Toggle Active Button (Теперь показывается всегда) */}
                        {onToggleActive && (
                            <button
                                onClick={() => onToggleActive(year)}
                                disabled={isSettingActive}
                                title={year.isActive ? "Перенести в архив (деактивировать)" : "Сделать год активным"}
                                className={cn(
                                    "w-8 h-8 rounded-[10px] flex items-center justify-center transition-all",
                                    year.isActive
                                        ? "bg-emerald-50 hover:bg-amber-50 text-emerald-600 hover:text-amber-500" // Зеленая, при наведении желтеет (типа отключаем)
                                        : "bg-black/5 hover:bg-emerald-50 text-black/35 hover:text-emerald-500"   // Серая, при наведении зеленеет (включаем)
                                )}
                            >
                                {isSettingActive ? (
                                    <Loader2 className="w-3.5 h-3.5 animate-spin" />
                                ) : (
                                    <CheckCircle2 className="w-3.5 h-3.5" />
                                )}
                            </button>
                        )}

                        {/* Delete */}
                        {onDelete && (
                            <button
                                onClick={handleDeleteClick}
                                disabled={isDeleting}
                                title={confirmDelete ? "Нажмите ещё раз для подтверждения" : "Удалить учебный год"}
                                className={cn(
                                    "w-8 h-8 rounded-[10px] flex items-center justify-center transition-all",
                                    confirmDelete
                                        ? "bg-[var(--red)] text-white scale-105"
                                        : "bg-black/5 hover:bg-red-50 text-black/25 hover:text-[var(--red)]"
                                )}
                            >
                                {isDeleting ? (
                                    <Loader2 className="w-3.5 h-3.5 animate-spin" />
                                ) : (
                                    <Trash2 className="w-3.5 h-3.5" />
                                )}
                            </button>
                        )}
                    </div>
                )}
            </div>

            {/* Подтверждение удаления */}
            {confirmDelete && onDelete && (
                <p className="text-[11px] text-[var(--red)] font-bold mt-2 pl-[60px]">
                    Нажмите ещё раз для удаления
                </p>
            )}
        </div>
    );
}