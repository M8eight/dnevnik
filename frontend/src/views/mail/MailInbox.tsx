import { useMemo, useState } from "react";
import { ArrowLeft, Loader2, Mail, Reply, Search, SquarePen, Trash2 } from "lucide-react";
import { Dialog, DialogContent, DialogDescription, DialogTitle } from "@/components/ui/dialog";
import { cn } from "@/lib/utils";
import {
  useDeleteInboxMail,
  useDeleteSentMail,
  useInbox,
  useMarkMailRead,
  useSendDirectMail,
  useSent,
  useUnreadCount,
} from "@/hooks/use-mail";
import type { MailWithUserResponse } from "@/services/mail-service";
import type { UserResponse } from "@/services/user-service";
import RecipientPicker from "./RecipientPicker";

type MailFolder = "inbox" | "sent";

interface MailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const fullName = (u: { firstName: string; lastName: string }) => `${u.firstName} ${u.lastName}`;

const initials = (u: { firstName: string; lastName: string }) =>
  `${u.firstName[0] ?? ""}${u.lastName[0] ?? ""}`.toUpperCase();

const isToday = (d: Date) => d.toDateString() === new Date().toDateString();

const formatShort = (iso: string) => {
  const d = new Date(iso);
  return isToday(d)
    ? d.toLocaleTimeString("ru-RU", { hour: "2-digit", minute: "2-digit" })
    : d.toLocaleDateString("ru-RU", { day: "numeric", month: "short" });
};

const formatFull = (iso: string) =>
  new Date(iso).toLocaleString("ru-RU", {
    day: "numeric",
    month: "long",
    hour: "2-digit",
    minute: "2-digit",
  });

export default function MailDialog({ open, onOpenChange }: MailDialogProps) {
  const [folder, setFolder] = useState<MailFolder>("inbox");
  const [query, setQuery] = useState("");
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [composing, setComposing] = useState<{ replyTo?: MailWithUserResponse } | null>(null);

  const inboxQuery = useInbox(0, 50, open && folder === "inbox");
  const sentQuery = useSent(0, 50, open && folder === "sent");
  const unread = useUnreadCount(open);
  const markRead = useMarkMailRead();
  const deleteInbox = useDeleteInboxMail();
  const deleteSent = useDeleteSentMail();
  const removeMail = folder === "sent" ? deleteSent : deleteInbox;

  const activeQuery = folder === "inbox" ? inboxQuery : sentQuery;
  const mails = useMemo(() => activeQuery.data?.content ?? [], [activeQuery.data]);
  const unreadCount = unread.data ?? 0;

  const list = useMemo(() => {
    const q = query.trim().toLowerCase();
    return [...mails]
      .filter(
        (m) =>
          !q ||
          `${fullName(m.sender)} ${m.sender.username} ${m.subject} ${m.body}`.toLowerCase().includes(q),
      )
      .sort((a, b) => +new Date(b.sentAt) - +new Date(a.sentAt));
  }, [mails, query]);

  const selected = mails.find((m) => m.mailId === selectedId) ?? null;
  const showMain = composing !== null || selected !== null;

  const switchFolder = (next: MailFolder) => {
    setFolder(next);
    setSelectedId(null);
    setComposing(null);
  };

  const select = (m: MailWithUserResponse) => {
    setComposing(null);
    setSelectedId(m.mailId);
    if (folder === "inbox") markRead.mutate(m.mailId);
  };

  const back = () => {
    setComposing(null);
    setSelectedId(null);
  };

  const handleDelete = (mailId: number) => {
    removeMail.mutate(mailId, { onSuccess: back });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="glass-card flex h-[min(760px,88vh)] w-[calc(100vw-1.5rem)] max-w-5xl flex-col gap-0 overflow-hidden rounded-[22px] p-0 sm:max-w-5xl">
        <DialogTitle className="sr-only">Почта</DialogTitle>
        <DialogDescription className="sr-only">Сообщения от учителей и школы</DialogDescription>

        <div className="grid min-h-0 flex-1 md:grid-cols-[350px_1fr]">
          {/* ── Список ── */}
          <aside
            className={cn(
              "flex min-h-0 flex-col border-black/8 md:border-r",
              showMain && "hidden md:flex",
            )}
          >
            <div className="flex items-center justify-between gap-3 px-5 pt-5 pb-3">
              <h2 className="font-serif text-2xl font-black text-(--navy)">Почта</h2>
              <button
                type="button"
                onClick={() => {
                  setSelectedId(null);
                  setComposing({});
                }}
                className="mr-8 inline-flex items-center gap-1.5 rounded-full bg-(--navy) px-3.5 py-1.5 text-sm font-semibold text-white transition-opacity hover:opacity-90 md:mr-0"
              >
                <SquarePen className="size-4" />
                Написать
              </button>
            </div>

            <div className="flex gap-1 px-5 pb-3">
              {(
                [
                  ["inbox", "Входящие"],
                  ["sent", "Отправленные"],
                ] as const
              ).map(([id, label]) => (
                <button
                  key={id}
                  type="button"
                  onClick={() => switchFolder(id)}
                  className={cn(
                    "rounded-full px-3 py-1 text-sm font-semibold transition-colors",
                    folder === id
                      ? "bg-(--navy)/10 text-(--navy)"
                      : "text-(--navy)/55 hover:text-(--navy)",
                  )}
                >
                  {label}
                  {id === "inbox" && unreadCount > 0 && (
                    <span className="ml-1.5 rounded-full bg-(--red) px-1.5 py-0.5 text-[11px] text-white">
                      {unreadCount}
                    </span>
                  )}
                </button>
              ))}
            </div>

            <label className="relative mx-5 mb-3 block">
              <Search className="absolute top-1/2 left-3 size-4 -translate-y-1/2 text-(--navy)/40" />
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Поиск по письмам"
                className="w-full rounded-xl border border-black/8 bg-white/60 py-2 pr-3 pl-9 text-sm text-(--navy) outline-none placeholder:text-(--navy)/40 focus:border-(--navy)/40"
              />
            </label>

            <ul className="flex min-h-0 flex-1 flex-col gap-1.5 overflow-y-auto px-3 pb-4">
              {activeQuery.isLoading && (
                <li className="flex items-center justify-center py-10 text-(--navy)/40">
                  <Loader2 className="size-5 animate-spin" />
                </li>
              )}
              {activeQuery.isError && (
                <li className="px-3 py-10 text-center text-sm text-(--red)">
                  Не удалось загрузить письма.
                </li>
              )}
              {!activeQuery.isLoading && !activeQuery.isError && list.length === 0 && (
                <li className="px-3 py-10 text-center text-sm text-(--navy)/55">
                  {query ? "Ничего не нашлось. Попробуйте другой запрос." : "В этой папке пока пусто."}
                </li>
              )}
              {list.map((m) => (
                <li key={m.mailId}>
                  <MailCard
                    mail={m}
                    folder={folder}
                    active={m.mailId === selectedId}
                    onClick={() => select(m)}
                  />
                </li>
              ))}
            </ul>
          </aside>

          {/* ── Письмо / написать ── */}
          <section className={cn("min-h-0 flex-col", showMain ? "flex" : "hidden md:flex")}>
            {composing ? (
              <Composer
                key={composing.replyTo?.mailId ?? "new"}
                replyTo={composing.replyTo}
                onBack={back}
                onSent={back}
              />
            ) : selected ? (
              <MailView
                mail={selected}
                folder={folder}
                onBack={back}
                onReply={() => setComposing({ replyTo: selected })}
                onDelete={() => handleDelete(selected.mailId)}
                deleting={removeMail.isPending}
              />
            ) : (
              <div className="m-auto flex max-w-xs flex-col items-center gap-3 px-6 text-center">
                <Mail className="size-10 text-(--navy)/25" strokeWidth={1.4} />
                <p className="text-sm text-(--navy)/55">
                  Выберите письмо слева или напишите новое сообщение.
                </p>
              </div>
            )}
          </section>
        </div>
      </DialogContent>
    </Dialog>
  );
}

/* ───────────── Карточка письма ───────────── */

function MailCard({
  mail,
  folder,
  active,
  onClick,
}: {
  mail: MailWithUserResponse;
  folder: MailFolder;
  active: boolean;
  onClick: () => void;
}) {
  const tone = folder === "sent" ? "bg-(--red-light)/60 text-(--red)" : "bg-(--navy)/8 text-(--navy)";
  const person = folder === "sent" ? "Вы" : fullName(mail.sender);

  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "w-full rounded-2xl border p-3.5 text-left transition-colors focus-visible:outline-2 focus-visible:outline-(--navy)",
        active ? "border-(--navy)/20 bg-white shadow-sm" : "border-transparent hover:bg-white/60",
      )}
    >
      <div className="flex items-start gap-3">
        <span className={cn("grid size-9 shrink-0 place-items-center rounded-full text-xs font-bold", tone)}>
          {folder === "sent" ? "Я" : initials(mail.sender)}
        </span>

        <div className="min-w-0 flex-1">
          <div className="flex items-baseline justify-between gap-2">
            <span className="truncate text-sm font-bold text-(--navy)">{person}</span>
            <time className="shrink-0 text-xs text-(--navy)/50">{formatShort(mail.sentAt)}</time>
          </div>

          <p className="truncate text-sm font-semibold text-(--navy)">{mail.subject}</p>
          <p className="mt-0.5 line-clamp-2 text-[13px] leading-snug text-(--navy)/55">
            {mail.body.replace(/\n+/g, " ")}
          </p>
        </div>
      </div>
    </button>
  );
}

/* ───────────── Просмотр письма ───────────── */

function MailView({
  mail,
  folder,
  onBack,
  onReply,
  onDelete,
  deleting,
}: {
  mail: MailWithUserResponse;
  folder: MailFolder;
  onBack: () => void;
  onReply: () => void;
  onDelete: () => void;
  deleting: boolean;
}) {
  const tone = folder === "sent" ? "bg-(--red-light)/60 text-(--red)" : "bg-(--navy)/8 text-(--navy)";
  const canReply = folder === "inbox";

  return (
    <>
      <BackBar onBack={onBack} />
      <article className="min-h-0 flex-1 overflow-y-auto px-6 pt-5 pb-6 md:px-10 md:pt-8">
        <h3 className="max-w-2xl pr-8 font-serif text-[clamp(1.5rem,2.6vw,2rem)] leading-tight font-black text-(--navy)">
          {mail.subject}
        </h3>

        <div className="mt-5 flex items-center gap-3 border-b border-black/8 pb-5">
          <span className={cn("grid size-11 place-items-center rounded-full text-sm font-bold", tone)}>
            {folder === "sent" ? "Я" : initials(mail.sender)}
          </span>
          <div className="min-w-0">
            <p className="truncate text-sm font-bold text-(--navy)">
              {folder === "sent" ? "Вы" : fullName(mail.sender)}
            </p>
            <p className="truncate text-xs text-(--navy)/55">
              @{mail.sender.username}, {formatFull(mail.sentAt)}
            </p>
          </div>
        </div>

        <div className="mt-6 max-w-[62ch] space-y-4 font-serif text-base leading-relaxed text-(--navy)/90">
          {mail.body.split("\n\n").map((p, i) => (
            <p key={i}>{p}</p>
          ))}
        </div>

        <div className="mt-8 flex items-center gap-3">
          {canReply && (
            <button
              type="button"
              onClick={onReply}
              className="inline-flex items-center gap-2 rounded-full border border-(--navy)/20 px-4 py-2 text-sm font-semibold text-(--navy) transition-colors hover:bg-(--navy)/5"
            >
              <Reply className="size-4" />
              Ответить
            </button>
          )}
          <button
            type="button"
            onClick={onDelete}
            disabled={deleting}
            className="ml-auto inline-flex items-center gap-2 rounded-full border border-(--red)/20 px-4 py-2 text-sm font-semibold text-(--red) transition-colors hover:bg-(--red)/5 disabled:opacity-40"
          >
            <Trash2 className="size-4" />
            Удалить
          </button>
        </div>
      </article>
    </>
  );
}

/* ───────────── Написать / ответить ───────────── */

function Composer({
  replyTo,
  onBack,
  onSent,
}: {
  replyTo?: MailWithUserResponse;
  onBack: () => void;
  onSent: () => void;
}) {
  const send = useSendDirectMail();
  const [recipients, setRecipients] = useState<UserResponse[]>(
    replyTo ? [{ ...replyTo.sender, roles: [] }] : [],
  );
  const [subject, setSubject] = useState(replyTo ? `Re: ${replyTo.subject}` : "");
  const [text, setText] = useState("");

  const ready = recipients.length > 0 && subject.trim() && text.trim() && !send.isPending;
  const inputClass =
    "w-full rounded-xl border border-black/10 bg-white px-3.5 py-2.5 text-sm text-(--navy) outline-none placeholder:text-(--navy)/35 transition focus:border-(--navy)/30 focus:ring-2 focus:ring-(--navy)/25";

  const handleSend = () => {
    if (recipients.length === 0 || !subject.trim() || !text.trim()) return;
    send.mutate(
      {
        subject: subject.trim(),
        body: text.trim(),
        recipientIds: recipients.map((r) => r.id),
      },
      {
        onSuccess: onSent,
        onError: () => window.alert("Не удалось отправить письмо. Попробуйте позже."),
      },
    );
  };

  return (
    <>
      <BackBar onBack={onBack} />
      <div className="flex min-h-0 flex-1 flex-col px-6 pt-5 pb-6 md:px-10 md:pt-8">
        <h3 className="pr-8 font-serif text-2xl font-black text-(--navy)">
          {replyTo ? "Ответ" : "Новое письмо"}
        </h3>

        <div className="mt-5 flex min-h-0 flex-1 flex-col gap-3 overflow-y-auto pr-1">
          {/* Кому */}
          <div className="rounded-2xl border border-black/8 bg-white/50 p-4">
            <label className="mb-2 block text-[11px] font-extrabold tracking-[0.15em] uppercase text-(--navy)/45">
              Кому
            </label>
            {replyTo ? (
              <div className="flex items-center gap-3 rounded-xl border border-black/8 bg-white px-3.5 py-2.5">
                <span className="grid size-8 shrink-0 place-items-center rounded-full bg-(--navy)/8 text-xs font-bold text-(--navy)">
                  {initials(replyTo.sender)}
                </span>
                <div className="min-w-0">
                  <p className="truncate text-sm font-semibold text-(--navy)">{fullName(replyTo.sender)}</p>
                  <p className="truncate text-xs text-black/40">@{replyTo.sender.username}</p>
                </div>
              </div>
            ) : (
              <RecipientPicker value={recipients} onChange={setRecipients} />
            )}
          </div>

          {/* Тема */}
          <div className="rounded-2xl border border-black/8 bg-white/50 p-4">
            <label className="mb-2 block text-[11px] font-extrabold tracking-[0.15em] uppercase text-(--navy)/45">
              Тема
            </label>
            <input
              className={inputClass}
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              placeholder="О чём письмо"
              aria-label="Тема"
            />
          </div>

          {/* Сообщение */}
          <div className="flex min-h-44 flex-1 flex-col rounded-2xl border border-black/8 bg-white/50 p-4">
            <label className="mb-2 block text-[11px] font-extrabold tracking-[0.15em] uppercase text-(--navy)/45">
              Сообщение
            </label>
            <textarea
              value={text}
              onChange={(e) => setText(e.target.value)}
              placeholder="Напишите сообщение..."
              aria-label="Текст письма"
              className="min-h-32 flex-1 resize-none bg-transparent font-serif text-base leading-relaxed text-(--navy) outline-none placeholder:text-(--navy)/35"
            />
          </div>
        </div>

        <div className="flex items-center justify-end pt-4">
          <button
            type="button"
            disabled={!ready}
            onClick={handleSend}
            className="inline-flex items-center gap-2 rounded-full bg-(--red) px-5 py-2 text-sm font-semibold text-white transition-opacity hover:opacity-90 disabled:opacity-40"
          >
            {send.isPending && <Loader2 className="size-4 animate-spin" />}
            Отправить
          </button>
        </div>
      </div>
    </>
  );
}

/* Кнопка «назад» — только на мобильных, где список и письмо на разных экранах */
function BackBar({ onBack }: { onBack: () => void }) {
  return (
    <div className="px-4 pt-4 md:hidden">
      <button
        type="button"
        onClick={onBack}
        className="inline-flex items-center gap-1.5 rounded-full px-2 py-1 text-sm font-semibold text-(--navy)/70 hover:text-(--navy)"
      >
        <ArrowLeft className="size-4" />
        К списку
      </button>
    </div>
  );
}
