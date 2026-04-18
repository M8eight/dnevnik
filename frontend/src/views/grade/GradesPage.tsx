import React from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Download, Loader2 } from "lucide-react";
import { useGradesLessonsByStudentId } from "@/hooks/use-grade";
import type { GradeLessonDto } from "@/services/grade-service";

// --- Вспомогательные компоненты ---

function Chip({ children, className = "" }: { children: React.ReactNode; className?: string }) {
  return (
    <Badge
      variant="outline"
      className={`text-[11px] p-2 font-extrabold tracking-[0.18em] uppercase mb-3 rounded-md ${className}`}
    >
      {children}
    </Badge>
  );
}

function GradeBadge({ grade, size = "md" }: { grade?: number; size?: "sm" | "md" }) {
  if (!grade) {
    return (
      <div className="w-full flex justify-center items-center h-[32px]">
        <span className="text-[var(--ink-faint)] opacity-20 font-serif text-lg">•</span>
      </div>
    );
  }

  const styles: Record<number, string> = {
    5: "bg-[var(--green-light)] text-[var(--green)]",
    4: "bg-[var(--gold-light)]   text-[var(--gold)]",
    3: "bg-[var(--red-light)]    text-[var(--red)]",
    2: "bg-[var(--red-light)]    text-[var(--red)]",
  };

  const sizeClass = size === "md" ? "w-[34px] h-[34px] text-lg" : "w-[28px] h-[28px] text-base";

  return (
    <span
      className={`${sizeClass} rounded-[9px] flex items-center justify-center font-serif font-bold flex-shrink-0 shadow-sm transition-transform active:scale-95 ${
        styles[grade] ?? "bg-gray-100 text-gray-600"
      }`}
    >
      {grade}
    </span>
  );
}

// --- Хелперы ---

const formatDate = (dateStr: string) => {
  const [, month, day] = dateStr.split("-");
  return `${day}.${month}`;
};

/**
 * Расчет СРЕДНЕВЗВЕШЕННОГО балла
 * Формула: (балл1 * вес1 + балл2 * вес2) / (вес1 + вес2)
 */
const calculateWeightedAvg = (grades: GradeLessonDto[]) => {
  if (grades.length === 0) return 0;
  const totalValueWithWeight = grades.reduce((acc, g) => acc + g.value * g.weight, 0);
  const totalWeight = grades.reduce((acc, g) => acc + g.weight, 0);
  return totalValueWithWeight / totalWeight;
};

/**
 * Форматирование без лишних нулей (5.00 -> 5, 4.50 -> 4.5)
 */
const formatAvg = (val: number) => {
  if (val === 0) return "—";
  return parseFloat(val.toFixed(2)).toString();
};

export default function GradesPage() {
  const { data: response, isLoading, isError } = useGradesLessonsByStudentId(1, 4);

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Loader2 className="w-10 h-10 animate-spin text-[var(--red)]" />
      </div>
    );
  }

  if (isError || !response) {
    return (
      <div className="p-20 text-center font-serif text-xl text-[var(--navy)]">
        Ошибка загрузки электронного журнала
      </div>
    );
  }

  return (
    <div className="relative z-10 min-h-screen px-8 pt-24 pb-10 max-w-[1600px] mx-auto">
      {/* Header */}
      <header className="flex items-end justify-between mb-10 pb-6 border-b border-black/10">
        <div className="border-l-4 border-[var(--red)] pl-5">
          <p className="text-[10px] font-extrabold tracking-[0.22em] text-[var(--red)] uppercase mb-1">
            ✦ Учебный процесс {response.academicPeriod.schoolYear}
          </p>
          <h1 className="font-serif font-black text-[clamp(2rem,4.5vw,3.2rem)] text-[var(--navy)] leading-none">
            Успеваемость <em className="not-italic text-[var(--red)]">ученика</em>
          </h1>
        </div>

        <div className="flex gap-3">
          <Select defaultValue={response.academicPeriod.id.toString()}>
            <SelectTrigger className="w-[170px] bg-[var(--bg-card)] border-black/10 font-bold uppercase text-[9px] tracking-widest rounded-xl shadow-sm">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={response.academicPeriod.id.toString()}>
                {response.academicPeriod.name}
              </SelectItem>
            </SelectContent>
          </Select>
          <Button
            variant="outline"
            className="border-black/10 bg-[var(--bg-card)] text-[9px] font-bold uppercase tracking-widest rounded-xl shadow-sm"
          >
            <Download className="mr-2 h-3.5 w-3.5" /> PDF
          </Button>
        </div>
      </header>

      {/* Main Table Card */}
      <Card className="bg-[var(--bg-card)] border-black/10 rounded-[28px] overflow-hidden shadow-2xl shadow-black/[0.03]">
        <CardContent className="p-0">
          <div className="p-7 pb-4">
            <Chip className="border-[var(--navy)] text-[var(--navy)] bg-[var(--navy-light)]/30">
              Электронный журнал
            </Chip>
          </div>

          <ScrollArea className="w-full whitespace-nowrap">
            <Table className="border-collapse min-w-full">
              <TableHeader className="bg-black/[0.02]">
                <TableRow className="hover:bg-transparent border-black/5">
                  <TableHead className="w-[220px] pl-7 h-14 sticky left-0 bg-[var(--bg-card)] z-30 border-r border-black/5 font-extrabold text-[10px] uppercase tracking-widest text-[var(--ink-dim)]">
                    Предмет
                  </TableHead>
                  {response.dates.map((date) => (
                    <TableHead
                      key={date}
                      className="text-center min-w-[85px] px-2 font-bold text-[10px] text-[var(--ink-faint)] border-r border-black/[0.03]"
                    >
                      {formatDate(date)}
                    </TableHead>
                  ))}
                  <TableHead className="w-[90px] sticky right-0 z-30 bg-[var(--bg-card2)] border-l border-black/10 text-center font-extrabold text-[10px] uppercase tracking-widest text-[var(--red)] shadow-[-10px_0_15px_rgba(0,0,0,0.02)]">
                    Средний
                  </TableHead>
                </TableRow>
              </TableHeader>

              <TableBody>
                {response.subjects.map((sub) => {
                  const weightedAvg = calculateWeightedAvg(sub.grades);
                  return (
                    <TableRow
                      key={sub.subject}
                      className="hover:bg-black/[0.01] border-black/5 transition-colors group h-16"
                    >
                      <TableCell className="pl-7 py-0 font-bold text-[14px] text-[var(--navy)] sticky left-0 bg-[var(--bg-card)] z-20 border-r border-black/5 group-hover:bg-[#f9f5eb] transition-colors">
                        {sub.subject}
                      </TableCell>

                      {response.dates.map((date) => {
                        const dayGrades = sub.grades.filter((g) => g.date === date);

                        return (
                          <TableCell
                            key={date}
                            className="p-0 text-center border-r border-black/[0.03] min-w-[85px]"
                          >
                            <div className="flex items-center justify-center h-16 px-1">
                              {dayGrades.length > 0 ? (
                                <div
                                  className={`flex items-center justify-center ${
                                    dayGrades.length > 1 ? "gap-0.5" : ""
                                  }`}
                                >
                                  {dayGrades.map((g, idx) => (
                                    <React.Fragment key={g.gradeId}>
                                      <GradeBadge
                                        grade={g.value}
                                        size={dayGrades.length > 1 ? "sm" : "md"}
                                      />
                                      {dayGrades.length > 1 && idx === 0 && (
                                        <span className="text-[var(--ink-faint)] opacity-30 font-light mx-[-1px] select-none">
                                          /
                                        </span>
                                      )}
                                    </React.Fragment>
                                  ))}
                                </div>
                              ) : (
                                <GradeBadge />
                              )}
                            </div>
                          </TableCell>
                        );
                      })}

                      <TableCell className="sticky right-0 z-20 bg-[var(--bg-card2)] border-l border-black/10 p-0 group-hover:bg-[#f3ead3] transition-colors shadow-[-10px_0_15px_rgba(0,0,0,0.02)]">
                        <div className="flex flex-col items-center justify-center h-16">
                          <span className="font-serif text-xl font-black text-[var(--navy)]">
                            {formatAvg(weightedAvg)}
                          </span>
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
            
            {/* Улучшенный ползунок скролла */}
            <ScrollBar 
              orientation="horizontal" 
              className="h-2.5 bg-black/[0.02] hover:bg-black/[0.05] transition-colors cursor-grab active:cursor-grabbing"
            />
          </ScrollArea>
        </CardContent>
      </Card>

      {/* Footer Legend */}
      <div className="mt-8 flex flex-wrap justify-center md:justify-start gap-8 opacity-60">
        {[
          { color: "var(--green)", label: "Отлично" },
          { color: "var(--gold)", label: "Хорошо" },
          { color: "var(--red)", label: "Пересдача" },
        ].map((item) => (
          <div
            key={item.label}
            className="flex items-center gap-2.5 text-[9px] font-bold uppercase tracking-[0.2em]"
          >
            <span
              className="w-2.5 h-2.5 rounded-full shadow-sm"
              style={{ backgroundColor: item.color }}
            />
            {item.label}
          </div>
        ))}
      </div>
    </div>
  );
}