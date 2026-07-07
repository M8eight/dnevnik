import { Link } from "react-router-dom";
import { ShieldAlert } from "lucide-react";

export default function ForbiddenPage() {
    return (
        <div className="min-h-screen flex flex-col items-center justify-center gap-4 text-center px-4">
            <ShieldAlert className="w-16 h-16 text-red-500" />
            <h1 className="text-2xl font-black text-(--navy)">Доступ запрещён</h1>
            <p className="text-black/50 max-w-md">
                У вас недостаточно прав для просмотра этой страницы.
            </p>
            <Link
                to="/"
                className="mt-2 px-5 h-10 flex items-center rounded-2xl bg-(--red) text-white text-sm font-bold"
            >
                На главную
            </Link>
        </div>
    );
}