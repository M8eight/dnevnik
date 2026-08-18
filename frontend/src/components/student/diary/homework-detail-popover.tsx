import { useState } from "react";
import { BookOpen } from "lucide-react";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import subjectColor from "@/components/student/home/subject-color-helper";

interface HomeworkPopoverProps {
  text: string;
  subjectName: string;
  variant?: "row" | "compact";
}

export function HomeworkPopover({ text, subjectName, variant = "row" }: HomeworkPopoverProps) {
  const [open, setOpen] = useState(false);
  const color = subjectColor(subjectName);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild onClick={(e) => e.stopPropagation()}>
        {variant === "row" ? (
          <button
            type="button"
            className="w-full flex items-center gap-3 px-3 py-2 rounded-[12px] bg-white/40 ring-1 ring-black/5 cursor-pointer hover:bg-white/60 transition-colors text-left"
          >
            <span className="w-2 h-2 rounded-full shrink-0" style={{ backgroundColor: color }} />
            <span className="text-[12px] font-semibold text-(--navy) truncate">{text}</span>
            <span className="ml-auto text-[9px] font-bold text-black/25 uppercase tracking-widest shrink-0">
              {subjectName}
            </span>
          </button>
        ) : (
          <button type="button" className="block w-full text-left cursor-pointer group">
            <p className="text-[12px] text-black/35 italic leading-snug line-clamp-2 group-hover:text-black/55 transition-colors">
              {text}
            </p>
          </button>
        )}
      </PopoverTrigger>

      <PopoverContent
        align="start"
        sideOffset={8}
        className="w-80 rounded-[18px] border border-black/6 bg-white p-0 shadow-[0_8px_30px_rgba(30,42,68,0.12)] overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center gap-3 px-5 py-4 bg-amber-200/15">
          <span className="flex items-center justify-center w-10 h-10 rounded-[12px] shrink-0">
            <BookOpen className="w-4.5 h-4.5" />
          </span>
          <div className="min-w-0">
            <p className="font-bold text-[13px] text-(--navy) leading-tight truncate">{subjectName}</p>
            <p className="text-[11px] text-black/40 mt-0.5">Домашнее задание</p>
          </div>
        </div>

        <div className="px-5 py-4">
          <div className="rounded-[14px] bg-black/2 ring-1 ring-black/6 p-4">
            <p className="text-[13px] leading-relaxed text-(--navy) whitespace-pre-wrap">
              {text}
            </p>
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
}