import { Avatar } from "@/components/layout/layout";
import type { UserSimpleResponse } from "@/services/user-service";
import {
    Check,
    ChevronDown,
    Search
} from "lucide-react";
import { useEffect, useRef, useState } from "react";

export default function GroupStudentPicker({
    placeholder,
    value,
    onSelect,
    options,
}: {
    placeholder: string;
    value: UserSimpleResponse | null;
    onSelect: (user: UserSimpleResponse) => void;
    options: UserSimpleResponse[];
}) {
    const [open, setOpen] = useState(false);
    const [search, setSearch] = useState("");
    const ref = useRef<HTMLDivElement>(null);

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

    const filtered = options.filter((u) => {
        const fullName = `${u.firstName ?? ""} ${u.lastName ?? ""}`.toLowerCase();
        return fullName.includes(search.toLowerCase());
    });

    const displayName = value ? `${value.firstName ?? ""} ${value.lastName ?? ""}`.trim() : null;

    return (
        <div ref={ref} className="relative w-full">
            <button
                type="button"
                onClick={() => setOpen((p) => !p)}
                className="w-full h-10 flex items-center justify-between gap-2 px-3 bg-white/60 border border-black/10 rounded-xl text-sm font-semibold text-left focus:outline-none focus:ring-2 focus:ring-(--red) transition"
            >
                <span className={displayName ? "text-(--navy)" : "text-black/30 font-normal"}>
                    {displayName || placeholder}
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
                                placeholder="Поиск ученика..."
                                className="w-full pl-8 pr-3 py-1.5 text-sm bg-black/4 rounded-lg outline-none placeholder:text-black/30 font-medium"
                            />
                        </div>
                    </div>
                    <div className="overflow-y-auto flex-1">
                        {filtered.length === 0 ? (
                            <p className="text-xs text-black/30 text-center py-4 font-semibold">
                                {options.length === 0 ? "Все ученики класса уже в группе" : "Не найдено"}
                            </p>
                        ) : (
                            filtered.map((u) => {
                                const isActive = value?.id === u.id;
                                return (
                                    <button
                                        key={u.id}
                                        type="button"
                                        onClick={() => handleSelect(u)}
                                        className={`w-full flex items-center gap-3 px-3 py-2.5 text-sm hover:bg-black/4 transition text-left ${isActive ? "bg-(--red-light)/40" : ""}`}
                                    >
                                        <Avatar firstName={u.firstName} lastName={u.lastName} />
                                        <span className="font-semibold text-(--navy) flex-1 truncate">
                                            {`${u.firstName ?? ""} ${u.lastName ?? ""}`.trim() || `#${u.id}`}
                                        </span>
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