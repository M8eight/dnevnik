import { Layers } from "lucide-react";
import NavbarComponent from "./NavbarComponent";

const NAV_LINKS = [
    { to: "/parent/home", label: "Выбор ученика" },
    { to: "/student/home", label: "Главная" },
    { to: "/student/school-class", label: "Класс" },
    { to: "/student/diary", label: "Дневник" },
    { to: "/student/grade", label: "Оценки" },
];

export default function ParentNavbar() {
  return (
    <NavbarComponent
      title="Панель родителя"
      icon={<Layers className="w-5 h-5 text-(--red)" />}
      links={NAV_LINKS}
      roleLabel="родитель"
    />
  );
}