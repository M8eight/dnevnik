import type { ROLES } from "@/constants/component-constants";
import { cn } from "@/lib/utils";

export default function RoleTab({
    role,
    active,
    onClick,
}: {
    role: (typeof ROLES)[number];
    active: boolean;
    onClick: () => void;
}) {
    return (
        <button
            type="button"
            onClick={onClick}
            className={cn(
                "flex-1 flex items-center justify-center gap-2 h-11 rounded-2xl text-[12px] font-extrabold uppercase tracking-wider transition-all",
                active
                    ? cn("bg-white/60 shadow-sm", role.color)
                    : "text-black/30 hover:text-black/50 hover:bg-white/30"
            )}
        >
            <span className={active ? role.color : ""}>{role.icon}</span>
            {role.label}
        </button>
    );
}