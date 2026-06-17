import { useState } from "react";
import { Loader2, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useCreateClass } from "@/hooks/use-school-class";

export interface CreateClassFormProps {
    academicYearId: number;
}

export default function CreateClassForm({ academicYearId }: CreateClassFormProps) {
    const [name, setName] = useState("");

    const createMutation = useCreateClass();

    const handleSubmit = () => {
        if (!name.trim()) return;
        createMutation.mutate(
            { name: name.trim(), academicYearId: academicYearId },
            {
                onSuccess: () => {
                    setName("");
                },
            }
        );
    };

    const isValid = name.trim();

    return (
        <div className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
                <Label className="text-xs font-bold text-black/50 uppercase tracking-wide">
                    Название класса
                </Label>
                <Input
                    placeholder="Например: 5А"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="h-11 bg-white/40 border-black/10 rounded-2xl text-sm font-semibold placeholder:font-normal focus-visible:ring-[var(--red)]"
                />
            </div>

            <Button
                onClick={handleSubmit}
                disabled={!isValid || createMutation.isPending}
                className="mt-1 h-11 rounded-2xl bg-[var(--red)] hover:bg-[var(--red)]/90 text-white font-bold text-sm gap-2 disabled:opacity-40"
            >
                {createMutation.isPending ? (
                    <Loader2 className="w-4 h-4 animate-spin" />
                ) : (
                    <Plus className="w-4 h-4" />
                )}
                Создать класс
            </Button>

            {createMutation.isError && (
                <p className="text-xs text-red-500 font-semibold text-center">
                    Ошибка при создании класса
                </p>
            )}
        </div>
    );
}