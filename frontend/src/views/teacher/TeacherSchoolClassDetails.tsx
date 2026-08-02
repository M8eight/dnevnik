import TeacherNavbar from "@/components/layout/navbars/TeacherNavbar";
import { LeftColumnSkeleton, RightColumnSkeleton, SchoolClassCard, StudentsSection, TeacherCard } from "@/components/layout/schoolClassDetails/school-class-details-layout";
import { useGetClassDetails } from "@/hooks/use-school-class";
import {
    GraduationCap,
    ChevronLeft,
} from "lucide-react";
import { useNavigate, useParams } from "react-router-dom";


export default function ClassDetailsPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const classId = id ? Number(id) : null;

    const { data, isLoading } = useGetClassDetails(classId);

    if (!id) {
        return <div>Неверный id класса</div>;
    }

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
            <TeacherNavbar />

            <div className="max-w-350 mx-auto mb-6">
                <div className="glass-card rounded-[24px] p-4 flex items-center justify-between border-none shadow-lg backdrop-blur-md">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-[14px] bg-(--red-light)/60 flex items-center justify-center ring-1 ring-(--red)/10">
                            <GraduationCap className="w-5 h-5 text-(--red)" />
                        </div>
                        <div>
                            <h1 className="font-serif font-black text-xl text-(--navy) tracking-tight">Класс</h1>
                            <p className="text-xs text-black/40 mt-0.5">Состав и руководство</p>
                        </div>
                    </div>
                    <button
                        onClick={() => navigate(-1)}
                        className="glass-pill flex items-center gap-2 px-4 py-2 rounded-2xl text-sm font-bold text-(--navy) border-none shadow-sm"
                    >
                        <ChevronLeft className="w-4 h-4 text-(--red)" />
                        Назад
                    </button>
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
                            <StudentsSection students={data.students} />
                        </>
                    )}
                </div>
            </div>

        </div>
    );
}