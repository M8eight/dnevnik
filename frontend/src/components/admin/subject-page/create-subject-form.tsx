import { useCreateSubject } from "@/hooks/use-subject";
import { Loader2, CheckCircle2, Send } from "lucide-react";
import { useState } from "react";
import { useForm, Controller, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Field, FieldLabel, FieldError } from "@/components/ui/field";

const formSchema = z.object({
    name: z
        .string()
        .min(3, "Название предмета не может быть меньше 3 символов")
        .max(50, "Название предмета не может быть больше 50 символов")
        .trim(),
});

type FormValues = z.infer<typeof formSchema>;

export default function CreateSubjectForm() {
    const [success, setSuccess] = useState(false);
    const createMutation = useCreateSubject();
    const { control, handleSubmit, reset } = useForm<FormValues>({
        resolver: zodResolver(formSchema),
        defaultValues: { name: "" },
    });

    const name = useWatch({
        control,
        name: "name",
    });

    const onSubmit = (values: FormValues) => {
        createMutation.mutate(
            { subjectName: values.name },
            {
                onSuccess: () => {
                    reset();
                    setSuccess(true);
                    setTimeout(() => setSuccess(false), 2500);
                },
            }
        );
    };

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <Controller
                name="name"
                control={control}
                render={({ field, fieldState }) => (
                    <Field data-invalid={fieldState.invalid}>
                        <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">
                            Название предмета
                        </FieldLabel>
                        <Input
                            {...field}
                            placeholder="Например: Математика"
                            disabled={createMutation.isPending}
                            aria-invalid={fieldState.invalid}
                            className="h-12 bg-white/40 border-black/10 rounded-2xl focus-visible:ring-(--red) text-sm font-semibold placeholder:font-normal"
                        />
                        {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                )}
            />

            <Button
                type="submit"
                disabled={!name || createMutation.isPending}
                className="w-full gap-2 bg-(--red) hover:bg-(--red)/70 text-white rounded-2xl py-6 text-sm font-bold shadow-lg shadow-(--red)/20 transition-all active:scale-[0.98] disabled:opacity-40"
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
                        Создать предмет
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