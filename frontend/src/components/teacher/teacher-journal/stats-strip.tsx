import type { TeacherJournalResponse } from "@/services/teacher-journal-service";
import { Users, BookOpen, TrendingUp } from "lucide-react";

interface StatsStripProps {
  data: TeacherJournalResponse;
}

export default function StatsStrip({ data }: StatsStripProps) {
  const totalGrades = data.studentsJournal.reduce(
    (s, e) => s + e.grades.length,
    0
  );
  const totalAbsent = data.studentsJournal.reduce(
    (s, e) => s + e.attendances.filter((a) => a.status === "ABSENT").length,
    0
  );
  const allGrades = data.studentsJournal.flatMap((e) => e.grades.map((g) => g.value));
  const periodAvg = allGrades.length
    ? parseFloat(
        (allGrades.reduce((a, b) => a + b, 0) / allGrades.length).toFixed(2)
      ).toString()
    : "—";

  const stats = [
    { icon: Users, label: "Учеников", val: data.students.length, sub: data.academicPeriod?.name ?? "..." },
    { icon: BookOpen, label: "Уроков", val: data.lessonInstances.length, sub: `Оценок: ${totalGrades}` },
    { icon: TrendingUp, label: "Ср. балл", val: periodAvg, sub: `Пропусков: ${totalAbsent}` },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8 anim-in">
      {stats.map(({ icon: Icon, label, val, sub }) => (
        <div key={label} className="glass-card rounded-[22px] p-6 flex items-center gap-5">
          <div className="w-12 h-12 rounded-[14px] bg-[var(--navy-light)]/40 flex items-center justify-center flex-shrink-0">
            <Icon className="w-5 h-5 text-[var(--navy)]" />
          </div>
          <div>
            <p className="text-[10px] font-extrabold uppercase tracking-[0.2em] text-black/30 mb-1">{label}</p>
            <p className="font-serif text-[1.8rem] font-black text-[var(--navy)] leading-none mb-1">{val}</p>
            <p className="text-[11px] font-medium text-black/40">{sub}</p>
          </div>
        </div>
      ))}
    </div>
  );
}