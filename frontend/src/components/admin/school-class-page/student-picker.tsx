import { Avatar } from "@/components/layout/layout";
import {
    useGetUnassignedStudents
} from "@/hooks/use-school-class";
import type { UserSimpleResponse } from "@/services/user-service";
import {
    Check,
    ChevronDown,
    Loader2,
    Search
} from "lucide-react";
import { useEffect, useRef, useState } from "react";

export default function StudentPicker({
    placeholder,
    value,
    onSelect,
}: {
    placeholder: string;
    value: UserSimpleResponse | null;
    onSelect: (user: UserSimpleResponse) => void;
}) {
    const [open, setOpen] = useState(false);
    const [search, setSearch] = useState("");
    const ref = useRef<HTMLDivElement>(null);

    const { data: unassignedStudents = [], isLoading } = useGetUnassignedStudents();

    useEffect(() => {
        const handler = (e: MouseEvent) => {
            if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
        };
        document.addEventListener("mousedown", handler);
        return () => document.removeEventListener("mousedown", handler);
    }, []);

    const handleSelect = (u: UserSimpleResponse) => {
        setOpen(false);
        setSearch("");
        onSelect(u);
    };

    const filteredStudents = unassignedStudents.filter((u) => {
        const fullName = `${u.firstName ?? ""} ${u.lastName ?? ""}`.toLowerCase();
        return fullName.includes(search.toLowerCase());
    });

    const displayName = value ? `${value.firstName} ${value.lastName}` : null;

    return (
        <div ref={ref} className="relative w-full">
            <button
                type="button"
                onClick={() => setOpen((p) => !p)}
                className="w-full h-10 flex items-center justify-between gap-2 px-3 bg-white/60 border border-black/10 rounded-xl text-sm font-semibold text-left focus:outline-none focus:ring-2 focus:ring-(--red) transition"
            >
                <span className={displayName ? "text-(--navy)" : "text-black/30 font-normal"}>
                    {displayName ?? placeholder}
                </span>
                <ChevronDown className={`w-4 h-4 text-black/30 transition-transform ${open ? "rotate-180" : ""}`} />
            </button>

            {open && (
                <div className="absolute z-50 mt-1 w-full rounded-2xl bg-white border border-black/8 shadow-xl overflow-hidden flex flex-col max-h-60">
                    <div className="p-2 border-b border-black/6 shrink-0">
                        <div className="relative">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-black/30" />
                            <input
                                autoFocus
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                placeholder="Поиск свободного ученика..."
                                className="w-full pl-8 pr-3 py-1.5 text-sm bg-black/4 rounded-lg outline-none placeholder:text-black/30 font-medium"
                            />
                        </div>
                    </div>
                    <div className="overflow-y-auto flex-1">
                        {isLoading ? (
                            <div className="flex justify-center py-4">
                                <Loader2 className="w-4 h-4 animate-spin text-black/30" />
                            </div>
                        ) : filteredStudents.length === 0 ? (
                            <p className="text-xs text-black/30 text-center py-4 font-semibold">Нет свободных учеников</p>
                        ) : (
                            filteredStudents.map((u) => {
                                const isActive = value?.id === u.id;
                                return (
                                    <button
                                        key={u.id}
                                        type="button"
                                        onClick={() => handleSelect(u)}
                                        className={`w-full flex items-center gap-3 px-3 py-2.5 text-sm hover:bg-black/4 transition text-left ${isActive ? "bg-(--red-light)/40" : ""}`}
                                    >
                                        <Avatar firstName={u.firstName} lastName={u.lastName} />
                                        <span className="font-semibold text-(--navy) flex-1 truncate">{`${u.firstName} ${u.lastName}`}</span>
                                        {isActive && <Check className="w-3.5 h-3.5 text-(--red) shrink-0" />}
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