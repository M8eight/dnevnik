import { useState } from "react";
import {
    GraduationCap,
    Plus,
    Loader2,
    BookOpen,
    Search,
} from "lucide-react";
import { useGetAllClasses, useDeleteClass, useUpdateClass } from "@/hooks/use-school-class";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import AdminNavbar from "@/templates/navbars/AdminNavbar";
import ClassCard from "@/components/admin/school-class-page/class-card";
import CreateClassForm from "@/components/admin/school-class-page/create-class-form";
import ClassDetailModal from "@/components/admin/school-class-page/class-detail-modal";

export default function SchoolClassPage() {
    const [search, setSearch] = useState("");
    const [page] = useState(0);
    const [selectedClassId, setSelectedClassId] = useState<number | null>(null);

    const { data: pageData, isLoading } = useGetAllClasses(page, 20);
    const classes = pageData?.content ?? [];

    const deleteMutation = useDeleteClass();
    const updateMutation = useUpdateClass();

    const filtered = classes.filter(
        (c) =>
            c.name?.toLowerCase().includes(search.toLowerCase()) ||
            c.year?.toLowerCase().includes(search.toLowerCase())
    );

    const selectedClass = classes.find((c) => c.id === selectedClassId);

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

            {/* ── Controls bar ── */}
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
                                    : `${classes.length} класс${
                                          classes.length === 1
                                              ? ""
                                              : classes.length < 5
                                              ? "а"
                                              : "ов"
                                      }`}
                            </p>
                        </div>
                    </div>

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

            {/* ── Main grid ── */}
            <div className="max-w-[1400px] mx-auto grid grid-cols-1 lg:grid-cols-3 gap-6">

                {/* List */}
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
                            ) : filtered.length > 0 ? (
                                <div className="flex flex-col gap-3">
                                    {filtered.map((schoolClass, idx) => (
                                        <ClassCard
                                            key={schoolClass.id}
                                            schoolClass={schoolClass}
                                            index={idx}
                                            isSelected={selectedClassId === schoolClass.id}
                                            onSelect={handleSelect}
                                            onDelete={handleDelete}
                                            onUpdate={(id, data) =>
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
                                    <p className="font-bold text-sm">Классы не найдены</p>
                                </div>
                            )}
                        </ScrollArea>

                    </div>
                </div>

                {/* Form */}
                <div className="lg:col-span-1">
                    <div className="sticky top-6">
                        <div className="glass-card rounded-[32px] p-6 backdrop-blur-md">
                            <h2 className="text-base font-black text-[var(--navy)] flex items-center gap-2 mb-5">
                                <Plus className="w-4 h-4 text-[var(--red)]" />
                                Создать класс
                            </h2>
                            <CreateClassForm />
                        </div>
                    </div>
                </div>

            </div>

            {/* ── Detail panel (slide-over) ── */}
            <ClassDetailModal
                classId={selectedClassId}
                className={selectedClass?.name}
                onClose={() => setSelectedClassId(null)}
            />
        </div>
    );
}