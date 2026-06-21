import type { ScheduleLessonDto } from "@/services/schedule-service";
import { AlertTriangle, Calendar, Loader2 } from "lucide-react";

interface ConfirmCloseModalProps {
    lesson: ScheduleLessonDto;
    isClosing: boolean;
    closeDate: string;
    onCloseDateChange: (date: string) => void;
    onConfirm: () => void;
    onCancel: () => void;
}

export default function ConfirmCloseModal({ lesson, isClosing, closeDate, onCloseDateChange, onConfirm, onCancel }: ConfirmCloseModalProps) {
    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-in fade-in duration-200"
            style={{ background: "rgba(15,20,40,0.25)", backdropFilter: "blur(8px)" }}
            onClick={onCancel}
        >
            <div
                className="glass-card w-full max-w-sm rounded-[28px] p-6 shadow-2xl backdrop-blur-xl animate-in slide-in-from-bottom-4 fade-in duration-200"
                style={{ boxShadow: "0 32px 80px rgba(15,20,60,0.12), 0 0 0 1px rgba(255,255,255,0.5)" }}
                onClick={(e) => e.stopPropagation()}
            >
                <div className="flex items-start gap-3 mb-5">
                    <div className="w-9 h-9 rounded-[12px] bg-orange-50 flex items-center justify-center ring-1 ring-orange-200 shrink-0 mt-0.5">
                        <AlertTriangle className="w-4 h-4 text-orange-500" />
                    </div>
                    <div>
                        <p className="font-black text-(--navy) text-base leading-none">
                            Закрыть урок?
                        </p>
                        <p className="text-xs text-black/40 font-semibold mt-1.5 leading-relaxed">
                            <span className="text-(--navy)">{lesson.subject.name}</span> перестанет проводиться
                            с выбранной даты. Прошлые оценки и посещаемость сохранятся.
                        </p>
                    </div>
                </div>

                {/* Дата закрытия */}
                <div className="space-y-1.5 mb-5">
                    <label className="text-[11px] font-black uppercase tracking-widest text-black/30">
                        Последний день урока
                    </label>
                    <div className="flex items-center gap-2 bg-white/40 border border-white/60 rounded-2xl px-4 h-11">
                        <Calendar className="w-3.5 h-3.5 text-(--red) shrink-0" />
                        <input
                            type="date"
                            value={closeDate}
                            onChange={(e) => onCloseDateChange(e.target.value)}
                            className="bg-transparent text-(--navy) font-bold text-sm focus:outline-none cursor-pointer w-full"
                        />
                    </div>
                    <p className="text-[10px] text-black/30 font-semibold px-1">
                        Урок включительно в этот день — последний
                    </p>
                </div>

                <div className="flex gap-2">
                    <button
                        onClick={onCancel}
                        className="flex-1 h-10 rounded-2xl bg-black/5 hover:bg-black/10 text-black/50 font-bold text-sm transition-all"
                    >
                        Отмена
                    </button>
                    <button
                        onClick={onConfirm}
                        disabled={isClosing || !closeDate}
                        className="flex-1 h-10 rounded-2xl bg-(--red) hover:bg-(--red-dark) text-white font-bold text-sm shadow-lg shadow-(--red)/20 transition-all disabled:opacity-50 flex items-center justify-center gap-2"
                    >
                        {isClosing ? (
                            <Loader2 className="w-3.5 h-3.5 animate-spin" />
                        ) : (
                            "Закрыть"
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
}