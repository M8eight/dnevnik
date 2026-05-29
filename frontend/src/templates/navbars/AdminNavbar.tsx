import { cn } from "@/lib/utils";
import { Layers } from "lucide-react";
import { NavLink } from "react-router-dom";

function NavItem({ to, label }: { to: string; label: string }) {
    return (
        <NavLink
            to={to}
            className={({ isActive }) =>
                cn(
                    "glass-pill px-5 h-10 flex items-center rounded-2xl text-[12px] font-extrabold uppercase tracking-wider transition-all",
                    isActive
                        ? "text-[var(--navy)] bg-white/40 shadow-sm"
                        : "text-black/30 hover:text-[var(--navy)] hover:bg-white/20"
                )
            }
        >
            {label}
        </NavLink>
    );
}

export default function AdminNavbar() {
    return (
        <header className="mb-6">
                <div className="max-w-[1400px] mx-auto">
                    <div className="glass-card rounded-[24px] px-6 h-16 flex items-center justify-between border-none shadow-lg backdrop-blur-md">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 rounded-[14px] bg-[var(--red-light)]/60 flex items-center justify-center ring-1 ring-[var(--red)]/10">
                                <Layers className="w-5 h-5 text-[var(--red)]" />
                            </div>
                            <span className="font-serif font-black text-[1.2rem] text-[var(--navy)] tracking-tight">
                                Панель администратора
                            </span>
                        </div>

                        <nav className="hidden lg:flex items-center gap-2">
                            <NavItem to="/admin/subject" label="Предмет" />
                            <NavItem to="/admin/period" label="Четверть" />
                            <NavItem to="/admin/school-class" label="Класс" />
                            <NavItem to="/admin/user" label="Пользователь" />
                            <NavItem to="/admin/schedule" label="Расписание" />
                        </nav>

                        <div className="flex items-center gap-4">
                            <div className="text-right hidden sm:block">
                                <p className="text-[13px] font-black text-[var(--navy)] leading-none mb-1">
                                    Администратор
                                </p>
                                <p className="text-[9px] font-extrabold tracking-[0.2em] uppercase text-black/25">
                                    Admin
                                </p>
                            </div>
                            <div className="w-11 h-11 rounded-[15px] bg-[var(--navy-light)]/40 ring-1 ring-black/[0.05] flex items-center justify-center shadow-inner">
                                <span className="font-serif font-black text-[15px] text-[var(--navy)]">А</span>
                            </div>
                        </div>
                    </div>
                </div>
            </header>
    );
}