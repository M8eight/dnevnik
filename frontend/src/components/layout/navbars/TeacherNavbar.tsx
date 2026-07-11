import { Layers } from "lucide-react";
import NavbarComponent from "./NavbarComponent";

const NAV_LINKS = [
    { to: "/teacher/journal", label: "Табель" },
    { to: "/teacher/homework", label: "Добавить ДЗ" },
];

export default function TeacherNavbar() {
  return (
    <NavbarComponent
      title="Школьный дневник"
      icon={<Layers className="w-5 h-5 text-(--red)" />}
      links={NAV_LINKS}
      roleLabel="учитель"
    />
  );
}