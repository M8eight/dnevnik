import { Layers } from "lucide-react";
import NavbarComponent from "./NavbarComponent";

const NAV_LINKS = [
    { to: "/admin/subject", label: "Предмет" },
    { to: "/admin/period", label: "Четверть" },
    { to: "/admin/school-class", label: "Класс" },
    { to: "/admin/user", label: "Пользователь" },
    { to: "/admin/schedule", label: "Расписание" },
    { to: "/admin/academic-year", label: "Учебный год" },
];

export default function AdminNavbar() {
  return (
    <NavbarComponent
      title="Панель администратора"
      icon={<Layers className="w-5 h-5 text-(--red)" />}
      links={NAV_LINKS}
      roleLabel="админ"
    />
  );
}