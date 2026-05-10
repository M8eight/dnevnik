import { VIEW_MODE_OPTIONS , type ViewMode} from "@/constants/component-constants";
import { cn } from "@/lib/utils";
import { Search, Download } from "lucide-react";


interface ToolbarPanelProps {
  searchQuery: string;
  onSearchChange: (v: string) => void;
  viewMode: ViewMode;
  onViewModeChange: (v: ViewMode) => void;
  onExport: () => void;
}

export default function ToolbarPanel({
  searchQuery,
  onSearchChange,
  viewMode,
  onViewModeChange,
  onExport,
}: ToolbarPanelProps) {
  return (
    <div className="glass-card rounded-[22px] px-5 py-4 flex items-center gap-4 border-none shadow-md backdrop-blur-md mb-6 anim-in">
      <div className="relative flex items-center w-[360px] shrink-0">
        <Search className="w-4 h-4 text-[var(--navy)]/40 absolute left-3 pointer-events-none" />
        <input
          type="text"
          placeholder="Поиск ученика..."
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          className="glass-pill h-10 w-full pl-9 pr-4 text-[12px] font-bold rounded-2xl text-[var(--navy)] border-0 shadow-sm outline-none focus:ring-2 focus:ring-[var(--navy)]/20 transition-all placeholder:text-[var(--navy)]/30"
        />
      </div>

      <div className="flex-1" />

      <div className="flex items-center bg-black/[0.04] p-1 rounded-[18px] shrink-0">
        {VIEW_MODE_OPTIONS.map((mode) => (
          <button
            key={mode.id}
            onClick={() => onViewModeChange(mode.id)}
            className={cn(
              "px-5 py-2 text-[12px] font-bold rounded-2xl transition-all duration-300",
              viewMode === mode.id
                ? "bg-white text-[var(--navy)] shadow-sm"
                : "text-black/40 hover:text-black/70"
            )}
          >
            {mode.label}
          </button>
        ))}
      </div>

      <div className="flex-1" />

      <button
        onClick={onExport}
        className="glass-pill h-10 px-5 flex items-center gap-2 text-[12px] font-bold rounded-2xl text-[var(--navy)] border-0 shadow-sm hover:bg-white/60 transition active:scale-95 shrink-0"
      >
        <Download className="w-4 h-4 text-[var(--red)]" />
        <span>Экспорт</span>
      </button>
    </div>
  );
}