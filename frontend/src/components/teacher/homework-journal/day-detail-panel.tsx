import { toDateKey } from "@/helpers/page-helpers";
import { useDeleteHomework } from "@/hooks/use-homework";
import type { HomeworkResponse } from "@/services/homework-service";
import type { lessonInstance } from "@/services/lesson-instance-service";
import { format } from "date-fns";
import { ru } from "date-fns/locale";
import { FileText, CalendarDays, Sparkles, Trash2, Plus } from "lucide-react";
import { useMemo } from "react";
import CreateHomeworkForm from "./create-homework-form";
import { ScrollArea } from "@/components/ui/scroll-area";

export default function DayDetailPanel({
    selectedDate,
    homeworks,
    lessonInstances,
    onCreateHomework,
    isSubmitting,
}: {
    selectedDate: Date | null;
    homeworks: HomeworkResponse[];
    lessonInstances: lessonInstance[];
    onCreateHomework: (text: string, id: number) => void;
    isSubmitting: boolean;
}) {
    const deleteMutation = useDeleteHomework();

    const dayHws = useMemo(() => {
        if (!selectedDate) return [];
        const key = toDateKey(selectedDate);
        return homeworks.filter(
            (hw) => hw.lessonInstance?.lessonDate?.slice(0, 10) === key
        );
    }, [selectedDate, homeworks]);

    return (
        <div className="flex flex-col gap-5">
            <div className="glass-card rounded-[32px] p-6 backdrop-blur-md">
                <h2 className="text-base font-black text-[var(--navy)] flex items-center gap-2 mb-4">
                    <FileText className="w-4 h-4 text-[var(--red)]" />
                    {selectedDate
                        ? format(selectedDate, "d MMMM, EEEE", { locale: ru })
                        : "Выберите день"}
                </h2>

                {!selectedDate && (
                    <div className="text-center py-8 text-black/30">
                        <CalendarDays className="w-10 h-10 mx-auto mb-2 opacity-30" />
                        <p className="text-sm">Нажмите на дату в календаре</p>
                    </div>
                )}

                {selectedDate && dayHws.length === 0 && (
                    <div className="text-center py-8 text-black/30">
                        <Sparkles className="w-8 h-8 mx-auto mb-2 opacity-30" />
                        <p className="text-sm font-medium">Нет заданий</p>
                        <p className="text-xs mt-1">Добавьте задание ниже</p>
                    </div>
                )}

                {dayHws.length > 0 && (
                    <ScrollArea className="max-h-[280px] pr-2">
                        <div className="space-y-3">
                            {dayHws.map((hw) => (
                                <div
                                    key={hw.id}
                                    className="rounded-2xl bg-white/40 border border-black/5 p-4"
                                >
                                    <div className="flex items-center justify-between mb-2">
                                        <div className="flex items-center gap-1.5">
                                            <div className="w-1.5 h-1.5 rounded-full bg-[var(--red)]" />
                                            <span className="text-xs font-bold text-black/40 uppercase tracking-wider">
                                                Задание #{hw.id}
                                            </span>
                                        </div>
                                        {/* ← кнопка удаления */}
                                        <button
                                            onClick={() => deleteMutation.mutate(hw.id)}
                                            disabled={deleteMutation.isPending}
                                            className="w-7 h-7 rounded-xl flex items-center justify-center text-black/20 hover:text-[var(--red)] hover:bg-[var(--red-light)]/60 transition-all active:scale-90 disabled:opacity-40"
                                        >
                                            <Trash2 className="w-3.5 h-3.5" />
                                        </button>
                                    </div>
                                    <p className="text-sm text-[var(--navy)] leading-relaxed whitespace-pre-wrap">
                                        {hw.text}
                                    </p>
                                </div>
                            ))}
                        </div>
                    </ScrollArea>
                )}
            </div>

            <div className="glass-card rounded-[32px] p-6 backdrop-blur-md">
                <h2 className="text-base font-black text-[var(--navy)] flex items-center gap-2 mb-5">
                    <Plus className="w-4 h-4 text-[var(--red)]" />
                    Создать задание
                </h2>
                <CreateHomeworkForm
                    onSubmit={onCreateHomework}
                    isSubmitting={isSubmitting}
                    lessonInstances={lessonInstances}
                    preselectedDate={selectedDate}
                />
            </div>
        </div>
    );
}
