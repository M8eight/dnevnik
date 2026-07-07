import { Skeleton } from "@/components/ui/skeleton";
import { useStudentFullDetails } from "@/hooks/use-student";
import { CurrentDate } from "@/helpers/student-helpers";
import { UserCard, RatingCard, TeacherCard, TodayScheduleCard, TodayGradesCard, HomeworkCard, WeekScheduleCard } from "@/components/student/home/home-cards";
import StudentNavbar from "@/components/layout/navbars/StudentNavbar";
import { useHomeAggregation } from "@/hooks/bff/use-student-bff";


function Home() {
  const studentId = 27;
  const today = new Date();
  
  // const username = useSelector(selectUsername);
  const todayDateStr = today.toISOString().split("T")[0];
  const currentDayOfWeek = new Intl.DateTimeFormat("en-US", { weekday: "long" })
    .format(today)
    .toUpperCase();

  const { data: user, isLoading, isError } = useStudentFullDetails(studentId);
  const { data: academicAggregation} = useHomeAggregation(todayDateStr);

  return (
    <div className="relative z-10 min-h-screen px-6 md:px-10 pt-2 pb-14">
      
      <StudentNavbar />

      {/* ── Header ── */}
      <header className="flex items-end justify-between mb-10 pb-6 border-b border-black/8 max-w-6xl mx-auto anim-in">
        <div>
          <p className="text-[10px] font-extrabold tracking-[0.25em] text-(--red) uppercase mb-2 flex items-center gap-2">
            <span className="inline-block w-4 h-0.5 bg-(--red) rounded-full" />
            Академический год 25/26
          </p>
          <h1 className="font-serif font-black text-[clamp(2rem,4vw,3rem)] text-(--navy) leading-[0.95]">
            Учебный{" "}
            <em className="not-italic relative">
              <span className="relative z-10 text-(--red)">дневник</span>
              <span className="absolute bottom-0 left-0 right-0 h-1.25 rounded-full opacity-15 bg-(--red)" />
            </em>
          </h1>
        </div>
        <CurrentDate />
      </header>

      {/* ── Grid ── */}
      <main className="grid grid-cols-12 gap-4 max-w-6xl mx-auto">

        {/* User card */}
        {isLoading ? (
          <div className="col-span-12 md:col-span-6 glass-card rounded-[22px] p-7 anim-in anim-delay-1">
            <Skeleton className="h-5 w-16 mb-4 rounded-full" />
            <Skeleton className="h-7 w-48 mb-5 rounded-lg" />
            <div className="flex gap-8">
              <Skeleton className="h-4 w-14 rounded" />
              <Skeleton className="h-4 w-20 rounded" />
              <Skeleton className="h-4 w-18 rounded" />
            </div>
          </div>
        ) : isError || !user ? (
          <div className="col-span-12 md:col-span-6 glass-card rounded-[22px] p-7 anim-in anim-delay-1">
            <p className="text-(--red) text-sm font-semibold">Ошибка загрузки профиля</p>
          </div>
        ) : (
          <UserCard user={user} />
        )}

        <RatingCard avgGrade={academicAggregation?.todayAverage} />
        <TeacherCard user={user} />

        <TodayScheduleCard schedule={academicAggregation?.todaySchedule} />
        <TodayGradesCard grades={academicAggregation?.todayGrades} />
        <HomeworkCard homework={academicAggregation?.todayHomework} />

        <WeekScheduleCard fullSchedule={academicAggregation?.weekSchedule} currentDayOfWeek={currentDayOfWeek} />
      </main>
    </div>
  );
}

export default Home;