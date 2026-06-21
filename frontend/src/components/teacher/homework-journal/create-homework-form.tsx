import { useState, useEffect } from "react";
import { Controller, useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { format, isSameDay, parseISO } from "date-fns";
import { ru } from "date-fns/locale";
import {
    Send,
    Calendar as CalendarIcon,
    Loader2,
    CheckCircle2,
} from "lucide-react";

import { cn } from "@/lib/utils";
import type { lessonInstance } from "@/services/lesson-instance-service";

import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { Textarea } from "@/components/ui/textarea";
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover";

import {
    Field,
    FieldLabel,
    FieldError,
} from "@/components/ui/field";

const formSchema = z.object({
    text: z
        .string()
        .min(1, "Введите текст задания")
        .max(2000, "Слишком длинный текст"),

    lessonInstanceId: z.number().nullable(),
});

type FormValues = z.infer<typeof formSchema>;

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
    const [success, setSuccess] = useState(false);

    const {
        control,
        handleSubmit,
        reset,
        setValue,
    } = useForm<FormValues>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            text: "",
            lessonInstanceId: null,
        },
    });

    useEffect(() => {
        if (preselectedDate) {
            const inst = lessonInstances.find((i) =>
                isSameDay(parseISO(i.lessonDate), preselectedDate)
            );

            setValue("lessonInstanceId", inst?.id ?? null);
        }
    }, [preselectedDate, lessonInstances, setValue]);

    const values = useWatch({ control });

    const selectedInstance = lessonInstances.find(
        (i) => i.id === values.lessonInstanceId
    );

    const selectedDate = selectedInstance
        ? parseISO(selectedInstance.lessonDate)
        : undefined;

    const disabledDays = (date: Date) =>
        !lessonInstances.some((inst) =>
            isSameDay(parseISO(inst.lessonDate), date)
        );

    const formValid =
        !!values?.text?.trim() &&
        values?.lessonInstanceId !== null;

    const submitHandler = (values: FormValues) => {
        if (values.lessonInstanceId === null) return;

        onSubmit(
            values.text.trim(),
            values.lessonInstanceId
        );

        reset({
            text: "",
            lessonInstanceId: values.lessonInstanceId,
        });

        setSuccess(true);
        setTimeout(() => setSuccess(false), 2500);
    };

    const fieldClass =
        "bg-white/40 border border-black/10 rounded-2xl focus-visible:ring-(--red) text-sm transition-all duration-200";

    return (
        <form
            onSubmit={handleSubmit(submitHandler)}
            className="space-y-4"
        >
            <Controller
                name="lessonInstanceId"
                control={control}
                render={({ fieldState }) => (
                    <Field
                        data-invalid={fieldState.invalid}
                        className="space-y-1.5"
                    >
                        <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">
                            Дата урока
                        </FieldLabel>

                        <Popover>
                            <PopoverTrigger asChild>
                                <Button
                                    type="button"
                                    variant="outline"
                                    className={cn(
                                        "w-full justify-start text-left h-11 bg-white/40 border border-black/10 rounded-2xl text-sm font-semibold",
                                        !selectedDate &&
                                        "text-black/30 font-normal"
                                    )}
                                >
                                    <CalendarIcon className="mr-2 h-4 w-4 text-(--red)" />

                                    {selectedDate
                                        ? format(
                                            selectedDate,
                                            "d MMMM yyyy",
                                            { locale: ru }
                                        )
                                        : "Выберите дату урока"}
                                </Button>
                            </PopoverTrigger>

                            <PopoverContent className="w-auto p-0 bg-white rounded-2xl border-none shadow-2xl">
                                <Calendar
                                    mode="single"
                                    selected={selectedDate}
                                    locale={ru}
                                    initialFocus
                                    disabled={disabledDays}
                                    className="rounded-2xl"
                                    onSelect={(date) => {
                                        if (!date) {
                                            setValue(
                                                "lessonInstanceId",
                                                null,
                                                {
                                                    shouldValidate: true,
                                                }
                                            );
                                            return;
                                        }

                                        const inst =
                                            lessonInstances.find((i) =>
                                                isSameDay(
                                                    parseISO(
                                                        i.lessonDate
                                                    ),
                                                    date
                                                )
                                            );

                                        setValue(
                                            "lessonInstanceId",
                                            inst?.id ?? null,
                                            {
                                                shouldValidate: true,
                                            }
                                        );
                                    }}
                                />
                            </PopoverContent>
                        </Popover>

                        {fieldState.error && (
                            <FieldError
                                errors={[fieldState.error]}
                            />
                        )}
                    </Field>
                )}
            />

            <Controller
                name="text"
                control={control}
                render={({ field, fieldState }) => (
                    <Field
                        data-invalid={fieldState.invalid}
                        className="space-y-1.5"
                    >
                        <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">
                            Текст задания
                        </FieldLabel>

                        <Textarea
                            {...field}
                            disabled={isSubmitting}
                            placeholder="Введите домашнее задание..."
                            className={cn(
                                fieldClass,
                                "min-h-[110px] resize-none"
                            )}
                        />

                        {fieldState.error && (
                            <FieldError
                                errors={[fieldState.error]}
                            />
                        )}
                    </Field>
                )}
            />

            <Button
                type="submit"
                disabled={!formValid || isSubmitting}
                className="w-full gap-2 bg-(--red) hover:bg-(--red)/90 text-white rounded-2xl py-6 text-sm font-bold shadow-lg shadow-(--red)/20 transition-all active:scale-[0.98] disabled:opacity-40"
            >
                {isSubmitting ? (
                    <>
                        <Loader2 className="w-4 h-4 animate-spin" />
                        Создание...
                    </>
                ) : success ? (
                    <>
                        <CheckCircle2 className="w-4 h-4" />
                        Создано!
                    </>
                ) : (
                    <>
                        Создать задание
                        <Send className="w-4 h-4" />
                    </>
                )}
            </Button>
        </form>
    );
}