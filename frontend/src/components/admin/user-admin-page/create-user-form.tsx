import { ROLES } from "@/constants/component-constants";
import { useCreateStudent, useCreateParent, useCreateTeacher } from "@/hooks/use-user";
import { cn } from "@/lib/utils";
import type { UserRole } from "@/services/user-service";
import { Loader2, CheckCircle2, Send } from "lucide-react";
import { Input } from "@/components/ui/input";
import RoleTab from "./role-tab";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { useForm, Controller, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Field, FieldLabel, FieldError } from "@/components/ui/field";

const formSchema = z.object({
    role: z.enum(["STUDENT", "PARENT", "TEACHER"]),
    username: z.string().min(3, "Имя пользователя не может быть меньше 3 символов").max(50, "Имя пользователя не может быть больше 50 символов").trim(),
    password: z.string().min(5, "Пароль не может быть меньше 5 символов").max(50, "Пароль не может быть больше 50 символов").trim(),
    firstName: z.string().min(1, "Введите имя").max(255, "Имя не может быть больше 255 символов").trim(),
    lastName: z.string().min(1, "Введите фамилию").max(255, "Фамилия не может быть больше 255 символов").trim(),
    studentDetails: z.string().optional(),
    email: z.string().optional(),
    phoneNumber: z.string().optional(),
}).superRefine((data, ctx) => {
    if (data.role === "STUDENT" && (!data.studentDetails || !data.studentDetails.trim())) {
        ctx.addIssue({ path: ["studentDetails"], code: z.ZodIssueCode.custom, message: "Обязательное поле" });
    }
    if (data.role === "TEACHER") {
        if (!data.email || !data.email.trim()) {
            ctx.addIssue({ path: ["email"], code: z.ZodIssueCode.custom, message: "Обязательное поле" });
        }
        if (!data.phoneNumber || !data.phoneNumber.trim()) {
            ctx.addIssue({ path: ["phoneNumber"], code: z.ZodIssueCode.custom, message: "Обязательное поле" });
        }
    }
});

const roleFormat = (role: string) => {
    switch (role) {
        case "ученик": return "ученика";
        case "родитель": return "родителя";
        case "учитель": return "учителя";
    }
}

type FormValues = z.infer<typeof formSchema>;

export default function CreateUserForm() {
    const [success, setSuccess] = useState(false);

    const studentMutation = useCreateStudent();
    const parentMutation = useCreateParent();
    const teacherMutation = useCreateTeacher();

    const { control, handleSubmit, reset, setValue, clearErrors, formState: { isValid } } = useForm<FormValues>({
        resolver: zodResolver(formSchema),
        mode: "onChange",
        defaultValues: {
            role: "STUDENT",
            username: "",
            password: "",
            firstName: "",
            lastName: "",
            studentDetails: "",
            email: "",
            phoneNumber: "",
        },
    });

    const currentRole = useWatch({ control, name: "role" });

    const isPending =
        studentMutation.isPending ||
        parentMutation.isPending ||
        teacherMutation.isPending;

    const isError =
        studentMutation.isError ||
        parentMutation.isError ||
        teacherMutation.isError;

    const handleRoleChange = (newRole: UserRole) => {
        setValue("role", newRole, { shouldValidate: true });
        studentMutation.reset();
        parentMutation.reset();
        teacherMutation.reset();
        clearErrors();
    };

    const handleSuccess = () => {
        reset();
        setSuccess(true);
        setTimeout(() => setSuccess(false), 2500);
    };

    const onSubmit = (values: FormValues) => {
        const userBase = {
            username: values.username,
            password: values.password,
            firstName: values.firstName,
            lastName: values.lastName,
        };

        if (values.role === "STUDENT") {
            studentMutation.mutate(
                { user: userBase, role: "STUDENT", details: { studyProfile: values.studentDetails! } },
                { onSuccess: handleSuccess }
            );
        } else if (values.role === "PARENT") {
            parentMutation.mutate(
                { user: userBase, role: "PARENT", details: {} },
                { onSuccess: handleSuccess }
            );
        } else if (values.role === "TEACHER") {
            teacherMutation.mutate(
                { user: userBase, role: "TEACHER", details: { email: values.email!, phoneNumber: values.phoneNumber! } },
                { onSuccess: handleSuccess }
            );
        }
    };

    const fieldClass =
        "h-11 bg-white/40 border border-black/10 rounded-2xl focus-visible:ring-(--red) text-sm font-semibold placeholder:font-normal transition-all duration-200";

    const activeRole = ROLES.find((r) => r.value === currentRole)!;

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <div className="space-y-1.5">
                <label className="text-xs font-bold tracking-widest uppercase text-black/30">Роль</label>
                <div className="flex gap-1 bg-black/5 rounded-[18px] p-1">
                    {ROLES.map((r) => (
                        <RoleTab
                            key={r.value}
                            role={r}
                            active={currentRole === r.value}
                            onClick={() => handleRoleChange(r.value)}
                        />
                    ))}
                </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
                <Controller
                    name="firstName"
                    control={control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid} className="space-y-1.5">
                            <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">Имя</FieldLabel>
                            <Input {...field} placeholder="Иван" disabled={isPending} aria-invalid={fieldState.invalid} className={fieldClass} />
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />
                <Controller
                    name="lastName"
                    control={control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid} className="space-y-1.5">
                            <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">Фамилия</FieldLabel>
                            <Input {...field} placeholder="Иванов" disabled={isPending} aria-invalid={fieldState.invalid} className={fieldClass} />
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />
            </div>

            <Controller
                name="username"
                control={control}
                render={({ field, fieldState }) => (
                    <Field data-invalid={fieldState.invalid} className="space-y-1.5">
                        <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">Логин</FieldLabel>
                        <Input {...field} placeholder="ivanov_ivan" disabled={isPending} aria-invalid={fieldState.invalid} className={fieldClass} />
                        {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                )}
            />

            <Controller
                name="password"
                control={control}
                render={({ field, fieldState }) => (
                    <Field data-invalid={fieldState.invalid} className="space-y-1.5">
                        <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">Пароль</FieldLabel>
                        <Input {...field} type="password" placeholder="••••••••" disabled={isPending} aria-invalid={fieldState.invalid} className={fieldClass} />
                        {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                )}
            />

            {currentRole === "STUDENT" && (
                <Controller
                    name="studentDetails"
                    control={control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid} className="space-y-1.5">
                            <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">Детали ученика</FieldLabel>
                            <Input {...field} placeholder="Профиль ученика" disabled={isPending} aria-invalid={fieldState.invalid} className={fieldClass} />
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />
            )}

            {currentRole === "TEACHER" && (
                <>
                    <Controller
                        name="email"
                        control={control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid} className="space-y-1.5">
                                <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">Email</FieldLabel>
                                <Input {...field} type="email" placeholder="teacher@school.ru" disabled={isPending} aria-invalid={fieldState.invalid} className={fieldClass} />
                                {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />
                    <Controller
                        name="phoneNumber"
                        control={control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid} className="space-y-1.5">
                                <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">Телефон</FieldLabel>
                                <Input {...field} placeholder="+79001234567" disabled={isPending} aria-invalid={fieldState.invalid} className={fieldClass} />
                                {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />
                </>
            )}

            <Button
                type="submit"
                disabled={!isValid || isPending}
                className={cn(
                    "w-full gap-2 text-white rounded-2xl py-6 text-sm font-bold shadow-lg transition-all active:scale-[0.98] disabled:opacity-40",
                    currentRole === "STUDENT"
                        ? "bg-blue-600 hover:bg-blue-700 shadow-blue-200"
                        : currentRole === "PARENT"
                            ? "bg-violet-600 hover:bg-violet-700 shadow-violet-200"
                            : "bg-emerald-600 hover:bg-emerald-700 shadow-emerald-200"
                )}
            >
                {isPending ? (
                    <><Loader2 className="w-4 h-4 animate-spin" />Создание...</>
                ) : success ? (
                    <><CheckCircle2 className="w-4 h-4" />Создан!</>
                ) : (
                    <>Создать {roleFormat(activeRole.label.toLowerCase())}
                        <Send className="w-4 h-4" /></>
                )}
            </Button>

            {isError && (
                <p className="text-xs text-(--red) font-semibold text-center">
                    Ошибка при создании. Попробуйте ещё раз.
                </p>
            )}
        </form>
    );
}