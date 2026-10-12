import { useState } from "react";
import { Mail } from "lucide-react";
import MailDialog from "./MailInbox";
import { useUnreadCount } from "@/hooks/use-mail";

/**
 * Кнопка почты для навбара. Ставится справа.
 * Данные берутся из mail-service через хук use-mail.
 */
export default function MailButton() {
  const [open, setOpen] = useState(false);
  const { data: unread = 0 } = useUnreadCount();

  return (
    <>
      <button
        type="button"
        onClick={() => setOpen(true)}
        aria-label={unread ? `Почта, непрочитанных: ${unread}` : "Почта"}
        className="glass-card relative grid size-10 place-items-center rounded-full text-(--navy) transition-transform hover:scale-105 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-(--navy)"
      >
        <Mail className="size-5" strokeWidth={1.8} />
        {unread > 0 && (
          <span className="absolute -top-1 -right-1 grid min-w-5 place-items-center rounded-full bg-(--red) px-1 text-[11px] leading-5 font-bold text-white">
            {unread}
          </span>
        )}
      </button>

      <MailDialog open={open} onOpenChange={setOpen} />
    </>
  );
}
