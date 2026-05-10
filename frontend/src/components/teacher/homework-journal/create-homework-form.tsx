import React, { useState, useEffect } from "react";
import { cn } from "@/lib/utils";
import {
    Send,
    Calendar as CalendarIcon,
} from "lucide-react";
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover";
import { format, isSameDay, parseISO } from "date-fns";
import { ru } from "date-fns/locale";
import type { lessonInstance } from "@/services/lesson-instance-service";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { Textarea } from "@/components/ui/textarea";


export default function CreateHomeworkForm({
    onSubmit,
    isSubmitting,
    lessonInstances,
    preselectedDate,
}: {
    onSubmit: (text: string, lessonInstanceId: number) => void;
    isSubmitting: boolean;
    lessonInstances: lessonInstance[];
    preselectedDate?: Date | null;
}) {
    const [text, setText] = useState("");
    const [selectedInstanceId, setSelectedInstanceId] = useState<number | null>(null);

    useEffect(() => {
        if (preselectedDate) {
            const inst = lessonInstances.find((i) =>
                isSameDay(parseISO(i.lessonDate), preselectedDate)
            );
            setSelectedInstanceId(inst?.id ?? null);
        }
    }, [preselectedDate, lessonInstances]);

    const selectedInstance = lessonInstances.find((i) => i.id === selectedInstanceId);
    const date = selectedInstance ? parseISO(selectedInstance.lessonDate) : undefined;

    const disabledDays = (d: Date) =>
        !lessonInstances.some((inst) => isSameDay(parseISO(inst.lessonDate), d));

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (text.trim() && selectedInstanceId) {
            onSubmit(text.trim(), selectedInstanceId);
            setText("");
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
                <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                    Дата урока
                </label>
                <Popover>
                    <PopoverTrigger asChild>
                        <Button
                            variant="outline"
                            className={cn(
                                "w-full justify-start text-left font-semibold bg-white/40 border-black/10 rounded-2xl h-12 text-sm",
                                !date && "text-black/30 font-normal"
                            )}
                        >
                            <CalendarIcon className="mr-2 h-4 w-4 text-[var(--red)]" />
                            {date
                                ? format(date, "d MMMM yyyy", { locale: ru })
                                : "Выберите дату урока"}
                        </Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0 bg-white rounded-2xl border-none shadow-2xl">
                        <Calendar
                            mode="single"
                            selected={date}
                            onSelect={(sel) => {
                                if (sel) {
                                    const inst = lessonInstances.find((i) =>
                                        isSameDay(parseISO(i.lessonDate), sel)
                                    );
                                    setSelectedInstanceId(inst?.id ?? null);
                                } else {
                                    setSelectedInstanceId(null);
                                }
                            }}
                            disabled={disabledDays}
                            initialFocus
                            locale={ru}
                            className="rounded-2xl"
                        />
                    </PopoverContent>
                </Popover>
            </div>

            <div className="space-y-2">
                <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                    Текст задания
                </label>
                <Textarea
                    placeholder="Введите домашнее задание..."
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    className="min-h-[110px] resize-none bg-white/40 border-black/10 rounded-2xl focus-visible:ring-[var(--red)] text-sm"
                    disabled={isSubmitting}
                />
            </div>

            <Button
                type="submit"
                disabled={!text.trim() || !selectedInstanceId || isSubmitting}
                className="w-full gap-2 bg-[var(--red)] hover:bg-[var(--red-dark)] text-white rounded-2xl py-6 text-sm font-bold shadow-lg shadow-[var(--red)]/20 transition-all active:scale-[0.98] disabled:opacity-40"
            >
                {isSubmitting ? "Создание..." : "Создать задание"}
                <Send className="w-4 h-4" />
            </Button>
        </form>
    );
}