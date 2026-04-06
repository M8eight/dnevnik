import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { Skeleton } from "@/components/ui/skeleton";
import { useUser } from "@/hooks/use-user";
import { useAvgGrade, useGradesByDate } from "@/hooks/use-grade";
import type { User } from "@/services/user-service";
import { useScheduleByDate } from "@/hooks/use-schedule";

const student = {
  period: "3 четверть",
  rating: 4.8,
};

const homework = [
  { text: "Подготовиться к контрол.", subject: "Алгебра", color: "bg-[var(--red)]" },
  { text: "Реферат по истории", subject: "История", color: "bg-[var(--gold)]" },
];

// weekSchedule — пятидневка
const weekSchedule = [
  { day: "Пн", today: false, lessons: ["Математика", "Русский язык", "Химия"] },
  { day: "Вт", today: true, lessons: ["Математика", "Физика", "Литература"] },
  { day: "Ср", today: false, lessons: ["Химия", "История", "Алгебра"] },
  { day: "Чт", today: false, lessons: ["Английский", "Биология", "Физкультура"] },
  { day: "Пт", today: false, lessons: ["Информатика", "География", "Русский язык"] },
];

function UserCard({ user }: { user: User }) {

  return (
    <Card className="col-span-12 md:col-span-6 bg-[var(--bg-card)] border-black/10 relative overflow-hidden
                         hover:-translate-y-1 hover:shadow-xl transition-all duration-200">
      <CardContent className="p-7">

        <Chip className="border-[var(--red)] text-[var(--red)] bg-[var(--red-light)]">
          ученик
        </Chip>

        <div className="absolute top-5 right-6 w-14 h-14 rounded-full border-2 border-[var(--red)] opacity-15 flex items-center justify-center">
          <svg className="w-7 h-7 fill-[var(--red)]" viewBox="0 0 24 24">
            <path d="M12 2C9.243 2 7 4.243 7 7s2.243 5 5 5 5-2.243 5-5-2.243-5-5-5zM2.001 22c0-4.418 4.477-8 9.999-8s9.999 3.582 9.999 8H2.001z" />
          </svg>
        </div>

        <p className="font-serif text-2xl font-bold text-[var(--navy)] mb-4">
          {user?.lastName} {user?.firstName}
        </p>

        <div className="flex flex-wrap gap-10">
          {[
            { label: "Класс", value: user?.schoolClass?.name, accent: true },
            { label: "Профиль", value: user?.studyProfile },
            { label: "Период", value: student.period },
          ].map(({ label, value, accent }) => (
            <div key={label}>
              <p className="text-[9px] font-bold tracking-[0.15em] uppercase text-[var(--ink-faint)] mb-1">
                {label}
              </p>
              <p className={`font-bold ${accent ? "text-[var(--red)]" : "text-[var(--ink)]"}`}>
                {value}
              </p>
            </div>
          ))}
        </div>

      </CardContent>
    </Card>
  );
}


function Chip({ children, className = "" }: { children: React.ReactNode, className?: string }) {
  // shadcn Badge variant="outline" — только рамка
  // Tailwind: text-[10px] — произвольный размер через []
  // tracking-[0.18em] — произвольный letter-spacing через []
  return (
    <Badge
      variant="outline"
      className={`text-[11px] p-2 font-extrabold tracking-[0.18em] uppercase mb-3 rounded-md ${className}`}
    >
      {children}
    </Badge>
  );
}

// ─── Оценка — цвет бейджа зависит от значения ────────────
function GradeBadge({ grade }: { grade: number }) {
  // Объект-словарь: ключ → классы Tailwind
  // bg-[var(--green-light)] — обращаемся к CSS-переменной из index.css
  const styles: Record<number, string> = {
    5: "bg-[var(--green-light)] text-[var(--green)]",
    4: "bg-[var(--gold-light)]  text-[var(--gold)]",
    3: "bg-[var(--red-light)]   text-[var(--red)]",
    2: "bg-[var(--red-light)]   text-[var(--red)]",
  };
  return (
    <span
      className={`
        w-[38px] h-[38px] rounded-[10px] flex items-center justify-center
        font-serif text-xl font-bold flex-shrink-0
        ${styles[grade] ?? "bg-gray-100 text-gray-600"}
      `}
    >
      {grade}
    </span>
  );
}

function CurrentDate() {
  const now = new Date();

  const dayName = now.toLocaleDateString("ru-RU", { weekday: "long" });
  const day = now.getDate();
  const monthYear = now.toLocaleDateString("ru-RU", { month: "long", year: "numeric" });

  // "января, 2026" → capitalize первую букву
  const monthYearCapitalized = monthYear.charAt(0).toUpperCase() + monthYear.slice(1);

  return (
    <div className="text-right text-[11px] font-semibold text-[var(--ink-dim)] uppercase tracking-widest">
      {dayName}
      <strong className="block font-serif text-[1.6rem] font-bold text-[var(--ink)] normal-case tracking-normal leading-tight">
        {day}
      </strong>
      {monthYearCapitalized}
    </div>
  );
}


function Home() {

  const { data: user, isLoading, isError } = useUser(2);
  const { data: avgGrade } = useAvgGrade(2, 3);
  const { data: todayGrades } = useGradesByDate(1, "2026-04-08");
  const { data: todaySchedule } = useScheduleByDate(1, "MONDAY", "2026-03-30")

  return (
    <div className="relative z-10 min-h-screen px-8 pt-24 pb-10">

      <header className="flex items-end justify-between mb-10 pb-6 border-b border-black/10 max-w-6xl mx-auto">

        <div className="border-l-4 border-[var(--red)] pl-5">
          <p className="text-[10px] font-extrabold tracking-[0.22em] text-[var(--red)] uppercase mb-1">
            ✦ Академический год 25/26
          </p>
          <h1 className="font-serif font-black text-[clamp(2rem,4.5vw,3.2rem)] text-[var(--navy)] leading-none">
            Учебный{" "}
            <em className="not-italic text-[var(--red)]">дневник</em>
          </h1>
        </div>

        <CurrentDate />
      </header>

      <main className="grid grid-cols-12 gap-5 max-w-6xl mx-auto">

        {/* if (isLoading) return (
        <Card className="col-span-12 md:col-span-6 bg-[var(--bg-card)] border-black/10">
          <CardContent className="p-7">
            <p className="text-[var(--ink-faint)]">Загрузка...</p>
          </CardContent>
        </Card>
        ) */}

        {/* if (isError) return (
        <Card className="col-span-12 md:col-span-6 bg-[var(--bg-card)] border-black/10">
          <CardContent className="p-7">
            <p className="text-[var(--red)]">Ошибка загрузки</p>
          </CardContent>
        </Card>
        ) */}

        {isLoading ? (
          <Card className="col-span-12 md:col-span-6 bg-[var(--bg-card)] border-black/10">
            <CardContent className="p-7">
              {/* Повторяет форму реального контента */}
              <Skeleton className="h-4 w-16 mb-4 rounded-md" />         {/* чип */}
              <Skeleton className="h-7 w-48 mb-4 rounded-md" />         {/* имя */}
              <div className="flex gap-10">
                <Skeleton className="h-4 w-16 rounded-md" />
                <Skeleton className="h-4 w-24 rounded-md" />
                <Skeleton className="h-4 w-20 rounded-md" />
              </div>
            </CardContent>
          </Card>
        ) : isError || !user ? (
          <Card className="col-span-12 md:col-span-6 bg-[var(--bg-card)] border-black/10">
            <CardContent className="p-7">
              <p className="text-[var(--red)]">Ошибка загрузки</p>
            </CardContent>
          </Card>
        ) : (
          <UserCard user={user} />
        )}

        <Card className="col-span-6 md:col-span-2 ...">
          <CardContent className="p-6 flex flex-col items-center justify-center text-center">
            <Chip className="border-[var(--gold)] text-[var(--gold)] bg-[var(--gold-light)]">
              Рейтинг
            </Chip>
            <span className="font-serif text-[3.5rem] font-black text-[var(--navy)] leading-none">
              {avgGrade ?? "—"}
            </span>
            <span className="text-[10px] font-bold tracking-[0.18em] uppercase text-[var(--ink-faint)] mt-1 mb-4">
              средний балл
            </span>

            <Progress
              value={avgGrade ? (avgGrade / 5) * 100 : 0}
              className="h-1 bg-[var(--gold-light)] [&>div]:bg-[var(--gold)] w-full"
            />
          </CardContent>
        </Card>

        <Card className="col-span-6 md:col-span-4 bg-[var(--bg-card)] border-black/10
                         hover:-translate-y-1 hover:shadow-xl transition-all duration-200">
          <CardContent className="pt-6">
            <Chip className="border-[var(--brown)] text-[var(--brown)] bg-[var(--brown-light)]">
              классный руководитель
            </Chip>


            <div className="flex items-center gap-3">
              <Avatar className="bg-[var(--navy-light)] h-12 w-12">
                <AvatarFallback className="bg-[var(--navy-light)] text-[var(--navy)] font-serif font-bold">
                  {user?.schoolClassTeacher?.firstName[0]}{user?.schoolClassTeacher?.lastName[0]}
                </AvatarFallback>
              </Avatar>
              <div>
                <p className="font-bold text-[var(--ink)]">{user?.schoolClassTeacher?.firstName} {user?.schoolClassTeacher?.lastName}</p>
              </div>
            </div>

            <div className="mt-4 pt-4 border-t border-black/10">
              <p className="text-[9px] font-bold tracking-[0.15em] uppercase text-[var(--ink-faint)] mb-1">
                Контакты
              </p>
              <p className="text-sm font-semibold text-[var(--ink)]">{user?.schoolClassTeacher?.email} {user?.schoolClassTeacher?.phoneNumber}</p>
            </div>
          </CardContent>
        </Card>

        <Card className="col-span-12 md:col-span-4 bg-[var(--bg-card)] border-black/10
                         hover:-translate-y-1 hover:shadow-xl transition-all duration-200">
          <CardContent className="pt-6">
            <Chip className="border-[var(--red)] text-[var(--red)] bg-[var(--red-light)]">
              Сегодня
            </Chip>

            <div className="divide-y divide-black/[0.07]">
              {todaySchedule?.map((l) => (
                // key — обязателен при .map() в React
                <div key={l.lessonNumber} className="flex items-center gap-4 py-3">
                  {/* Большое полупрозрачное число — декоративный элемент */}
                  <span className="font-serif text-[1.6rem] font-bold text-[var(--red-light)] leading-none min-w-[32px]">
                    {l.lessonNumber}
                  </span>
                  <div>
                    <p className="font-bold text-sm text-[var(--ink)]">{l.subjectName}</p>
                    <p className="text-[10px] font-semibold text-[var(--ink-faint)] mt-0.5">{l.classRoom}</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* ── Оценки ── col-span-4 */}
        <Card className="col-span-12 md:col-span-4 bg-[var(--bg-card)] border-black/10
                         hover:-translate-y-1 hover:shadow-xl transition-all duration-200">
          <CardContent className="pt-6">
            <Chip className="border-[var(--green)] text-[var(--green)] bg-[var(--green-light)]">
              Оценки за сегодня
            </Chip>

            <div className="divide-y divide-black/[0.07]">
              {todayGrades?.map((g) => (
                <div key={g.subjectName} className="flex items-center justify-between py-3">
                  <span className="font-semibold text-sm">{g.subjectName}</span>
                  <GradeBadge grade={g.value} />
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* ── ДZ на завтра ── col-span-4 */}
        <Card className="col-span-12 md:col-span-4 bg-[var(--bg-card2)] border-black/10
                         hover:-translate-y-1 hover:shadow-xl transition-all duration-200">
          <CardContent className="pt-6">
            <Chip className="border-[var(--brown)] text-[var(--brown)] bg-[var(--brown-light)]">
              ДЗ на завтра
            </Chip>

            <div className="flex flex-col gap-2 mt-1">
              {homework.map((hw) => (
                <div
                  key={hw.subject}
                  className="flex items-center gap-3 px-3 py-2 rounded-xl bg-white/50 border border-black/[0.07]"
                >
                  {/* Цветная точка — w-2 h-2 rounded-full */}
                  <span className={`w-2 h-2 rounded-full flex-shrink-0 ${hw.color}`} />
                  <span className="text-[13px] font-semibold text-[var(--ink)]">{hw.text}</span>
                  {/* ml-auto — прижимает элемент вправо (как ms-auto в Bootstrap) */}
                  <span className="ml-auto text-[10px] font-bold text-[var(--ink-faint)] uppercase tracking-[0.08em]">
                    {hw.subject}
                  </span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* ── Расписание на неделю ── col-span-12 (на всю ширину) */}
        <Card className="col-span-12 bg-[var(--bg-card)] border-black/10
                         hover:-translate-y-1 hover:shadow-xl transition-all duration-200">
          <CardContent className="pt-6">
            <Chip className="border-[var(--brown)] text-[var(--brown)] bg-[var(--brown-light)]">
              Расписание на неделю
            </Chip>

            {/*
              grid-cols-3 md:grid-cols-5 — адаптив:
              на мобиле 3 колонки, на md+ (768px) — 5 колонок
            */}
            <div className="grid grid-cols-3 md:grid-cols-5 gap-3">
              {weekSchedule.map((d) => (
                <div
                  key={d.day}
                  className={`
                    px-3 py-3 rounded-2xl border transition-colors
                    ${d.today
                      ? "bg-[var(--red-light)] border-[var(--red)]/25"
                      : "bg-white/35 border-black/[0.07] hover:bg-white/60"
                    }
                  `}
                >
                  {/* Название дня */}
                  <p className={`
                    text-[10px] font-extrabold tracking-[0.15em] uppercase mb-2 pb-1.5 border-b
                    ${d.today ? "text-[var(--red)] border-[var(--red)]/20" : "text-[var(--ink-dim)] border-black/[0.07]"}
                  `}>
                    {d.today ? `${d.day} — сегодня` : d.day}
                  </p>

                  {/* Предметы */}
                  <div className="flex flex-col gap-1">
                    {d.lessons.map((lesson) => (
                      <div key={lesson} className="flex items-center gap-1.5">
                        {/* Маленькая точка через pseudo не делаем — просто span */}
                        <span className={`w-1 h-1 rounded-full flex-shrink-0 ${d.today ? "bg-[var(--red)]" : "bg-[var(--ink-faint)]"}`} />
                        <span className={`text-[11px] font-semibold ${d.today ? "text-[var(--navy)]" : "text-[var(--ink)]"}`}>
                          {lesson}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

      </main>
    </div>
  );
}

export default Home