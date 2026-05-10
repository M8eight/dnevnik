import { ACCENT_PALETTE } from "@/constants/component-constants";
import { cn } from "@/lib/utils";
import type { AcademicPeriodResponse } from "@/services/grade-service";
import {  Hash, Loader2, LockOpen, Lock, Trash2, CalendarDays } from "lucide-react";
import { useState } from "react";

function formatDate(iso: string) {
    return new Date(iso).toLocaleDateString("ru-RU", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
    });
}

export default function PeriodCard({
    period,
    index,
    onDelete,
    onToggle,
    isDeleting,
    isToggling,
}: {
    period: AcademicPeriodResponse;
    index: number;
    onDelete: (id: number) => void;
    onToggle: (period: AcademicPeriodResponse) => void;
    isDeleting: boolean;
    isToggling: boolean;
}) {
    const accent = ACCENT_PALETTE[index % ACCENT_PALETTE.length];
    const [confirmDelete, setConfirmDelete] = useState(false);

    const handleDeleteClick = () => {
        if (confirmDelete) {
            onDelete(period.id);
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
                    <CalendarDays className={cn("w-5 h-5", (accent as any).icon)} />
                </div>
                        
                {/* Info */}
                <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                        <p className="font-black text-[var(--navy)] text-[15px] leading-snug truncate">
                            {period.name}
                        </p>
                        {/* Status badge */}
                        <span
                            className={cn(
                                "text-[10px] font-extrabold px-2.5 py-0.5 rounded-full tracking-wider uppercase",
                                period.isClosed
                                    ? "bg-black/5 text-black/35"
                                    : "bg-emerald-50 text-emerald-600 ring-1 ring-emerald-200"
                            )}
                        >
                            {period.isClosed ? "Закрыта" : "Открыта"}
                        </span>
                    </div>

                    <div className="flex items-center gap-3 mt-1.5 flex-wrap">
                        <div className="flex items-center gap-1">
                            <Hash className="w-3 h-3 text-black/20" />
                            <span className="text-[11px] font-bold text-black/30 tracking-wider">
                                {period.schoolYear}
                            </span>
                        </div>
                        <span className="text-[11px] font-semibold text-black/30">
                            {formatDate(period.startDate)} — {formatDate(period.endDate)}
                        </span>
                    </div>
                </div>

                {/* Actions */}
                <div className="flex items-center gap-1.5 shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
                    {/* Open / Close toggle */}
                    <button
                        onClick={() => onToggle(period)}
                        disabled={isToggling}
                        title={period.isClosed ? "Открыть четверть" : "Закрыть четверть"}
                        className={cn(
                            "w-8 h-8 rounded-[10px] flex items-center justify-center transition-all",
                            period.isClosed
                                ? "bg-emerald-50 hover:bg-emerald-100 text-emerald-600"
                                : "bg-black/5 hover:bg-amber-50 text-black/35 hover:text-amber-500"
                        )}
                    >
                        {isToggling ? (
                            <Loader2 className="w-3.5 h-3.5 animate-spin" />
                        ) : period.isClosed ? (
                            <LockOpen className="w-3.5 h-3.5" />
                        ) : (
                            <Lock className="w-3.5 h-3.5" />
                        )}
                    </button>

                    {/* Delete */}
                    <button
                        onClick={handleDeleteClick}
                        disabled={isDeleting}
                        title={confirmDelete ? "Нажмите ещё раз для подтверждения" : "Удалить четверть"}
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
                </div>
            </div>

            {confirmDelete && (
                <p className="text-[11px] text-[var(--red)] font-bold mt-2 pl-[60px]">
                    Нажмите ещё раз для удаления
                </p>
            )}
        </div>
    );
}