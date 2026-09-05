import { AlertTriangle } from "lucide-react";
import { clearLogout } from "@/hooks/use-logout";
import ParentNavbar from "@/components/layout/navbars/ParentNavbar";

export default function NoStudentsAttached() {
    const handleLogout = () => {
        if (window.confirm("Выйти из аккаунта?")) {
            clearLogout();
        }
    };

    return (
        <div className="relative z-10 min-h-screen px-6 md:px-10 pt-2 pb-14">
            <ParentNavbar />
            <div className="flex flex-col items-center justify-center min-h-[60vh] text-center px-6 anim-in">
                <div className="w-14 h-14 rounded-[16px] bg-(--red-light)/60 flex items-center justify-center ring-1 ring-(--red)/10 mb-4">
                    <AlertTriangle className="w-6 h-6 text-(--red)" />
                </div>
                <h2 className="font-serif font-black text-xl text-(--navy) mb-2">Нет привязанных учеников</h2>
                <p className="text-sm text-black/40 max-w-sm mb-6">
                    К вашему аккаунту пока не привязан ни один ученик. Обратитесь к администратору школы.
                </p>
                <button
                    onClick={handleLogout}
                    className="px-5 py-2.5 rounded-full text-sm font-semibold text-(--red) border border-(--red)/20 hover:bg-(--red-light)/40 transition-colors"
                >
                    Выйти из аккаунта
                </button>
            </div>
        </div>
    );
}