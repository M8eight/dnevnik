import { useCreateAcademicPeriod } from "@/hooks/use-academic-period";
import { Loader2, CheckCircle2, Send } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useState } from "react";
import { Input } from "../../ui/input";

export default function CreatePeriodForm() {
    const [form, setForm] = useState({
        name: "",
        schoolYear: "",
        startDate: "",
        endDate: "",
    });
    const [success, setSuccess] = useState(false);
    const createMutation = useCreateAcademicPeriod();

    const isValid =
        form.name.trim() &&
        form.schoolYear.trim() &&
        form.startDate &&
        form.endDate;

    const handleChange = (field: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm((prev) => ({ ...prev, [field]: e.target.value }));
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!isValid) return;
        createMutation.mutate(
            {
                name: form.name.trim(),
                schoolYear: form.schoolYear.trim(),
                startDate: form.startDate,
                endDate: form.endDate,
            },
            {
                onSuccess: () => {
                    setForm({ name: "", schoolYear: "", startDate: "", endDate: "" });
                    setSuccess(true);
                    setTimeout(() => setSuccess(false), 2500);
                },
            }
        );
    };

    const fieldClass =
        "h-11 bg-white/40 border-black/10 rounded-2xl focus-visible:ring-[var(--red)] text-sm font-semibold placeholder:font-normal";

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            {/* Name */}
            <div className="space-y-1.5">
                <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                    Название
                </label>
                <Input
                    placeholder="Введите название четверти"
                    value={form.name}
                    onChange={handleChange("name")}
                    disabled={createMutation.isPending}
                    className={fieldClass}
                />
            </div>

            {/* School year */}
            <div className="space-y-1.5">
                <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                    Учебный год
                </label>
                <Input
                    placeholder="Например: 2024-2025"
                    value={form.schoolYear}
                    onChange={handleChange("schoolYear")}
                    disabled={createMutation.isPending}
                    className={fieldClass}
                />
            </div>

            {/* Dates */}
            <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                    <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                        Начало
                    </label>
                    <Input
                        type="date"
                        value={form.startDate}
                        onChange={handleChange("startDate")}
                        disabled={createMutation.isPending}
                        className={fieldClass}
                    />
                </div>
                <div className="space-y-1.5">
                    <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                        Конец
                    </label>
                    <Input
                        type="date"
                        value={form.endDate}
                        onChange={handleChange("endDate")}
                        disabled={createMutation.isPending}
                        className={fieldClass}
                    />
                </div>
            </div>

            <Button
                type="submit"
                disabled={!isValid || createMutation.isPending}
                className="w-full gap-2 bg-[var(--red)] hover:bg-[var(--red-dark)] text-white rounded-2xl py-6 text-sm font-bold shadow-lg shadow-[var(--red)]/20 transition-all active:scale-[0.98] disabled:opacity-40"
            >
                {createMutation.isPending ? (
                    <>
                        <Loader2 className="w-4 h-4 animate-spin" />
                        Создание...
                    </>
                ) : success ? (
                    <>
                        <CheckCircle2 className="w-4 h-4" />
                        Создана!
                    </>
                ) : (
                    <>
                        Создать четверть
                        <Send className="w-4 h-4" />
                    </>
                )}
            </Button>

            {createMutation.isError && (
                <p className="text-xs text-[var(--red)] font-semibold text-center">
                    Ошибка при создании. Попробуйте ещё раз.
                </p>
            )}
        </form>
    );
}