import { useState } from "react";
import { cn } from "@/lib/utils";
import { Menu, X, LogOut } from "lucide-react";
import { NavLink } from "react-router-dom";
import { useSelector } from "react-redux";
import { selectUsername } from "@/store/slices/authSlice";
import { clearLogout } from "@/hooks/use-logout";

type NavbarProps = {
  title: string;
  icon: React.ReactNode;
  links: { to: string; label: string }[];
  roleLabel: string;
};

export default function NavbarComponent({
  title,
  icon,
  links,
  roleLabel,
}: NavbarProps) {
  const [open, setOpen] = useState(false);
  const username = useSelector(selectUsername);

  const handleLogout = () => {
    if (window.confirm("Выйти из аккаунта?")) {
      clearLogout();
    }
  };

  return (
    <header className="mb-6 relative z-30">
      <div className="max-w-350 mx-auto px-4 md:px-10 pt-6">
        <div className="glass-card rounded-[24px] px-6 h-16 flex items-center justify-between border-none shadow-lg backdrop-blur-md">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-[14px] bg-(--red-light)/60 flex items-center justify-center ring-1 ring-(--red)/10">
              {icon}
            </div>
            <span className="font-serif font-black text-[1.2rem] text-(--navy) tracking-tight">
              {title}
            </span>
          </div>

          <nav className="hidden lg:flex items-center gap-2">
            {links.map((link) => (
              <NavItem key={link.to} {...link} />
            ))}
          </nav>

          <div className="flex items-center gap-3">
            <div className="text-right hidden sm:block">
              <p className="text-[13px] font-black text-(--navy) leading-none mb-1">
                {username}
              </p>
              <p className="text-[9px] font-extrabold tracking-[0.2em] uppercase text-black/25">
                {roleLabel}
              </p>
            </div>

            <div className="w-11 h-11 rounded-[15px] bg-(--navy-light)/40 ring-1 ring-black/5 flex items-center justify-center shadow-inner">
              <span className="font-serif font-black text-[15px] text-(--navy)">А</span>
            </div>

            {/* Кнопка выхода — теперь спокойная */}
            <button
              onClick={handleLogout}
              className="hidden sm:flex items-center justify-center w-10 h-10 rounded-xl hover:bg-black/5 transition-colors text-black/40 hover:text-black/70"
              title="Выйти из аккаунта"
            >
              <LogOut className="w-5 h-5" />
            </button>

            {/* Бургер */}
            <button
              type="button"
              onClick={() => setOpen((v) => !v)}
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
            {links.map((link) => (
              <MobileNavItem
                key={link.to}
                {...link}
                onClick={() => setOpen(false)}
              />
            ))}

            {/* Выход в мобильном меню */}
            <button
              onClick={handleLogout}
              className="flex items-center gap-3 px-4 h-11 rounded-2xl text-red-600/70 hover:text-red-600 hover:bg-red-50 transition-all mt-2"
            >
              <LogOut className="w-5 h-5" />
              <span className="font-medium">Выйти из аккаунта</span>
            </button>
          </nav>
        </div>
      </div>
    </header>
  );
}

/* Остальные компоненты NavItem и MobileNavItem без изменений */
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

function MobileNavItem({
  to,
  label,
  onClick,
}: {
  to: string;
  label: string;
  onClick: () => void;
}) {
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