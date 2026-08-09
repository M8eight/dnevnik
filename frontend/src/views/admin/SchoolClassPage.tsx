import { useMemo, useState } from "react";
import {
    GraduationCap,
    Plus,
    Loader2,
    BookOpen,
    Search,
} from "lucide-react";
import { useDeleteClass, useUpdateClass, useGetAllClassesByAcademicYear } from "@/hooks/use-school-class";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import AdminNavbar from "@/components/layout/navbars/AdminNavbar";
import ClassCard from "@/components/admin/school-class-page/class-card";
import CreateClassForm from "@/components/admin/school-class-page/create-class-form";
import ClassDetailModal from "@/components/admin/school-class-page/class-detail-modal";
import { useAcademicYearSelection } from "@/hooks/use-academic-year-selection";
import PageHeader from "@/components/admin/page-header";
import AcademicYearSelect from "@/components/admin/academic-year-select";
import ClosedYearAlert from "@/components/admin/closed-year-alert";

export default function SchoolClassPage() {
    const [search, setSearch] = useState("");
    const [selectedClassId, setSelectedClassId] = useState<number | null>(null);

    const {
        resolvedAcademicYearId,
        setSelectedAcademicYearId,
        currentAcademicYear,
        isYearClosed,
    } = useAcademicYearSelection();

    const { data: classes = [], isLoading } = useGetAllClassesByAcademicYear(parseInt(resolvedAcademicYearId, 10));
    
    const filteredClasses = useMemo(() => {
        return classes.filter((schoolClass) =>
            schoolClass.name.toLowerCase().includes(search.toLowerCase())
        );
    }, [classes, search]);

    const selectedClass = classes.find((c) => c.id === selectedClassId);

    const deleteMutation = useDeleteClass();
    const updateMutation = useUpdateClass();

    const handleSelect = (id: number) => {
        setSelectedClassId((prev) => (prev === id ? null : id));
    };

    const handleDelete = (id: number) => {
        if (selectedClassId === id) setSelectedClassId(null);
        deleteMutation.mutate(id);
    };

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
            
            <AdminNavbar />

            <PageHeader
                icon={GraduationCap}
                title="Классы"
                subtitle={
                    isLoading
                        ? "Загрузка..."
                        : `${classes.length} класс${classes.length === 1
                            ? ""
                            : classes.length < 5
                                ? "а"
                                : "ов"
                        }`
                }
            >
                <AcademicYearSelect
                    value={resolvedAcademicYearId}
                    onChange={setSelectedAcademicYearId}
                />

                <div className="relative w-full lg:w-70">
                    <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-black/30" />
                    <Input
                        placeholder="Поиск класса..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="pl-10 h-11 bg-white/40 border-black/10 rounded-2xl text-sm font-semibold placeholder:font-normal focus-visible:ring-(--red)"
                    />
                </div>
            </PageHeader>

            {isYearClosed && (
                <ClosedYearAlert
                    yearName={currentAcademicYear?.name}
                    description="Операции удаления и редактирования классов запрещены"
                />
            )}

            <div className="max-w-350 mx-auto grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Classes list */}
                <div className="lg:col-span-2">
                    <div className="glass-card rounded-[32px] p-6 backdrop-blur-md min-h-125 flex flex-col">
                        <h2 className="font-serif font-black text-lg text-(--navy) tracking-tight flex items-center gap-2 mb-5">
                            <GraduationCap className="w-5 h-5 text-(--red)" />
                            Все классы
                            {selectedClassId && (
                                <span className="ml-auto text-xs font-bold text-black/30 bg-black/5 rounded-full px-3 py-1">
                                    Выбран: {selectedClass?.name}
                                </span>
                            )}
                        </h2>

                        <ScrollArea className="flex-1 pr-2">
                            {isLoading ? (
                                <div className="flex flex-col items-center justify-center py-20 text-black/30">
                                    <Loader2 className="w-8 h-8 animate-spin" />
                                </div>
                            ) : filteredClasses.length > 0 ? (
                                <div className="flex flex-col gap-3">
                                    {filteredClasses.map((schoolClass, idx) => (
                                        <ClassCard
                                            key={schoolClass.id}
                                            schoolClass={schoolClass}
                                            index={idx}
                                            isSelected={selectedClassId === schoolClass.id}
                                            onSelect={handleSelect}
                                            onDelete={isYearClosed ? undefined : handleDelete}
                                            onUpdate={isYearClosed ? undefined : (id, data) =>
                                                updateMutation.mutate({ id, data })
                                            }
                                            isDeleting={
                                                deleteMutation.isPending &&
                                                deleteMutation.variables === schoolClass.id
                                            }
                                            isUpdating={
                                                updateMutation.isPending &&
                                                updateMutation.variables?.id === schoolClass.id
                                            }
                                        />
                                    ))}
                                </div>
                            ) : (
                                <div className="flex flex-col items-center justify-center py-20 text-black/25">
                                    <BookOpen className="w-12 h-12 mb-3 opacity-30" />
                                    <p className="font-bold text-sm">
                                        {search ? "По запросу ничего не найдено" : "Классы не найдены"}
                                    </p>
                                </div>
                            )}
                        </ScrollArea>
                    </div>
                </div>

                <div className="lg:col-span-1">
                    <div className="sticky top-6">
                        <div className="glass-card rounded-[32px] p-6 backdrop-blur-md">
                            <h2 className="text-base font-black text-(--navy) flex items-center gap-2 mb-5">
                                <Plus className="w-4 h-4 text-(--red)" />
                                Создать класс
                            </h2>
                            {isYearClosed ? (
                                <div className="text-center py-8 px-4 border border-dashed border-black/10 rounded-2xl bg-black/2">
                                    <p className="text-sm font-semibold text-black/40">
                                        Создание классов в закрытом году недоступно
                                    </p>
                                </div>
                            ) : (
                                <CreateClassForm academicYearId={parseInt(resolvedAcademicYearId, 10)} />
                            )}
                        </div>
                    </div>
                </div>
            </div>

            <ClassDetailModal
                classId={selectedClassId}
                className={selectedClass?.name}
                onClose={() => setSelectedClassId(null)}
            />
            
        </div>
    );
}