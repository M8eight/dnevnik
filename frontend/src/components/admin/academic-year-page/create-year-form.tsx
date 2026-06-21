import { useCreateAcademicYear } from "@/hooks/use-academic-year";
import { Loader2, CheckCircle2, Send, CalendarIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useState } from "react";
import { Input } from "@/components/ui/input";
import { useForm, Controller, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Field, FieldLabel, FieldError } from "@/components/ui/field";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { cn } from "@/lib/utils";
import { format } from "date-fns";

const formSchema = z.object({
    name: z
        .string()
        .min(3, "Название года не может быть меньше 3 символов")
        .max(50, "Слишком длинное название")
        .trim(),
    startDate: z.date({ message: "Укажите дату начала" }),
    endDate: z.date({ message: "Укажите дату окончания" }),
});

type FormValues = z.infer<typeof formSchema>;

export default function CreateYearForm() {
    const [success, setSuccess] = useState(false);
    const createMutation = useCreateAcademicYear();

    const { control, handleSubmit, reset } = useForm<FormValues>({
        resolver: zodResolver(formSchema),
        defaultValues: { 
            name: "", 
            startDate: undefined, 
            endDate: undefined 
        },
    });

    const formValues = useWatch({ control });
    const isValid = !!(formValues.name && formValues.startDate && formValues.endDate);

    const formatDateToString = (date: Date) => {
        return date.toLocaleDateString("en-CA"); // Возвращает строго "YYYY-MM-DD"
    };

    const onSubmit = (values: FormValues) => {
        createMutation.mutate(
            {
                name: values.name,
                startDate: formatDateToString(values.startDate),
                endDate: formatDateToString(values.endDate),
            },
            {
                onSuccess: () => {
                    reset();
                    setSuccess(true);
                    setTimeout(() => setSuccess(false), 2500);
                },
            }
        );
    };

    const fieldClass =
        "h-11 bg-white/40 border border-black/10 rounded-2xl focus-visible:ring-(--red) text-sm font-semibold placeholder:font-normal transition-all duration-200";

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            {/* Название года */}
            <Controller
                name="name"
                control={control}
                render={({ field, fieldState }) => (
                    <Field data-invalid={fieldState.invalid} className="space-y-1.5">
                        <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">
                            Название года
                        </FieldLabel>
                        <Input
                            {...field}
                            placeholder="Например: 2024-2025"
                            disabled={createMutation.isPending}
                            aria-invalid={fieldState.invalid}
                            className={fieldClass}
                        />
                        {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                )}
            />

            {/* Блок выбора дат */}
            <div className="grid grid-cols-2 gap-3">
                {/* Дата начала */}
                <Controller
                    name="startDate"
                    control={control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid} className="space-y-1.5 flex flex-col">
                            <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">
                                Начало
                            </FieldLabel>
                            <Popover>
                                <PopoverTrigger asChild>
                                    <Button
                                        variant="outline"
                                        disabled={createMutation.isPending}
                                        className={cn(
                                            fieldClass,
                                            "w-full justify-start text-left font-semibold px-3.5 shadow-none hover:bg-white/60",
                                            !field.value && "text-black/30 font-normal"
                                        )}
                                    >
                                        <CalendarIcon className="mr-2 h-4 w-4 text-black/40" />
                                        {field.value ? (
                                            format(field.value, "dd.MM.yyyy")
                                        ) : (
                                            <span>дд.мм.гггг</span>
                                        )}
                                    </Button>
                                </PopoverTrigger>
                                <PopoverContent className="w-auto p-0 rounded-2xl border-black/10" align="start">
                                    <Calendar
                                        mode="single"
                                        captionLayout="dropdown"
                                        selected={field.value}
                                        onSelect={field.onChange}
                                    />
                                </PopoverContent>
                            </Popover>
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />

                {/* Дата окончания */}
                <Controller
                    name="endDate"
                    control={control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid} className="space-y-1.5 flex flex-col">
                            <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">
                                Конец
                            </FieldLabel>
                            <Popover>
                                <PopoverTrigger asChild>
                                    <Button
                                        variant="outline"
                                        disabled={createMutation.isPending}
                                        className={cn(
                                            fieldClass,
                                            "w-full justify-start text-left font-semibold px-3.5 shadow-none hover:bg-white/60",
                                            !field.value && "text-black/30 font-normal"
                                        )}
                                    >
                                        <CalendarIcon className="mr-2 h-4 w-4 text-black/40" />
                                        {field.value ? (
                                            format(field.value, "dd.MM.yyyy")
                                        ) : (
                                            <span>ДД.ММ.ГГГГ</span>
                                        )}
                                    </Button>
                                </PopoverTrigger>
                                <PopoverContent className="w-auto p-0 rounded-2xl border-black/10" align="start">
                                    <Calendar
                                        mode="single"
                                        captionLayout="dropdown"
                                        selected={field.value}
                                        onSelect={field.onChange}
                                    />
                                </PopoverContent>
                            </Popover>
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />
            </div>

            {/* Кнопка отправки */}
            <Button
                type="submit"
                disabled={!isValid || createMutation.isPending}
                className="w-full gap-2 bg-(--red) hover:bg-(--red)/90 text-white rounded-2xl py-6 text-sm font-bold shadow-lg shadow-(--red)/20 transition-all active:scale-[0.98] disabled:opacity-40"
            >
                {createMutation.isPending ? (
                    <>
                        <Loader2 className="w-4 h-4 animate-spin" />
                        Создание...
                    </>
                ) : success ? (
                    <>
                        <CheckCircle2 className="w-4 h-4" />
                        Создан!
                    </>
                ) : (
                    <>
                        Добавить учебный год
                        <Send className="w-4 h-4" />
                    </>
                )}
            </Button>

            {createMutation.isError && (
                <p className="text-xs text-(--red) font-semibold text-center mt-2">
                    Ошибка при создании. Попробуйте ещё раз.
                </p>
            )}
        </form>
    );
}