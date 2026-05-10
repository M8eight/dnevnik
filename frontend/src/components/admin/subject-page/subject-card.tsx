import { ACCENT_PALETTE } from "@/constants/component-constants";
import { cn } from "@/lib/utils";
import type { SubjectResponse } from "@/services/subject-service";
import { BookOpen, Hash, Loader2, Trash2 } from "lucide-react";
import { useState } from "react";


export default function SubjectCard({
    subject,
    index,
    onDelete,
    isDeleting,
}: {
    subject: SubjectResponse;
    index: number;
    onDelete: (id: number) => void;
    isDeleting: boolean;
}) {
    const accent = ACCENT_PALETTE[index % ACCENT_PALETTE.length];
    const [confirmDelete, setConfirmDelete] = useState(false);

    const handleDeleteClick = () => {
        if (confirmDelete) {
            onDelete(subject.id);
        } else {
            setConfirmDelete(true);
            setTimeout(() => setConfirmDelete(false), 3000);
        }
    };

    return (
        <div className="group glass-card rounded-[24px] p-5 backdrop-blur-md border border-white/40 hover:border-white/60 transition-all hover:shadow-xl hover:-translate-y-0.5">
            <div className="flex items-start gap-4">
                <div
                    className={cn(
                        "w-11 h-11 rounded-[14px] flex items-center justify-center shrink-0 ring-1 ring-black/5",
                        accent.light
                    )}
                >
                    <BookOpen className={cn("w-5 h-5", accent.text)} />
                </div>
                <div className="flex-1 min-w-0">
                    <p className="font-black text-[var(--navy)] text-[15px] leading-snug truncate">
                        {subject.name}
                    </p>
                    <div className="flex items-center gap-1.5 mt-1">
                        <Hash className="w-3 h-3 text-black/20" />
                        <span className="text-[11px] font-bold text-black/30 tracking-wider">
                            ID {subject.id}
                        </span>
                    </div>
                </div>

                {/* Delete button */}
                <button
                    onClick={handleDeleteClick}
                    disabled={isDeleting}
                    className={cn(
                        "shrink-0 w-8 h-8 rounded-[10px] flex items-center justify-center transition-all opacity-0 group-hover:opacity-100",
                        confirmDelete
                            ? "bg-[var(--red)] text-white scale-105 opacity-100"
                            : "bg-black/5 hover:bg-red-50 text-black/25 hover:text-[var(--red)]"
                    )}
                    title={confirmDelete ? "Нажмите ещё раз для подтверждения" : "Удалить предмет"}
                >
                    {isDeleting ? (
                        <Loader2 className="w-3.5 h-3.5 animate-spin" />
                    ) : (
                        <Trash2 className="w-3.5 h-3.5" />
                    )}
                </button>
            </div>

            {confirmDelete && (
                <p className="text-[11px] text-[var(--red)] font-bold mt-2 pl-[60px]">
                    Нажмите ещё раз для удаления
                </p>
            )}
        </div>
    );
}