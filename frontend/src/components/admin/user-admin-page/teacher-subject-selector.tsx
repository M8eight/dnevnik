import { useMemo, useState, useRef, useEffect } from "react";
import { Loader2, X, Plus, BookOpen } from "lucide-react";
import { cn } from "@/lib/utils";
import { useGetTeacherSubjects, useCreateTeacherSubject, useDeleteTeacherSubject } from "@/hooks/use-teacher-subject";
import { useGetAllSubjects } from "@/hooks/use-subject";

interface Props {
    teacherId: number;
    disabled?: boolean;
}

export default function TeacherSubjectsField({ teacherId, disabled }: Props) {
    const [pickerOpen, setPickerOpen] = useState(false);
    const [pendingSubjectId, setPendingSubjectId] = useState<number | null>(null);
    const pickerRef = useRef<HTMLDivElement>(null);

    const { data: allLinks, isLoading: isLinksLoading } = useGetTeacherSubjects();
    const { data: subjectsPage, isLoading: isSubjectsLoading } = useGetAllSubjects(0, 200);

    const { mutate: createLink, isPending: isCreating } = useCreateTeacherSubject();
    const { mutate: deleteLink, isPending: isDeleting } = useDeleteTeacherSubject();

    const teacherLinks = useMemo(
        () => (allLinks ?? []).filter((link) => link.teacher.id === teacherId),
        [allLinks, teacherId]
    );

    const assignedSubjectIds = useMemo(
        () => new Set(teacherLinks.map((link) => link.subject.id)),
        [teacherLinks]
    );

    const availableSubjects = useMemo(
        () => (subjectsPage?.content ?? []).filter((s) => !assignedSubjectIds.has(s.id)),
        [subjectsPage, assignedSubjectIds]
    );

    useEffect(() => {
        const handleClickOutside = (e: MouseEvent) => {
            if (pickerRef.current && !pickerRef.current.contains(e.target as Node)) {
                setPickerOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleAdd = (subjectId: number) => {
        setPendingSubjectId(subjectId);
        createLink(
            { teacherId, subjectId },
            { onSettled: () => setPendingSubjectId(null) }
        );
        setPickerOpen(false);
    };

    const handleRemove = (subjectId: number) => {
        setPendingSubjectId(subjectId);
        deleteLink(
            { teacherId, subjectId },
            { onSettled: () => setPendingSubjectId(null) }
        );
    };

    const isLoading = isLinksLoading || isSubjectsLoading;
    const isBusy = isCreating || isDeleting;
    const isLocked = disabled || isLoading;

    return (
        <div className="space-y-1.5">
            <label className="text-xs font-bold tracking-widest uppercase text-black/30">
                Предметы
            </label>

            <div className="flex flex-wrap items-center gap-1.5 min-h-11 p-2 bg-white/40 border border-black/10 rounded-2xl">
                {isLoading ? (
                    <div className="flex items-center gap-2 px-2 text-black/30 text-xs font-semibold">
                        <Loader2 className="w-3.5 h-3.5 animate-spin" />
                        Загрузка...
                    </div>
                ) : (
                    <>
                        {teacherLinks.length === 0 && !pickerOpen && (
                            <span className="px-2 text-xs font-semibold text-black/30">
                                Предметы не назначены
                            </span>
                        )}

                        {teacherLinks.map((link) => {
                            const isRowBusy = isBusy && pendingSubjectId === link.subject.id;
                            return (
                                <span
                                    key={link.subject.id}
                                    className={cn(
                                        "inline-flex items-center gap-1.5 h-8 pl-3 pr-1.5 rounded-xl bg-(--navy)/8 text-(--navy) text-xs font-bold transition-opacity",
                                        isRowBusy && "opacity-50"
                                    )}
                                >
                                    {link.subject.name}
                                    <button
                                        type="button"
                                        disabled={isLocked || isRowBusy}
                                        onClick={() => handleRemove(link.subject.id)}
                                        className="w-5 h-5 flex items-center justify-center rounded-lg text-(--navy)/40 hover:text-(--red) hover:bg-white/60 transition-all disabled:pointer-events-none"
                                        aria-label={`Убрать предмет ${link.subject.name}`}
                                    >
                                        {isRowBusy ? (
                                            <Loader2 className="w-3 h-3 animate-spin" />
                                        ) : (
                                            <X className="w-3 h-3" />
                                        )}
                                    </button>
                                </span>
                            );
                        })}

                        {!isLocked && (
                            <div className="relative" ref={pickerRef}>
                                <button
                                    type="button"
                                    onClick={() => setPickerOpen((v) => !v)}
                                    disabled={availableSubjects.length === 0}
                                    className={cn(
                                        "inline-flex items-center gap-1 h-8 px-3 rounded-xl text-xs font-bold uppercase tracking-wide transition-all",
                                        "border border-dashed border-black/15 text-black/40 hover:text-(--navy) hover:border-(--navy)/30 hover:bg-(--navy)/5",
                                        "disabled:opacity-30 disabled:pointer-events-none"
                                    )}
                                >
                                    <Plus className="w-3.5 h-3.5" />
                                    Добавить
                                </button>

                                {pickerOpen && (
                                    <div className="absolute z-10 top-full left-0 mt-1.5 w-56 max-h-56 overflow-y-auto rounded-2xl bg-white shadow-2xl border border-black/10 p-1.5">
                                        {availableSubjects.length === 0 ? (
                                            <p className="px-3 py-2 text-xs font-semibold text-black/30">
                                                Все предметы уже назначены
                                            </p>
                                        ) : (
                                            availableSubjects.map((subject) => (
                                                <button
                                                    key={subject.id}
                                                    type="button"
                                                    onClick={() => handleAdd(subject.id)}
                                                    className="w-full flex items-center gap-2 px-3 py-2 rounded-xl text-left text-sm font-semibold text-black/70 hover:bg-(--navy)/8 hover:text-(--navy) transition-all"
                                                >
                                                    <BookOpen className="w-3.5 h-3.5 text-black/30" />
                                                    {subject.name}
                                                </button>
                                            ))
                                        )}
                                    </div>
                                )}
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}