import { useState } from "react";
import { Trash2, Loader2, Pencil, Check, X, GraduationCap } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import type { SchoolClassResponse } from "@/services/school-class-service";

interface ClassCardProps {
    schoolClass: SchoolClassResponse;
    index: number;
    onDelete: (id: number) => void;
    onUpdate: (id: number, data: { name?: string; year?: string }) => void;
    onSelect: (id: number) => void;
    isDeleting?: boolean;
    isUpdating?: boolean;
    isSelected?: boolean;
}

export default function ClassCard({
    schoolClass,
    index,
    onDelete,
    onUpdate,
    onSelect,
    isDeleting,
    isUpdating,
    isSelected,
}: ClassCardProps) {
    const [editing, setEditing] = useState(false);
    const [name, setName] = useState(schoolClass.name);
    const [year, setYear] = useState(schoolClass.schoolYear);

    const handleSave = (e: React.MouseEvent) => {
        e.stopPropagation();
        if (!name.trim() || !year.trim()) return;
        onUpdate(schoolClass.id, { name: name.trim(), year: year.trim() });
        setEditing(false);
    };

    const handleCancel = (e: React.MouseEvent) => {
        e.stopPropagation();
        setName(schoolClass.name);
        setYear(schoolClass.schoolYear);
        setEditing(false);
    };

    const handleEditClick = (e: React.MouseEvent) => {
        e.stopPropagation();
        setEditing(true);
    };

    const handleDeleteClick = (e: React.MouseEvent) => {
        e.stopPropagation();
        onDelete(schoolClass.id);
    };

    return (
        <div
            onClick={() => !editing && onSelect(schoolClass.id)}
            className={`group flex items-center justify-between gap-4 rounded-[18px] border px-5 py-4 shadow-sm
                transition-all duration-200 cursor-pointer
                ${isSelected
                    ? "bg-[var(--red-light)]/40 border-[var(--red)]/20 shadow-md ring-1 ring-[var(--red)]/15"
                    : "bg-white/50 border-black/5 hover:shadow-md hover:bg-white/70"
                }`}
            style={{ animationDelay: `${index * 40}ms` }}
        >
            <div className="flex items-center gap-4 flex-1 min-w-0">
                <div className={`flex-shrink-0 w-9 h-9 rounded-[12px] flex items-center justify-center ring-1 transition-colors
                    ${isSelected
                        ? "bg-[var(--red)]/15 ring-[var(--red)]/20"
                        : "bg-[var(--red-light)]/60 ring-[var(--red)]/10"
                    }`}>
                    <span className="text-xs font-black text-[var(--red)]">{index + 1}</span>
                </div>

                {editing ? (
                    <div className="flex items-center gap-2 flex-1 min-w-0" onClick={(e) => e.stopPropagation()}>
                        <Input
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            placeholder="Название"
                            className="h-9 text-sm font-semibold bg-white/60 border-black/10 rounded-xl focus-visible:ring-[var(--red)] w-28"
                            autoFocus
                        />
                        <Input
                            value={year}
                            onChange={(e) => setYear(e.target.value)}
                            placeholder="2024-2025"
                            className="h-9 text-sm bg-white/60 border-black/10 rounded-xl focus-visible:ring-[var(--red)] w-28"
                        />
                    </div>
                ) : (
                    <div className="min-w-0">
                        <p className="font-bold text-sm text-[var(--navy)] truncate leading-tight">
                            {schoolClass.name}
                        </p>
                        <p className="text-xs text-black/40 mt-0.5 flex items-center gap-1">
                            <GraduationCap className="w-3 h-3" />
                            {schoolClass.schoolYear}
                        </p>
                    </div>
                )}
            </div>

            <div className="flex items-center gap-1.5 flex-shrink-0">
                {editing ? (
                    <>
                        <Button
                            size="icon"
                            variant="ghost"
                            onClick={handleSave}
                            disabled={isUpdating || !name.trim() || !year.trim()}
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
                        <Button
                            size="icon"
                            variant="ghost"
                            onClick={handleEditClick}
                            className="w-8 h-8 rounded-xl text-black/30 hover:text-[var(--navy)] hover:bg-black/5 opacity-0 group-hover:opacity-100 transition-opacity"
                        >
                            <Pencil className="w-3.5 h-3.5" />
                        </Button>
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
                    </>
                )}
            </div>
        </div>
    );
}