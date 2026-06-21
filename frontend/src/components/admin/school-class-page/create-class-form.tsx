import { useCreateClass } from "@/hooks/use-school-class";
import { Loader2, CheckCircle2, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useState } from "react";
import { Input } from "@/components/ui/input";
import { useForm, Controller, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Field, FieldLabel, FieldError } from "@/components/ui/field";

const formSchema = z.object({
    name: z
        .string()
        .min(1, "Название класса не может быть меньше 1 символа")
        .max(20, "Название класса не может быть больше 20 символов")
        .trim(),
});

type FormValues = z.infer<typeof formSchema>;

export interface CreateClassFormProps {
    academicYearId: number;
}

export default function CreateClassForm({ academicYearId }: CreateClassFormProps) {
    const [success, setSuccess] = useState(false);
    const createMutation = useCreateClass();

    const { control, handleSubmit, reset } = useForm<FormValues>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            name: "",
        },
    });

    const formValues = useWatch({ control });
    const isValid = !!formValues.name;

    const onSubmit = (values: FormValues) => {
        createMutation.mutate(
            {
                name: values.name,
                academicYearId: academicYearId,
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
            <Controller
                name="name"
                control={control}
                render={({ field, fieldState }) => (
                    <Field data-invalid={fieldState.invalid} className="space-y-1.5">
                        <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">
                            Название класса
                        </FieldLabel>
                        <Input
                            {...field}
                            placeholder="Например: 5А"
                            disabled={createMutation.isPending}
                            aria-invalid={fieldState.invalid}
                            className={fieldClass}
                        />
                        {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                )}
            />

            <Button
                type="submit"
                disabled={!isValid || createMutation.isPending}
                className="w-full gap-2 bg-(--red) hover:bg-(--red)/90 text-white rounded-2xl py-6 text-sm font-bold shadow-lg shadow-(--red)/20 
                transition-all active:scale-[0.98] disabled:opacity-40"
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
                        Создать класс
                        <Plus className="w-4 h-4" />
                    </>
                )}
            </Button>

            {createMutation.isError && (
                <p className="text-xs text-(--red) font-semibold text-center mt-2">
                    Ошибка при создании класса. Попробуйте ещё раз.
                </p>
            )}
        </form>
    );
}