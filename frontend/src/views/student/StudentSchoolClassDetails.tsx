import StudentNavbar from "@/components/layout/navbars/StudentNavbar";
import { LeftColumnSkeleton, RightColumnSkeleton, SchoolClassCard, StudentsSection, TeacherCard } from "@/components/layout/schoolClassDetails/school-class-details-layout";
import { useGetDetailsByStudentId } from "@/hooks/use-school-class";
import {
    GraduationCap,
} from "lucide-react";


export default function StudentSchoolClassDetails() {
    const { data, isLoading } = useGetDetailsByStudentId();

    return (
        <div className="relative z-10 min-h-screen px-6 md:px-10 pt-2 pb-14">

            <StudentNavbar />

            <header className="flex items-end justify-between mb-10 pb-6 border-b border-black/8 max-w-7xl mx-auto anim-in">
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
            </header>

            <div className="max-w-7xl mx-auto mb-6">
                <div className="glass-card rounded-[24px] p-4 flex items-center justify-between border-none shadow-lg backdrop-blur-md anim-in">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-[14px] bg-(--red-light)/60 flex items-center justify-center ring-1 ring-(--red)/10">
                            <GraduationCap className="w-5 h-5 text-(--red)" />
                        </div>
                        <div>
                            <h1 className="font-serif font-black text-xl text-(--navy) tracking-tight">Мой класс</h1>
                            <p className="text-xs text-black/40 mt-0.5">Состав и классный руководитель</p>
                        </div>
                    </div>
                </div>
            </div>

            <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-3 gap-4 ">
                {isLoading || !data ? (
                    <LeftColumnSkeleton />
                ) : (
                    <SchoolClassCard data={data} />
                )}

                <div className="lg:col-span-2 flex flex-col gap-6">
                    {isLoading || !data ? (
                        <RightColumnSkeleton />
                    ) : (
                        <>
                            <TeacherCard teacher={data.teacher} classTeacherId={data.classTeacherId} />
                            <StudentsSection students={data.students} withLinks={false} />
                        </>
                    )}
                </div>
            </div>

        </div>
    );
}