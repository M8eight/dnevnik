import { useCreateSubject } from "@/hooks/use-subject";
import { Loader2, CheckCircle2, Send } from "lucide-react";
import { Input } from "../../ui/input";
import { useState } from "react";
import { Button } from "../../ui/button";


export default function CreateSubjectForm() {
    const [name, setName] = useState("");
    const [success, setSuccess] = useState(false);
    const createMutation = useCreateSubject();

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!name.trim()) return;
        createMutation.mutate(
            { subjectName: name.trim() },
            {
                onSuccess: () => {
                    setName("");
                    setSuccess(true);
                    setTimeout(() => setSuccess(false), 2500);
                },
            }
        );
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
                <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                    Название предмета
                </label>
                <Input
                    placeholder="Например: Математика"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    disabled={createMutation.isPending}
                    className="h-12 bg-white/40 border-black/10 rounded-2xl focus-visible:ring-[var(--red)] text-sm font-semibold placeholder:font-normal"
                />
            </div>

            <Button
                type="submit"
                disabled={!name.trim() || createMutation.isPending}
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
                <p className="text-xs text-[var(--red)] font-semibold text-center">
                    Ошибка при создании. Попробуйте ещё раз.
                </p>
            )}
        </form>
    );
}