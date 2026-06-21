import { RefreshCw, Calendar, Loader2 } from "lucide-react";
import { useState, useMemo } from "react";

interface GenerateModalProps {
    isGenerating: boolean;
    onConfirm: (from: string, to: string) => void;
    onCancel: () => void;
}

export default function GenerateModal({ isGenerating, onConfirm, onCancel }: GenerateModalProps) {
    const initialDates = useMemo(() => {
        const today = new Date();
        const nextMonth = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000);
        
        return {
            todayStr: today.toISOString().split("T")[0],
            nextMonthStr: nextMonth.toISOString().split("T")[0]
        };
    }, []);

    const [fromDate, setFromDate] = useState(initialDates.todayStr);
    const [toDate, setToDate] = useState(initialDates.nextMonthStr);

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
                {/* Внутреннее содержимое модалки */}
                <div className="flex items-start gap-3 mb-5">
                    <div className="w-9 h-9 rounded-[12px] bg-blue-50 flex items-center justify-center ring-1 ring-blue-200 shrink-0 mt-0.5">
                        <RefreshCw className="w-4 h-4 text-blue-500" />
                    </div>
                    <div>
                        <p className="font-black text-(--navy) text-base leading-none">
                            Загрузить уроки
                        </p>
                        <p className="text-xs text-black/40 font-semibold mt-1.5 leading-relaxed">
                            Сгенерирует lesson instances по активным шаблонам за выбранный период
                        </p>
                    </div>
                </div>

                <div className="space-y-3 mb-5">
                    <div className="space-y-1.5">
                        <label className="text-[11px] font-black uppercase tracking-widest text-black/30">
                            С даты
                        </label>
                        <div className="flex items-center gap-2 bg-white/40 border border-white/60 rounded-2xl px-4 h-11">
                            <Calendar className="w-3.5 h-3.5 text-(--red) shrink-0" />
                            <input
                                type="date"
                                value={fromDate}
                                onChange={(e) => setFromDate(e.target.value)}
                                className="bg-transparent text-(--navy) font-bold text-sm focus:outline-none cursor-pointer w-full"
                            />
                        </div>
                    </div>
                    <div className="space-y-1.5">
                        <label className="text-[11px] font-black uppercase tracking-widest text-black/30">
                            По дату
                        </label>
                        <div className="flex items-center gap-2 bg-white/40 border border-white/60 rounded-2xl px-4 h-11">
                            <Calendar className="w-3.5 h-3.5 text-(--red) shrink-0" />
                            <input
                                type="date"
                                value={toDate}
                                onChange={(e) => setToDate(e.target.value)}
                                className="bg-transparent text-(--navy) font-bold text-sm focus:outline-none cursor-pointer w-full"
                            />
                        </div>
                    </div>
                </div>

                <div className="flex gap-2">
                    <button
                        onClick={onCancel}
                        className="flex-1 h-10 rounded-2xl bg-black/5 hover:bg-black/10 text-black/50 font-bold text-sm transition-all"
                    >
                        Отмена
                    </button>
                    <button
                        onClick={() => onConfirm(fromDate, toDate)}
                        disabled={isGenerating || !fromDate || !toDate}
                        className="flex-1 h-10 rounded-2xl bg-(--navy) hover:bg-(--navy)/80 text-white font-bold text-sm transition-all disabled:opacity-50 flex items-center justify-center gap-2"
                    >
                        {isGenerating ? (
                            <Loader2 className="w-3.5 h-3.5 animate-spin" />
                        ) : (
                            <>
                                <RefreshCw className="w-3.5 h-3.5" />
                                Загрузить
                            </>
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
}