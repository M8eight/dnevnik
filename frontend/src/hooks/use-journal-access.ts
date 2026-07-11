import { useContext, useMemo, createElement } from "react";
import type { ReactNode } from "react";

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
  periods?: AcademicPeriod[] | null;
}

export function JournalAccessProvider({
  children,
  currentPeriod,
  currentYear,
  periods,
}: JournalAccessProviderProps) {
  
  const value: JournalAccessContextType = useMemo(() => {
    const isPeriodClosed = Boolean(currentPeriod?.isClosed);
    const isYearClosed = currentYear ? currentYear.closed : false;
    const isReadOnly = isPeriodClosed || isYearClosed;

    const areAllPeriodsClosed = Boolean(
      periods && periods.length > 0 && periods.every((p) => p.isClosed)
    );

    const isFinalGradeReadOnly = isYearClosed || !areAllPeriodsClosed;

    let closedReason: "year" | "period" | null = null;
    if (isYearClosed) closedReason = "year";
    else if (isPeriodClosed) closedReason = "period";

    return {
      isReadOnly,
      isPeriodClosed,
      isYearClosed,
      areAllPeriodsClosed,
      isFinalGradeReadOnly,
      closedReason,
    };
  }, [currentPeriod, currentYear, periods]);

  return createElement(JournalAccessContext.Provider, { value }, children);
}

export function useJournalAccess() {
  const context = useContext(JournalAccessContext);
  if (!context) {
    throw new Error("useJournalAccess must be used within JournalAccessProvider");
  }
  return context;
}