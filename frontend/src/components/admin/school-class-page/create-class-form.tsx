import { useState, useEffect, useRef } from "react";
import { Loader2, Plus, Search, ChevronDown, Check } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useCreateClass } from "@/hooks/use-school-class";
import { useFindUsersByFilter } from "@/hooks/use-user"; // adjust path if needed
import type { UserResponse } from "@/services/user-service"; // adjust if needed

function Avatar({ name }: { name: string }) {
    const initials = name.split(" ").slice(0, 2).map((w) => w[0]).join("").toUpperCase();
    return (
        <div className="w-8 h-8 rounded-[10px] bg-gradient-to-br from-[var(--red-light)] to-[var(--red)]/20 flex items-center justify-center ring-1 ring-[var(--red)]/15 flex-shrink-0">
            <span className="text-xs font-black text-[var(--red)]">{initials || "?"}</span>
        </div>
    );
}

function TeacherPicker({
    value,
    onChange,
}: {
    value: UserResponse | null;
    onChange: (u: UserResponse | null) => void;
}) {
    const [open, setOpen] = useState(false);
    const [search, setSearch] = useState("");
    const ref = useRef<HTMLDivElement>(null);

    const { data, isLoading } = useFindUsersByFilter(0, 30, "TEACHER", search || undefined);
    const teachers = data?.content ?? [];

    useEffect(() => {
        const handler = (e: MouseEvent) => {
            if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
        };
        document.addEventListener("mousedown", handler);
        return () => document.removeEventListener("mousedown", handler);
    }, []);

    const displayName = value ? `${value.firstName} ${value.lastName}` : null;

    return (
        <div ref={ref} className="relative w-full">
            <button
                type="button"
                onClick={() => setOpen((p) => !p)}
                className="w-full h-11 flex items-center justify-between gap-2 px-3 bg-white/40 border border-black/10 rounded-2xl text-sm font-semibold text-left focus:outline-none focus:ring-2 focus:ring-[var(--red)] transition"
            >
                <span className={displayName ? "text-[var(--navy)]" : "text-black/30 font-normal"}>
                    {displayName ?? "Выбрать учителя..."}
                </span>
                <ChevronDown className={`w-4 h-4 text-black/30 flex-shrink-0 transition-transform ${open ? "rotate-180" : ""}`} />
            </button>

            {open && (
                <div className="absolute z-50 mt-1 w-full rounded-2xl bg-white border border-black/8 shadow-xl overflow-hidden">
                    <div className="p-2 border-b border-black/6">
                        <div className="relative">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-black/30" />
                            <input
                                autoFocus
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                placeholder="Поиск учителя..."
                                className="w-full pl-8 pr-3 py-1.5 text-sm bg-black/4 rounded-lg outline-none placeholder:text-black/30 font-medium"
                            />
                        </div>
                    </div>
                    <div className="max-h-48 overflow-y-auto">
                        {isLoading ? (
                            <div className="flex justify-center py-4">
                                <Loader2 className="w-4 h-4 animate-spin text-black/30" />
                            </div>
                        ) : teachers.length === 0 ? (
                            <p className="text-xs text-black/30 text-center py-4 font-semibold">Не найдено</p>
                        ) : (
                            teachers.map((t) => {
                                const name = `${t.firstName} ${t.lastName}`;
                                const isActive = value?.id === t.id;
                                return (
                                    <button
                                        key={t.id}
                                        type="button"
                                        onClick={() => { onChange(t); setOpen(false); setSearch(""); }}
                                        className={`w-full flex items-center gap-3 px-3 py-2.5 hover:bg-black/4 transition text-left ${isActive ? "bg-[var(--red-light)]/40" : ""}`}
                                    >
                                        <Avatar name={name} />
                                        <span className="font-semibold text-sm text-[var(--navy)] flex-1 truncate">{name}</span>
                                        {isActive && <Check className="w-3.5 h-3.5 text-[var(--red)] flex-shrink-0" />}
                                    </button>
                                );
                            })
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}

export default function CreateClassForm() {
    const [name, setName] = useState("");
    const [year, setYear] = useState("");
    const [teacher, setTeacher] = useState<UserResponse | null>(null);

    const createMutation = useCreateClass();

    const handleSubmit = () => {
        if (!name.trim() || !year.trim() || !teacher) return;
        createMutation.mutate(
            { name: name.trim(), year: year.trim(), classTeacherId: teacher.id },
            {
                onSuccess: () => {
                    setName("");
                    setYear("");
                    setTeacher(null);
                },
            }
        );
    };

    const isValid = name.trim() && year.trim() && teacher !== null;

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

            <div className="flex flex-col gap-1.5">
                <Label className="text-xs font-bold text-black/50 uppercase tracking-wide">
                    Учебный год
                </Label>
                <Input
                    placeholder="2024-2025"
                    value={year}
                    onChange={(e) => setYear(e.target.value)}
                    className="h-11 bg-white/40 border-black/10 rounded-2xl text-sm font-semibold placeholder:font-normal focus-visible:ring-[var(--red)]"
                />
            </div>

            <div className="flex flex-col gap-1.5">
                <Label className="text-xs font-bold text-black/50 uppercase tracking-wide">
                    Классный руководитель
                </Label>
                <TeacherPicker value={teacher} onChange={setTeacher} />
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