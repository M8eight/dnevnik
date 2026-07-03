import { useState } from "react";
import { cn } from "@/lib/utils";
import { Layers, Menu, X } from "lucide-react";
import { NavLink } from "react-router-dom";

const NAV_LINKS = [
    { to: "/admin/subject", label: "Предмет" },
    { to: "/admin/period", label: "Четверть" },
    { to: "/admin/school-class", label: "Класс" },
    { to: "/admin/user", label: "Пользователь" },
    { to: "/admin/schedule", label: "Расписание" },
    { to: "/admin/academic-year", label: "Учебный год" },
];

function NavItem({ to, label }: { to: string; label: string }) {
    return (
        <NavLink
            to={to}
            className={({ isActive }) =>
                cn(
                    "glass-pill px-5 h-10 flex items-center rounded-2xl text-[12px] font-extrabold uppercase tracking-wider transition-all",
                    isActive
                        ? "text-(--navy) bg-white/40 shadow-sm"
                        : "text-black/30 hover:text-(--navy) hover:bg-white/20"
                )
            }
        >
            {label}
        </NavLink>
    );
}

function MobileNavItem({ to, label, onClick }: { to: string; label: string; onClick: () => void }) {
    return (
        <NavLink
            to={to}
            onClick={onClick}
            className={({ isActive }) =>
                cn(
                    "flex items-center px-4 h-11 rounded-2xl text-[13px] font-extrabold uppercase tracking-wider transition-all",
                    isActive
                        ? "text-(--navy) bg-white/50 shadow-sm"
                        : "text-black/40 hover:text-(--navy) hover:bg-white/25"
                )
            }
        >
            {label}
        </NavLink>
    );
}

export default function AdminNavbar() {
    const [open, setOpen] = useState(false);

    return (
        <header className="mb-6 relative">
            <div className="max-w-350 mx-auto">
                <div className="glass-card rounded-[24px] px-6 h-16 flex items-center justify-between border-none shadow-lg backdrop-blur-md">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-[14px] bg-(--red-light)/60 flex items-center justify-center ring-1 ring-(--red)/10">
                            <Layers className="w-5 h-5 text-(--red)" />
                        </div>
                        <span className="font-serif font-black text-[1.2rem] text-(--navy) tracking-tight">
                            Панель администратора
                        </span>
                    </div>

                    <nav className="hidden lg:flex items-center gap-2">
                        {NAV_LINKS.map((link) => (
                            <NavItem key={link.to} {...link} />
                        ))}
                    </nav>

                    <div className="flex items-center gap-4">
                        <div className="text-right hidden sm:block">
                            <p className="text-[13px] font-black text-(--navy) leading-none mb-1">
                                Администратор
                            </p>
                            <p className="text-[9px] font-extrabold tracking-[0.2em] uppercase text-black/25">
                                Admin
                            </p>
                        </div>
                        <div className="w-11 h-11 rounded-[15px] bg-(--navy-light)/40 ring-1 ring-black/5 flex items-center justify-center shadow-inner">
                            <span className="font-serif font-black text-[15px] text-(--navy)">А</span>
                        </div>

                        {/* Бургер — только на мобильных/планшетах */}
                        <button
                            type="button"
                            onClick={() => setOpen((v) => !v)}
                            aria-label="Открыть меню"
                            aria-expanded={open}
                            className="lg:hidden w-10 h-10 rounded-[14px] glass-pill flex items-center justify-center text-(--navy) transition-all"
                        >
                            {open ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
                        </button>
                    </div>
                </div>

                {/* Мобильное меню */}
                <div
                    className={cn(
                        "lg:hidden overflow-hidden transition-all duration-300 ease-out",
                        open ? "max-h-96 opacity-100 mt-3" : "max-h-0 opacity-0 mt-0"
                    )}
                >
                    <nav className="glass-card rounded-[24px] p-3 shadow-lg backdrop-blur-md flex flex-col gap-1">
                        {NAV_LINKS.map((link) => (
                            <MobileNavItem key={link.to} {...link} onClick={() => setOpen(false)} />
                        ))}
                    </nav>
                </div>
            </div>
        </header>
    );
}