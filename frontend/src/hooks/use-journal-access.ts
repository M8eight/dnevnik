import { useContext, useMemo, createElement } from "react";
import type { ReactNode } from "react";

// ВАЖНО: Импортируем контекст и типы из первого файла!
// (Проверь путь, чтобы он соответствовал твоей структуре папок)
import { 
  JournalAccessContext, 
  type JournalAccessContextType,
  type AcademicPeriod,
  type AcademicYear
} from "@/contexts/JournalAccessContext"; 

interface JournalAccessProviderProps {
  children: ReactNode;
  currentPeriod?: AcademicPeriod | null;
  currentYear?: AcademicYear | null;
}

export function JournalAccessProvider({
  children,
  currentPeriod,
  currentYear,
}: JournalAccessProviderProps) {
  
  const value: JournalAccessContextType = useMemo(() => {
    const isPeriodClosed = Boolean(currentPeriod?.isClosed);
    const isYearClosed = currentYear ? !currentYear.isActive : false;
    const isReadOnly = isPeriodClosed || isYearClosed;
    
    let closedReason: "year" | "period" | null = null;
    if (isYearClosed) closedReason = "year";
    else if (isPeriodClosed) closedReason = "period";

    return {
      isReadOnly,
      isPeriodClosed,
      isYearClosed,
      closedReason,
    };
  }, [currentPeriod, currentYear]);

  // Оборачиваем провайдер
  return createElement(JournalAccessContext.Provider, { value }, children);
}

export function useJournalAccess() {
  const context = useContext(JournalAccessContext);
  if (!context) {
    throw new Error("useJournalAccess must be used within JournalAccessProvider");
  }
  return context;
}