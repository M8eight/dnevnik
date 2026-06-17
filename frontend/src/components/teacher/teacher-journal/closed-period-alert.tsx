import { AlertTriangle } from "lucide-react";
import { Alert, AlertTitle, AlertDescription } from "@/components/ui/alert";
import { useJournalAccess } from "@/hooks/use-journal-access";

interface ClosedPeriodAlertProps {
  periodName?: string;
  yearName?: string;
}

export default function ClosedPeriodAlert({ periodName, yearName }: ClosedPeriodAlertProps) {
  const { isReadOnly, closedReason } = useJournalAccess();

  if (!isReadOnly) return null;

  const title = closedReason === "year" 
    ? `Учебный год закрыт (${yearName})`
    : `Период закрыт (${periodName})`;

  return (
    <div className="max-w-[1400px] mx-auto mb-6 animate-in fade-in slide-in-from-top-2 duration-300">
      <Alert variant="destructive" className="rounded-[24px] bg-gradient-to-r from-yellow-50 to-yellow-50/50 border-yellow-200/80 shadow-lg backdrop-blur-sm">
        <div className="flex items-start gap-4">
          <div className="flex-shrink-0 mt-0.5 w-10 h-10 rounded-[14px] bg-yellow-100/60 flex items-center justify-center">
            <AlertTriangle className="h-5 w-5 text-yellow-600" />
          </div>
          <div className="flex-1">
            <AlertTitle className="font-serif font-black tracking-tight text-base text-yellow-900 mb-1">
              {title}
            </AlertTitle>
            <AlertDescription className="text-sm text-yellow-800/85 font-medium leading-relaxed">
              Редактирование запрещено
            </AlertDescription>
          </div>
        </div>
      </Alert>
    </div>
  );
}