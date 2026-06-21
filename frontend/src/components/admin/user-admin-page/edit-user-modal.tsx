import { useEffect } from "react";
import { Loader2, X, CheckCircle2, Save } from "lucide-react";
import { cn } from "@/lib/utils";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { ROLES } from "@/constants/component-constants";
import type { UserResponse, UserRole } from "@/services/user-service";
import {
    useUpdateUser,
    useStudentDetails,
    useTeacherDetails,
} from "@/hooks/use-user";
import { useForm, Controller, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Field, FieldLabel, FieldError } from "@/components/ui/field";
import { useState } from "react";

interface Props {
    user: UserResponse;
    onClose: () => void;
}

const formSchema = z.object({
    roles: z.array(z.enum(["STUDENT", "PARENT", "TEACHER"])).min(1, "Выберите хотя бы одну роль"),
    username: z.string().min(3, "Имя пользователя не может быть меньше 3 символов").max(50, "Имя пользователя не может быть больше 50 символов").trim(),
    firstName: z.string().min(1, "Введите имя").max(255, "Имя не может быть больше 255 символов").trim(),
    lastName: z.string().min(1, "Введите фамилию").max(255, "Фамилия не может быть больше 255 символов").trim(),
    password: z.string().trim().refine(
        (val) => val === "" || val.length >= 5,
        { message: "Пароль не может быть меньше 5 символов" }
    ).refine(
        (val) => val.length <= 50,
        { message: "Пароль не может быть больше 50 символов" }
    ),
    studyProfile: z.string().optional(),
    email: z.string().optional(),
    phoneNumber: z.string().optional(),
}).superRefine((data, ctx) => {
    if (data.roles.includes("STUDENT") && (!data.studyProfile || !data.studyProfile.trim())) {
        ctx.addIssue({ path: ["studyProfile"], code: z.ZodIssueCode.custom, message: "Обязательное поле" });
    }
    if (data.roles.includes("TEACHER")) {
        if (!data.email || !data.email.trim()) {
            ctx.addIssue({ path: ["email"], code: z.ZodIssueCode.custom, message: "Обязательное поле" });
        }
        if (!data.phoneNumber || !data.phoneNumber.trim()) {
            ctx.addIssue({ path: ["phoneNumber"], code: z.ZodIssueCode.custom, message: "Обязательное поле" });
        }
    }
});

type FormValues = z.infer<typeof formSchema>;

export default function EditUserModal({ user, onClose }: Props) {
    const [success, setSuccess] = useState(false);

    const { control, handleSubmit, setValue, formState: { isValid } } = useForm<FormValues>({
        resolver: zodResolver(formSchema),
        mode: "onChange",
        defaultValues: {
            roles: user.roles,
            username: user.username,
            firstName: user.firstName,
            lastName: user.lastName,
            password: "",
            studyProfile: "",
            email: "",
            phoneNumber: "",
        },
    });

    const selectedRoles = useWatch({ control, name: "roles" });

    const { data: studentData, isLoading: isStudentLoading } = useStudentDetails(
        user.roles.includes("STUDENT") ? user.id : null
    );
    const { data: teacherData, isLoading: isTeacherLoading } = useTeacherDetails(
        user.roles.includes("TEACHER") ? user.id : null
    );

    const isDetailsLoading =
        (user.roles.includes("STUDENT") && isStudentLoading) ||
        (user.roles.includes("TEACHER") && isTeacherLoading);

    useEffect(() => {
        if (studentData) {
            setValue("studyProfile", studentData.studyProfile ?? "", { shouldValidate: true });
        }
    }, [studentData, setValue]);

    useEffect(() => {
        if (teacherData) {
            setValue("email", teacherData.email ?? "", { shouldValidate: true });
            setValue("phoneNumber", teacherData.phoneNumber ?? "", { shouldValidate: true });
        }
    }, [teacherData, setValue]);

    const { mutate: update, isPending, isError } = useUpdateUser(user.id);

    const toggleRole = (role: UserRole) => {
        const next = selectedRoles.includes(role)
            ? selectedRoles.length > 1
                ? selectedRoles.filter((r) => r !== role)
                : selectedRoles
            : [...selectedRoles, role];
        setValue("roles", next, { shouldValidate: true });
    };

    const onSubmit = (values: FormValues) => {
        const requestDetails: Record<string, unknown> = {};
        values.roles.forEach((role) => {
            if (role === "STUDENT") requestDetails["STUDENT"] = { studyProfile: values.studyProfile };
            if (role === "TEACHER") requestDetails["TEACHER"] = { email: values.email, phoneNumber: values.phoneNumber };
            if (role === "PARENT") requestDetails["PARENT"] = {};
        });

        update(
            {
                userId: user.id,
                user: {
                    username: values.username.trim(),
                    firstName: values.firstName.trim(),
                    lastName: values.lastName.trim(),
                },
                ...(values.password.trim() ? { password: values.password.trim() } : {}),
                roles: values.roles,
                details: requestDetails,
            },
            {
                onSuccess: () => {
                    setSuccess(true);
                    setTimeout(() => {
                        setSuccess(false);
                        onClose();
                    }, 1500);
                },
            }
        );
    };

    const fieldClass =
        "h-11 bg-white/40 border border-black/10 rounded-2xl focus-visible:ring-(--red) text-sm font-semibold placeholder:font-normal transition-all duration-200";

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm p-4">
            <div className="glass-card w-full max-w-md rounded-[32px] p-6 shadow-2xl max-h-[90vh] overflow-y-auto">
                <div className="flex items-center justify-between mb-6">
                    <div>
                        <h2 className="font-serif font-black text-xl text-(--navy)">Редактировать</h2>
                        <p className="text-xs text-black/40 font-semibold mt-0.5">@{user.username}</p>
                    </div>
                    <button
                        onClick={onClose}
                        className="w-9 h-9 rounded-xl flex items-center justify-center text-black/30 hover:text-black/60 hover:bg-black/5 transition-all"
                    >
                        <X className="w-4 h-4" />
                    </button>
                </div>

                {isDetailsLoading ? (
                    <div className="flex items-center justify-center h-40 text-black/30">
                        <Loader2 className="w-6 h-6 animate-spin" />
                    </div>
                ) : (
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                        <div className="grid grid-cols-2 gap-3">
                            <Controller
                                name="firstName"
                                control={control}
                                render={({ field, fieldState }) => (
                                    <Field data-invalid={fieldState.invalid} className="space-y-1.5">
                                        <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">Имя</FieldLabel>
                                        <Input {...field} disabled={isPending} aria-invalid={fieldState.invalid} className={fieldClass} />
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
                                        <Input {...field} disabled={isPending} aria-invalid={fieldState.invalid} className={fieldClass} />
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
                                    <Input {...field} disabled={isPending} aria-invalid={fieldState.invalid} className={fieldClass} />
                                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                                </Field>
                            )}
                        />

                        <Controller
                            name="password"
                            control={control}
                            render={({ field, fieldState }) => (
                                <Field data-invalid={fieldState.invalid} className="space-y-1.5">
                                    <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">
                                        Новый пароль{" "}
                                        <span className="normal-case font-normal">(оставьте пустым чтобы не менять)</span>
                                    </FieldLabel>
                                    <Input {...field} type="password" placeholder="••••••••" disabled={isPending} aria-invalid={fieldState.invalid} className={fieldClass} />
                                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                                </Field>
                            )}
                        />

                        <div className="space-y-1.5">
                            <label className="text-xs font-bold tracking-widest uppercase text-black/30">Роли</label>
                            <div className="flex gap-1 bg-black/5 rounded-[18px] p-1">
                                {ROLES.map((r) => {
                                    const isActive = selectedRoles.includes(r.value);
                                    return (
                                        <button
                                            key={r.value}
                                            type="button"
                                            onClick={() => toggleRole(r.value)}
                                            className={cn(
                                                "flex-1 h-9 flex items-center justify-center gap-1.5 rounded-2xl text-[11px] font-extrabold uppercase tracking-wider transition-all",
                                                isActive
                                                    ? cn("bg-white/60 shadow-sm", r.color)
                                                    : "text-black/30 hover:text-black/50"
                                            )}
                                        >
                                            {r.icon}
                                            <span className="hidden sm:inline">{r.label}</span>
                                        </button>
                                    );
                                })}
                            </div>
                        </div>

                        {selectedRoles.includes("STUDENT") && (
                            <Controller
                                name="studyProfile"
                                control={control}
                                render={({ field, fieldState }) => (
                                    <Field data-invalid={fieldState.invalid} className="space-y-1.5">
                                        <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">Профиль обучения</FieldLabel>
                                        <Input {...field} disabled={isPending} aria-invalid={fieldState.invalid} className={fieldClass} />
                                        {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                                    </Field>
                                )}
                            />
                        )}

                        {selectedRoles.includes("TEACHER") && (
                            <>
                                <Controller
                                    name="email"
                                    control={control}
                                    render={({ field, fieldState }) => (
                                        <Field data-invalid={fieldState.invalid} className="space-y-1.5">
                                            <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">Email учителя</FieldLabel>
                                            <Input {...field} type="email" disabled={isPending} aria-invalid={fieldState.invalid} className={fieldClass} />
                                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                                        </Field>
                                    )}
                                />
                                <Controller
                                    name="phoneNumber"
                                    control={control}
                                    render={({ field, fieldState }) => (
                                        <Field data-invalid={fieldState.invalid} className="space-y-1.5">
                                            <FieldLabel className="text-xs font-bold tracking-widest uppercase text-black/30">Телефон учителя</FieldLabel>
                                            <Input {...field} disabled={isPending} aria-invalid={fieldState.invalid} className={fieldClass} />
                                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                                        </Field>
                                    )}
                                />
                            </>
                        )}

                        <Button
                            type="submit"
                            disabled={!isValid || isPending}
                            className="w-full gap-2 text-white bg-(--navy) hover:bg-(--navy)/90 rounded-2xl py-6 text-sm font-bold shadow-lg transition-all active:scale-[0.98] disabled:opacity-40 mt-2"
                        >
                            {isPending ? (
                                <><Loader2 className="w-4 h-4 animate-spin" />Сохранение...</>
                            ) : success ? (
                                <><CheckCircle2 className="w-4 h-4" />Сохранено!</>
                            ) : (
                                <><Save className="w-4 h-4" />Сохранить изменения</>
                            )}
                        </Button>

                        {isError && (
                            <p className="text-xs text-(--red) font-semibold text-center">
                                Ошибка при сохранении. Попробуйте ещё раз.
                            </p>
                        )}
                    </form>
                )}
            </div>
        </div>
    );
}