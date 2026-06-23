import { ACCENT_PALETTE } from "@/constants/component-constants";
import { cn } from "@/lib/utils";
import type { AcademicPeriodResponse } from "@/services/academic-period-service";
import { Loader2, LockOpen, Lock, Trash2, CalendarDays, Pencil, Check, X } from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

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
    onUpdate,
    isDeleting,
    isToggling,
    isUpdating,
}: {
    period: AcademicPeriodResponse;
    index: number;
    onDelete?: (id: number) => void;
    onToggle?: (period: AcademicPeriodResponse) => void;
    onUpdate?: (id: number, data: { name: string }) => void;
    isDeleting: boolean;
    isToggling: boolean;
    isUpdating?: boolean;
}) {
    const [editing, setEditing] = useState(false);
    const [name, setName] = useState(period.name);

    const accent = ACCENT_PALETTE[index % ACCENT_PALETTE.length];

    const handleSave = (e: React.MouseEvent) => {
        e.stopPropagation();
        if (!name.trim() || !onUpdate) return;
        onUpdate(period.id, { name: name.trim() });
        setEditing(false);
    };

    const handleCancel = (e: React.MouseEvent) => {
        e.stopPropagation();
        setName(period.name);
        setEditing(false);
    };

    const handleEditClick = (e: React.MouseEvent) => {
        e.stopPropagation();
        setEditing(true);
    };

    const handleDeleteClick = (e: React.MouseEvent) => {
        e.stopPropagation();
        if (onDelete) onDelete(period.id);
    };

    const handleToggleClick = (e: React.MouseEvent) => {
        e.stopPropagation();
        if (onToggle) onToggle(period);
    };

    return (
        <div
            className={cn(
                "group flex items-center justify-between gap-4 rounded-[18px] border px-5 py-4 shadow-sm transition-all duration-200 bg-white/50 border-black/5 hover:shadow-md hover:bg-white/70",
                period.isClosed
                    ? "border-amber-300 opacity-80"
                    : ""
            )}
            style={{ animationDelay: `${index * 40}ms` }}
        >
            {/* Left: index badge + info */}
            <div className="flex items-center gap-4 flex-1 min-w-0">
                <div
                    className={cn(
                        "w-11 h-11 rounded-[14px] flex items-center justify-center shrink-0 ring-1 ring-black/5",
                        accent.light
                    )}
                >
                    <CalendarDays className={cn("w-5 h-5")} />
                </div> 

                {editing ? (
                    <div
                        className="flex items-center gap-2 flex-1 min-w-0"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <Input
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            placeholder="Название"
                            className="h-9 text-sm font-semibold bg-white/60 border-black/10 rounded-xl focus-visible:ring-(--red) w-40"
                            autoFocus
                        />
                    </div>
                ) : (
                    <div className="min-w-0">
                        <p className="font-bold text-sm text-(--navy) truncate leading-tight">
                            {period.name}
                        </p>
                        <p className="text-xs text-black/40 mt-0.5 flex items-center gap-1">
                            <CalendarDays className="w-3 h-3" />
                            {formatDate(period.startDate)} — {formatDate(period.endDate)}
                        </p>
                    </div>
                )}
            </div>

            {/* Right: action buttons */}
            <div className="flex items-center gap-1.5 shrink-0">
                {editing ? (
                    <>
                        <Button
                            size="icon"
                            variant="ghost"
                            onClick={handleSave}
                            disabled={isUpdating || !name.trim()}
                            className="w-8 h-8 rounded-xl text-emerald-600 hover:bg-emerald-50"
                        >
                            {isUpdating ? (
                                <Loader2 className="w-4 h-4 animate-spin" />
                            ) : (
                                <Check className="w-4 h-4" />
                            )}
                        </Button>
                        <Button
                            size="icon"
                            variant="ghost"
                            onClick={handleCancel}
                            disabled={isUpdating}
                            className="w-8 h-8 rounded-xl text-black/40 hover:bg-black/5"
                        >
                            <X className="w-4 h-4" />
                        </Button>
                    </>
                ) : (
                    <>

                        {(onUpdate && !period.isClosed) && (
                            <Button
                                size="icon"
                                variant="ghost"
                                onClick={handleEditClick}
                                className="w-8 h-8 rounded-xl text-black/30 hover:text-(--navy) hover:bg-black/5 opacity-0 group-hover:opacity-100 transition-opacity"
                            >
                                <Pencil className="w-3.5 h-3.5" />
                            </Button>
                        )}

                        {onToggle && (
                            <Button
                                size="icon"
                                variant="ghost"
                                onClick={handleToggleClick}
                                disabled={isToggling}
                                className={cn(
                                    "w-8 h-8 rounded-xl opacity-0 group-hover:opacity-100 transition-opacity",
                                    period.isClosed
                                        ? "text-black/30 hover:text-emerald-600 hover:bg-emerald-50"
                                        : "text-black/30 hover:text-amber-600 hover:bg-amber-50"
                                )}
                            >
                                {isToggling ? (
                                    <Loader2 className="w-4 h-4 animate-spin" />
                                ) : period.isClosed ? (
                                    <LockOpen className="w-3.5 h-3.5" />
                                ) : (
                                    <Lock className="w-3.5 h-3.5" />
                                )}
                            </Button>
                        )}

                        {onDelete && (
                            <Button
                                size="icon"
                                variant="ghost"
                                onClick={handleDeleteClick}
                                disabled={isDeleting}
                                className="w-8 h-8 rounded-xl text-black/30 hover:text-red-500 hover:bg-red-50 opacity-0 group-hover:opacity-100 transition-opacity"
                            >
                                {isDeleting ? (
                                    <Loader2 className="w-4 h-4 animate-spin" />
                                ) : (
                                    <Trash2 className="w-3.5 h-3.5" />
                                )}
                            </Button>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}

