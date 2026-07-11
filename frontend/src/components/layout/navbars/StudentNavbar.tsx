import { Layers } from "lucide-react";
import NavbarComponent from "./NavbarComponent";

const NAV_LINKS = [
    { to: "/student/home", label: "Главная" },
    { to: "/student/diary", label: "Дневник" },
    { to: "/student/grade", label: "Оценки" },
];

export default function StudentNavbar() {
  return (
    <NavbarComponent
      title="Школьный дневник"
      icon={<Layers className="w-5 h-5 text-(--red)" />}
      links={NAV_LINKS}
      roleLabel="ученик"
    />
  );
}