import { useMemo, useState } from "react";
import {
    GraduationCap,
    Plus,
    Loader2,
    BookOpen,
    Search,
    CalendarClock,
    AlertTriangle,
} from "lucide-react";
import { useDeleteClass, useUpdateClass, useGetAllClassesByAcademicYear } from "@/hooks/use-school-class";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import AdminNavbar from "@/components/layout/navbars/AdminNavbar";
import ClassCard from "@/components/admin/school-class-page/class-card";
import CreateClassForm from "@/components/admin/school-class-page/create-class-form";
import ClassDetailModal from "@/components/admin/school-class-page/class-detail-modal";
import { useGetAcademicYears } from "@/hooks/use-academic-year";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";

export default function SchoolClassPage() {
    const [search, setSearch] = useState("");
    const [selectedClassId, setSelectedClassId] = useState<number | null>(null);

    // Academic year selection
    const { data: academicYears } = useGetAcademicYears();
    const [selectedAcademicYearId, setSelectedAcademicYearId] = useState<string>("");

    const defaultAcademicYearId = useMemo(() => {
        if (!academicYears?.length) return "";
        return academicYears[0].id.toString();
    }, [academicYears]);

    const resolvedAcademicYearId = selectedAcademicYearId || defaultAcademicYearId;

    const currentAcademicYear = useMemo(() => {
        return academicYears?.find(year => year.id.toString() === resolvedAcademicYearId);
    }, [academicYears, resolvedAcademicYearId]);

    const isYearClosed = currentAcademicYear ? !currentAcademicYear.isActive : false;

    // Classes data
    const { data: classes = [], isLoading } = useGetAllClassesByAcademicYear(parseInt(resolvedAcademicYearId, 10));
    
    const filteredClasses = useMemo(() => {
        return classes.filter((schoolClass) =>
            schoolClass.name.toLowerCase().includes(search.toLowerCase())
        );
    }, [classes, search]);

    const selectedClass = classes.find((c) => c.id === selectedClassId);

    // Mutations
    const deleteMutation = useDeleteClass();
    const updateMutation = useUpdateClass();

    // Handlers
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

            {/* Header with controls */}
            <div className="max-w-[1400px] mx-auto mb-6">
                <div className="glass-card rounded-[24px] p-5 flex flex-col lg:flex-row lg:items-center justify-between gap-5 border-none shadow-lg backdrop-blur-md">
                    <div className="flex items-center gap-4">
                        <div className="hidden sm:flex w-12 h-12 rounded-[18px] bg-[var(--red-light)]/60 items-center justify-center ring-1 ring-[var(--red)]/10">
                            <GraduationCap className="w-6 h-6 text-[var(--red)]" />
                        </div>
                        <div>
                            <h1 className="font-serif font-black text-2xl lg:text-3xl text-[var(--navy)] tracking-tight">
                                Классы
                            </h1>
                            <p className="text-sm text-black/40 mt-0.5">
                                {isLoading
                                    ? "Загрузка..."
                                    : `${classes.length} класс${classes.length === 1
                                        ? ""
                                        : classes.length < 5
                                            ? "а"
                                            : "ов"
                                    }`}
                            </p>
                        </div>
                    </div>

                    <div className="flex flex-col lg:flex-row items-start lg:items-center gap-4 w-full lg:w-auto">
                        <Select
                            value={resolvedAcademicYearId}
                            onValueChange={setSelectedAcademicYearId}
                        >
                            <SelectTrigger className="glass-pill h-10 px-5 text-[12px] font-bold rounded-2xl text-[var(--navy)] border-0 shadow-sm gap-2 min-w-[180px]">
                                <CalendarClock className="w-4 h-4 text-[var(--red)]" />
                                <SelectValue placeholder="Выберите год" />
                            </SelectTrigger>
                            <SelectContent className="rounded-2xl border-none shadow-2xl bg-white/95 backdrop-blur-xl max-h-[350px]">
                                {academicYears?.map((academicYear) => (
                                    <SelectItem key={academicYear.id} value={academicYear.id.toString()} className="font-bold text-[13px] py-3 rounded-xl cursor-pointer">
                                        {academicYear.name} {!academicYear.isActive && "(Архив)"}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>

                        <div className="relative w-full lg:w-[280px]">
                            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-black/30" />
                            <Input
                                placeholder="Поиск класса..."
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                className="pl-10 h-11 bg-white/40 border-black/10 rounded-2xl text-sm font-semibold placeholder:font-normal focus-visible:ring-[var(--red)]"
                            />
                        </div>
                    </div>
                </div>
            </div>

            {/* Closed year alert */}
            {isYearClosed && (
                <div className="max-w-[1400px] mx-auto mb-6 animate-in fade-in slide-in-from-top-2 duration-300">
                    <Alert variant="destructive" className="rounded-[24px] bg-gradient-to-r from-red-50 to-red-50/50 border-red-200/80 shadow-lg backdrop-blur-sm">
                        <div className="flex items-start gap-4">
                            <div className="flex-shrink-0 mt-0.5 w-10 h-10 rounded-[14px] bg-red-100/60 flex items-center justify-center">
                                <AlertTriangle className="h-5 w-5 text-yellow-600" />
                            </div>
                            <div className="flex-1">
                                <AlertTitle className="font-serif font-black tracking-tight text-base text-yellow-900 mb-1">
                                    Учебный год <span className="font-bold text-yellow-900">({currentAcademicYear?.name})</span> закрыт
                                </AlertTitle>
                                <AlertDescription className="text-sm text-yellow-800/85 font-medium leading-relaxed">
                                    Операции удаления и редактирования классов запрещены
                                </AlertDescription>
                            </div>
                        </div>
                    </Alert>
                </div>
            )}

            {/* Main content */}
            <div className="max-w-[1400px] mx-auto grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Classes list */}
                <div className="lg:col-span-2">
                    <div className="glass-card rounded-[32px] p-6 backdrop-blur-md min-h-[500px] flex flex-col">
                        <h2 className="font-serif font-black text-lg text-[var(--navy)] tracking-tight flex items-center gap-2 mb-5">
                            <GraduationCap className="w-5 h-5 text-[var(--red)]" />
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

                {/* Create class form */}
                <div className="lg:col-span-1">
                    <div className="sticky top-6">
                        <div className="glass-card rounded-[32px] p-6 backdrop-blur-md">
                            <h2 className="text-base font-black text-[var(--navy)] flex items-center gap-2 mb-5">
                                <Plus className="w-4 h-4 text-[var(--red)]" />
                                Создать класс
                            </h2>
                            {isYearClosed ? (
                                <div className="text-center py-8 px-4 border border-dashed border-black/10 rounded-2xl bg-black/[0.02]">
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

            {/* Class details modal */}
            <ClassDetailModal
                classId={selectedClassId}
                className={selectedClass?.name}
                onClose={() => setSelectedClassId(null)}
            />
        </div>
    );
}