import { useEffect, useRef, useState } from "react";
import { useSelector } from "react-redux";
import { Check, ChevronDown, Loader2, Search, X } from "lucide-react";
import { Avatar } from "@/components/layout/layout";
import { cn } from "@/lib/utils";
import { useFindUsersByFilter } from "@/hooks/use-user";
import { selectUserId } from "@/store/slices/authSlice";
import type { UserResponse, UserRole } from "@/services/user-service";

const ROLE_LABELS: Record<UserRole, string> = {
  STUDENT: "Ученик",
  PARENT: "Родитель",
  TEACHER: "Учитель",
  ADMIN: "Администратор",
};

const ROLE_TONE: Record<UserRole, string> = {
  STUDENT: "bg-(--navy)/8 text-(--navy)",
  PARENT: "bg-emerald-100/70 text-emerald-700",
  TEACHER: "bg-(--red-light)/60 text-(--red)",
  ADMIN: "bg-black/6 text-black/60",
};

const ROLE_FILTERS: { value: UserRole | undefined; label: string }[] = [
  { value: undefined, label: "Все" },
  { value: "TEACHER", label: "Учителя" },
  { value: "STUDENT", label: "Ученики" },
  { value: "PARENT", label: "Родители" },
  { value: "ADMIN", label: "Админы" },
];

export default function RecipientPicker({
  value,
  onChange,
}: {
  value: UserResponse[];
  onChange: (users: UserResponse[]) => void;
}) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState("");
  const [role, setRole] = useState<UserRole | undefined>(undefined);
  const ref = useRef<HTMLDivElement>(null);
  const currentUserId = useSelector(selectUserId);

  const {
    data,
    isLoading,
    isFetchingNextPage,
    hasNextPage,
    fetchNextPage,
  } = useFindUsersByFilter(30, role, search || undefined);
  const users = (data?.pages.flatMap((page) => page.content) ?? []).filter(
    (u) => u.id !== currentUserId,
  );

  const scrollRef = useRef<HTMLDivElement>(null);

  const handleScroll = () => {
    const el = scrollRef.current;
    if (!el || isFetchingNextPage || !hasNextPage) return;
    if (el.scrollTop + el.clientHeight >= el.scrollHeight - 60) {
      fetchNextPage();
    }
  };

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const isSelected = (id: number) => value.some((u) => u.id === id);

  const toggle = (u: UserResponse) => {
    onChange(isSelected(u.id) ? value.filter((v) => v.id !== u.id) : [...value, u]);
  };

  return (
    <div ref={ref} className="relative w-full">
      <div
        onClick={() => setOpen(true)}
        className="min-h-11 w-full flex flex-wrap items-center gap-1.5 rounded-xl border border-black/10 bg-white px-3 py-2 cursor-text transition focus-within:border-(--navy)/30 focus-within:ring-2 focus-within:ring-(--navy)/25"
      >
        {value.length === 0 && (
          <span className="text-sm font-normal text-black/30">Выберите получателей</span>
        )}
        {value.map((u) => (
          <span
            key={u.id}
            className="inline-flex items-center gap-1 rounded-full bg-(--navy)/8 py-0.5 pr-1 pl-2 text-xs font-semibold text-(--navy)"
          >
            {u.firstName} {u.lastName}
            <button
              type="button"
              aria-label={`Убрать ${u.firstName} ${u.lastName}`}
              onClick={(e) => {
                e.stopPropagation();
                toggle(u);
              }}
              className="grid size-4 place-items-center rounded-full text-(--navy)/60 hover:bg-(--navy)/15 hover:text-(--navy)"
            >
              <X className="size-3" />
            </button>
          </span>
        ))}
        <ChevronDown
          className={cn("ml-auto size-4 shrink-0 text-black/30 transition-transform", open && "rotate-180")}
        />
      </div>

      {open && (
        <div className="absolute z-50 mt-1 w-full rounded-2xl bg-white border border-black/8 shadow-xl overflow-hidden flex flex-col max-h-96">
          <div className="p-2.5 border-b border-black/6 shrink-0 space-y-2">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-black/30" />
              <input
                autoFocus
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Поиск по имени или логину..."
                className="w-full pl-8 pr-3 py-2 text-sm bg-black/4 rounded-lg outline-none placeholder:text-black/30 font-medium"
              />
            </div>
            <div className="flex flex-wrap gap-1.5">
              {ROLE_FILTERS.map((f) => (
                <button
                  key={f.label}
                  type="button"
                  onClick={() => setRole(f.value)}
                  className={cn(
                    "rounded-full px-2.5 py-1 text-xs font-semibold transition-colors",
                    role === f.value
                      ? "bg-(--navy) text-white"
                      : "bg-black/5 text-(--navy)/60 hover:bg-black/8",
                  )}
                >
                  {f.label}
                </button>
              ))}
            </div>
          </div>
          <div ref={scrollRef} onScroll={handleScroll} className="overflow-y-auto flex-1">
            {isLoading ? (
              <div className="flex justify-center py-6">
                <Loader2 className="w-4 h-4 animate-spin text-black/30" />
              </div>
            ) : users.length === 0 ? (
              <p className="text-xs text-black/30 text-center py-6 font-semibold">
                Пользователи не найдены
              </p>
            ) : (
              users.map((u) => {
                const selected = isSelected(u.id);
                return (
                  <button
                    key={u.id}
                    type="button"
                    onClick={() => toggle(u)}
                    className={cn(
                      "w-full flex items-center gap-3 px-3 py-2.5 text-sm hover:bg-black/4 transition text-left",
                      selected && "bg-(--navy)/6",
                    )}
                  >
                    <Avatar firstName={u.firstName} lastName={u.lastName} size="sm" color="navy" />
                    <div className="min-w-0 flex-1">
                      <p className="font-semibold text-(--navy) truncate">{`${u.firstName} ${u.lastName}`}</p>
                      <p className="text-xs text-black/40 truncate">@{u.username}</p>
                      <div className="mt-1 flex flex-wrap gap-1">
                        {u.roles.map((r) => (
                          <span
                            key={r}
                            className={cn("rounded-full px-1.5 py-0.5 text-[10px] font-bold", ROLE_TONE[r])}
                          >
                            {ROLE_LABELS[r]}
                          </span>
                        ))}
                      </div>
                    </div>
                    <span
                      className={cn(
                        "grid size-5 shrink-0 place-items-center rounded-full border transition-colors",
                        selected ? "border-(--navy) bg-(--navy) text-white" : "border-black/15 text-transparent",
                      )}
                    >
                      <Check className="size-3" />
                    </span>
</button>
                  );
                })
              )}
              {isFetchingNextPage ? (
                <div className="flex justify-center py-3">
                  <Loader2 className="size-4 animate-spin text-black/30" />
                </div>
              ) : hasNextPage ? (
                <button
                  type="button"
                  onClick={() => fetchNextPage()}
                  className="w-full py-2.5 text-center text-xs font-semibold text-(--navy) hover:bg-black/4"
                >
                  Загрузить ещё
                </button>
              ) : (
                <div className="py-2.5 text-center text-xs text-black/30">
                  Показаны все пользователи
                </div>
              )}
            </div>
          <div className="flex items-center justify-between border-t border-black/6 px-3 py-2 text-xs text-black/45">
            <span>Выбрано: {value.length}</span>
            <button
              type="button"
              onClick={() => setOpen(false)}
              className="font-semibold text-(--navy) hover:opacity-70"
            >
              Готово
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
