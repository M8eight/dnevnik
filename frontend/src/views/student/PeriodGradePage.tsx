import GradeBadge from "@/components/student/home/grade-badge";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";
import { TableBody, TableCell, TableHeader, TableHead, TableRow } from "@/components/ui/table";
import { useStudentPeriodGrades } from "@/hooks/use-period-grade";
import type { AcademicPeriodResponse } from "@/services/grade-service";
import StudentNavbar from "@/templates/navbars/StudentNavbar";
import { TrendingUp, Star, BookOpen, CalendarDays, Table } from "lucide-react";
import { useMemo } from "react";

export default function PeriodGradesPage() {
  const { data, isLoading, isError } = useStudentPeriodGrades(27);

  if (isLoading) { /* loader */ }
  if (isError || !data) { /* error */ }

  // Собираем уникальные периоды из всех предметов
  const allPeriods = useMemo(() => {
    const seen = new Map<number, AcademicPeriodResponse>();
    Object.values(data).flat().forEach(g => {
      if (!seen.has(g.academicPeriod.id)) seen.set(g.academicPeriod.id, g.academicPeriod);
    });
    return [...seen.values()].sort((a, b) => a.id - b.id);
  }, [data]);

  // Статистика
  // Короткое имя периода
  const shortName = (name: string) =>
    ({ "Первая четверть": "Q1", "Вторая четверть": "Q2", "Третья четверть": "Q3", "Четвертая четверть": "Q4" }[name] ?? name);

  return (
    <div className="...">
      <StudentNavbar />

      {/* Header */}
      <header>...</header>

      {/* Table */}
      <div className="glass-card rounded-[28px] overflow-hidden">
        <ScrollArea className="w-full whitespace-nowrap">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="sticky left-0 ...">Предмет</TableHead>
                {allPeriods.map(p => (
                  <TableHead key={p.id} className="text-center min-w-[100px]">
                    <div className="font-extrabold">{shortName(p.name)}</div>
                    <div className="text-[9px] text-black/30">{p.name}</div>
                  </TableHead>
                ))}
                <TableHead className="sticky right-0 text-[var(--red)] text-center">Среднее</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {Object.entries(data).map(([subject, grades]) => {
                const subAvg = grades.reduce((s, g) => s + g.value, 0) / grades.length;
                return (
                  <TableRow key={subject}>
                    <TableCell className="sticky left-0 font-bold">{subject}</TableCell>
                    {allPeriods.map(p => {
                      const g = grades.find(gr => gr.academicPeriod.id === p.id);
                      return (
                        <TableCell key={p.id} className="text-center">
                          {g ? <GradeBadge grade={g.value} /> : <GradeBadge />}
                        </TableCell>
                      );
                    })}
                    <TableCell className="sticky right-0 text-center">
                      <span className="font-serif font-black text-xl">{subAvg.toFixed(1)}</span>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
          <ScrollBar orientation="horizontal" />
        </ScrollArea>
      </div>
    </div>
  );
}