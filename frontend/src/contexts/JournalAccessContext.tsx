import { createContext } from "react";

export interface AcademicPeriod {
  id: number;
  isClosed: boolean;
  name: string;
}

export interface AcademicYear {
  id: number;
  closed: boolean;
  name: string;
}

export interface JournalAccessContextType {
  isReadOnly: boolean;
  isPeriodClosed: boolean;
  isYearClosed: boolean;
  areAllPeriodsClosed: boolean;
  isFinalGradeReadOnly: boolean;
  closedReason: "year" | "period" | null;
}

export const JournalAccessContext = createContext<JournalAccessContextType | null>(null);