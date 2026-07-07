import { useState } from "react";
import {
    LogIn,
    Mail,
    Lock,
    Eye,
    EyeOff,
    Loader2,
    ShieldCheck,
    AlertTriangle,
} from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";

const MOCK_USER = { email: "admin@school.ru", password: "admin123" };

export default function LoginPage() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [remember, setRemember] = useState(false);

    const [isPending, setIsPending] = useState(false);
    const [isError, setIsError] = useState(false);

    const isFormValid = email.trim().length > 0 && password.trim().length > 0;

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!isFormValid || isPending) return;

        setIsError(false);
        setIsPending(true);

        // мок-запрос вместо реального хука авторизации
        setTimeout(() => {
            const success =
                email.trim().toLowerCase() === MOCK_USER.email &&
                password === MOCK_USER.password;

            setIsPending(false);
            setIsError(!success);

            if (success) {
                // eslint-disable-next-line no-console
                console.log("mock login ok", { email, remember });
            }
        }, 1000);
    };

    return (
        <div className="relative z-10 min-h-screen flex items-center justify-center px-4 py-10">
            {/* ── Ambient background accents ── */}
            <div className="pointer-events-none absolute inset-0 overflow-hidden">
                <div className="absolute -top-24 -left-24 w-80 h-80 rounded-full bg-(--red-light)/40 blur-3xl" />
                <div className="absolute -bottom-24 -right-24 w-96 h-96 rounded-full bg-(--navy)/10 blur-3xl" />
            </div>

            <div className="relative w-full max-w-md">
                {/* ── Brand mark ── */}
                <div className="flex flex-col items-center mb-6">
                    <div className="w-14 h-14 rounded-[18px] bg-(--red-light)/60 flex items-center justify-center ring-1 ring-(--red)/10 mb-4">
                        <ShieldCheck className="w-7 h-7 text-(--red)" />
                    </div>
                    <h1 className="font-serif font-black text-2xl lg:text-3xl text-(--navy) tracking-tight">
                        Вход в систему
                    </h1>
                    <p className="text-sm text-black/40 mt-1 text-center">
                        Введите данные учётной записи, чтобы продолжить
                    </p>
                </div>

                {/* ── Login card ── */}
                <div className="glass-card rounded-[32px] p-6 sm:p-8 backdrop-blur-md shadow-lg">
                    {isError && (
                        <div className="mb-5 animate-in fade-in slide-in-from-top-2 duration-300">
                            <Alert variant="destructive" className="rounded-2xl bg-linear-to-r from-red-50 to-red-50/50 border-red-200/80">
                                <div className="flex items-start gap-3">
                                    <div className="shrink-0 mt-0.5 w-9 h-9 rounded-xl bg-red-100/60 flex items-center justify-center">
                                        <AlertTriangle className="h-4 w-4 text-yellow-600" />
                                    </div>
                                    <div className="flex-1">
                                        <AlertTitle className="font-serif font-black tracking-tight text-sm text-yellow-900 mb-0.5">
                                            Не удалось войти
                                        </AlertTitle>
                                        <AlertDescription className="text-xs text-yellow-800/85 font-medium leading-relaxed">
                                            Проверьте почту и пароль и попробуйте снова
                                        </AlertDescription>
                                    </div>
                                </div>
                            </Alert>
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                        {/* Email */}
                        <div className="flex flex-col gap-1.5">
                            <label className="text-xs font-bold text-(--navy)/70 pl-1">
                                Электронная почта
                            </label>
                            <div className="relative">
                                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-black/30" />
                                <Input
                                    type="email"
                                    placeholder="you@school.ru"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    autoComplete="email"
                                    className="pl-10 h-11 bg-white/40 border-black/10 rounded-2xl text-sm font-semibold placeholder:font-normal focus-visible:ring-(--red)"
                                />
                            </div>
                        </div>

                        {/* Password */}
                        <div className="flex flex-col gap-1.5">
                            <div className="flex items-center justify-between pl-1 pr-1">
                                <label className="text-xs font-bold text-(--navy)/70">
                                    Пароль
                                </label>
                                <a
                                    href="/forgot-password"
                                    className="text-xs font-bold text-(--red) hover:text-(--red)/80 transition-colors"
                                >
                                    Забыли пароль?
                                </a>
                            </div>
                            <div className="relative">
                                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-black/30" />
                                <Input
                                    type={showPassword ? "text" : "password"}
                                    placeholder="••••••••"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    autoComplete="current-password"
                                    className="pl-10 pr-11 h-11 bg-white/40 border-black/10 rounded-2xl text-sm font-semibold placeholder:font-normal focus-visible:ring-(--red)"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword((v) => !v)}
                                    className="absolute right-4 top-1/2 -translate-y-1/2 text-black/30 hover:text-(--navy) transition-colors"
                                    tabIndex={-1}
                                    aria-label={showPassword ? "Скрыть пароль" : "Показать пароль"}
                                >
                                    {showPassword ? (
                                        <EyeOff className="w-4 h-4" />
                                    ) : (
                                        <Eye className="w-4 h-4" />
                                    )}
                                </button>
                            </div>
                        </div>

                        {/* Remember me */}
                        <label className="flex items-center gap-2 pl-1 cursor-pointer select-none">
                            <Checkbox
                                checked={remember}
                                onCheckedChange={(v) => setRemember(v === true)}
                                className="data-[state=checked]:bg-(--red) data-[state=checked]:border-(--red) rounded-md"
                            />
                            <span className="text-xs font-semibold text-black/50">
                                Запомнить меня
                            </span>
                        </label>

                        {/* Submit */}
                        <Button
                            type="submit"
                            disabled={!isFormValid || isPending}
                            className="mt-2 h-11 rounded-2xl bg-(--red) hover:bg-(--red)/90 text-white font-bold text-sm gap-2 shadow-sm disabled:opacity-50"
                        >
                            {isPending ? (
                                <>
                                    <Loader2 className="w-4 h-4 animate-spin" />
                                    Входим...
                                </>
                            ) : (
                                <>
                                    <LogIn className="w-4 h-4" />
                                    Войти
                                </>
                            )}
                        </Button>
                    </form>
                </div>

                <p className="text-center text-xs text-black/30 font-medium mt-6">
                    Нет доступа?{" "}
                    <a href="/contact-admin" className="font-bold text-(--red) hover:text-(--red)/80 transition-colors">
                        Свяжитесь с администратором
                    </a>
                </p>
            </div>
        </div>
    );
}