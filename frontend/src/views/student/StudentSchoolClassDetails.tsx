import StudentNavbar from "@/components/layout/navbars/StudentNavbar";
import { LeftColumnSkeleton, RightColumnSkeleton, SchoolClassCard, StudentsSection, TeacherCard } from "@/components/layout/schoolClassDetails/school-class-details-layout";
import { useGetDetailsByStudentId } from "@/hooks/use-school-class";
import {
    GraduationCap,
} from "lucide-react";


export default function StudentSchoolClassDetails() {
    const { data, isLoading } = useGetDetailsByStudentId();

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
            <StudentNavbar />

            <div className="max-w-350 mx-auto mb-6">
                <div className="glass-card rounded-[24px] p-4 flex items-center justify-between border-none shadow-lg backdrop-blur-md">
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

            <div className="max-w-350 mx-auto grid grid-cols-1 lg:grid-cols-3 gap-6">
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