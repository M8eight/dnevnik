import { useDispatch, useSelector } from "react-redux";
import { GraduationCap, ChevronRight, Check } from "lucide-react";
import { selectSelectedStudentId, setSelectedStudent } from "@/store/slices/selectedStudentSlice";
import ParentNavbar from "@/components/layout/navbars/ParentNavbar";
import { useStudentsByParentId } from "@/hooks/use-student";

export default function ParentStudentPicker() {
    const { data } = useStudentsByParentId({ enabled: true });
    const students = data || [];
    const selectedStudentId = useSelector(selectSelectedStudentId)
    const dispatch = useDispatch();

    return (
        <div className="relative z-10 min-h-screen px-6 md:px-10 pt-2 pb-14">
            <ParentNavbar />

            <div className="px-0 pt-10 pb-14 max-w-3xl mx-auto">
                <header className="mb-10 anim-in">
                    <p className="text-[10px] font-extrabold tracking-[0.25em] text-(--red) uppercase mb-2 flex items-center gap-2">
                        <span className="inline-block w-4 h-0.5 bg-(--red) rounded-full" />
                        Чьи данные открыть
                    </p>
                    <h1 className="font-serif font-black text-[clamp(2rem,4vw,3rem)] text-(--navy) leading-[0.95]">
                        Выбор{" "}
                        <em className="not-italic relative">
                            <span className="relative z-10 text-(--red)">ученика</span>
                            <span className="absolute bottom-0 left-0 right-0 h-1.25 rounded-full opacity-15 bg-(--red)" />
                        </em>
                    </h1>
                </header>

                <div className="grid gap-3 anim-in">
                    {students.map((student) => (
                        <button
                            key={student.id}
                            onClick={() => dispatch(setSelectedStudent(student.id))}
                            className="glass-card rounded-[24px] p-4 flex items-center justify-between border-none shadow-lg backdrop-blur-md hover:shadow-xl transition-shadow text-left"
                        >
                            <div className="flex items-center gap-3">
                                <div className="w-10 h-10 rounded-[14px] bg-(--red-light)/60 flex items-center justify-center ring-1 ring-(--red)/10">
                                    <GraduationCap className="w-5 h-5 text-(--red)" />
                                </div>
                                <h2 className="font-serif font-black text-lg text-(--navy) tracking-tight">
                                    {student.firstName} {student.lastName}
                                </h2>
                                {student.id === selectedStudentId && 
                                <div className="glass-card p-2  flex items-center justify-center shadow-lg backdrop-blur-md rounded-[12px]" > 
                                    <Check className="w-4 h-4  text-(--red) " />
                                </div> }

                            </div>
                            <ChevronRight className="w-5 h-5 text-black/20" />
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );
}