import { Badge } from "@/components/ui/badge";

export default function Chip({ children, className = "" }: { children: React.ReactNode; className?: string }) {
  return (
    <Badge
      variant="outline"
      className={`text-[10px] px-3 py-1 font-extrabold tracking-[0.2em] uppercase rounded-full mb-3 ${className}`}
    >
      {children}
    </Badge>
  );
}