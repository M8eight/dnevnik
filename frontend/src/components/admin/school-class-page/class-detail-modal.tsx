import { useState, useEffect, useRef } from "react";
import {
    X, Loader2, GraduationCap, UserRound, Users,
    UserMinus, UserPlus, Search, ChevronDown, Check,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import {
    useGetClassDetails,
    useAddStudentToClass,
    useRemoveStudentFromClass,
    useGetUnassignedStudents,
    useAssignTeacherToClass,
} from "@/hooks/use-school-class";
import { useFindUsersByFilter } from "@/hooks/use-user";
import type { UserResponse, UserSimpleResponse } from "@/services/user-service";

interface ClassDetailModalProps {
    classId: number | null;
    className?: string;
    onClose: () => void;
}

// ─── tiny helpers ────────────────────────────────────────────────────────────

function Avatar({ name }: { name: string }) {
    const initials = name.split(" ").slice(0, 2).map((w) => w[0]).join("").toUpperCase();
    return (
        <div className="w-9 h-9 rounded-[12px] bg-gradient-to-br from-[var(--red-light)] to-[var(--red)]/20 flex items-center justify-center ring-1 ring-[var(--red)]/15 flex-shrink-0">
            <span className="text-xs font-black text-[var(--red)]">{initials || "?"}</span>
        </div>
    );
}

// ─── TEACHER PICKER (Server-side search) ─────────────────────────────────────

function TeacherPicker({
    placeholder,
    value,
    onSelect,
}: {
    placeholder: string;
    value: UserSimpleResponse | null;
    onSelect: (user: UserSimpleResponse) => void;
}) {
    const [open, setOpen] = useState(false);
    const [search, setSearch] = useState("");
    const ref = useRef<HTMLDivElement>(null);

    const { data, isLoading } = useFindUsersByFilter(0, 30, "TEACHER", search || undefined);
    const teachers = data?.content ?? [];

    useEffect(() => {
        const handler = (e: MouseEvent) => {
            if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
        };
        document.addEventListener("mousedown", handler);
        return () => document.removeEventListener("mousedown", handler);
    }, []);

    const handleSelect = (u: UserResponse) => {
        setOpen(false);
        setSearch("");
        onSelect(u);
    };

    const displayName = value ? `${value.firstName} ${value.lastName}` : null;

    return (
        <div ref={ref} className="relative w-full">
            <button
                type="button"
                onClick={() => setOpen((p) => !p)}
                className="w-full h-10 flex items-center justify-between gap-2 px-3 bg-white/60 border border-black/10 rounded-xl text-sm font-semibold text-left focus:outline-none focus:ring-2 focus:ring-[var(--red)] transition"
            >
                <span className={displayName ? "text-[var(--navy)]" : "text-black/30 font-normal"}>
                    {displayName ?? placeholder}
                </span>
                <ChevronDown className={`w-4 h-4 text-black/30 transition-transform ${open ? "rotate-180" : ""}`} />
            </button>

            {open && (
                <div className="absolute z-50 mt-1 w-full rounded-2xl bg-white border border-black/8 shadow-xl overflow-hidden flex flex-col max-h-60">
                    <div className="p-2 border-b border-black/6 flex-shrink-0">
                        <div className="relative">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-black/30" />
                            <input
                                autoFocus
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                placeholder="Поиск учителя..."
                                className="w-full pl-8 pr-3 py-1.5 text-sm bg-black/4 rounded-lg outline-none placeholder:text-black/30 font-medium"
                            />
                        </div>
                    </div>
                    <div className="overflow-y-auto flex-1">
                        {isLoading ? (
                            <div className="flex justify-center py-4">
                                <Loader2 className="w-4 h-4 animate-spin text-black/30" />
                            </div>
                        ) : teachers.length === 0 ? (
                            <p className="text-xs text-black/30 text-center py-4 font-semibold">Не найдено</p>
                        ) : (
                            teachers.map((u) => {
                                const name = `${u.firstName} ${u.lastName}`;
                                const isActive = value?.id === u.id;
                                return (
                                    <button
                                        key={u.id}
                                        type="button"
                                        onClick={() => handleSelect(u)}
                                        className={`w-full flex items-center gap-3 px-3 py-2.5 text-sm hover:bg-black/4 transition text-left ${isActive ? "bg-[var(--red-light)]/40" : ""}`}
                                    >
                                        <Avatar name={name} />
                                        <span className="font-semibold text-[var(--navy)] flex-1 truncate">{name}</span>
                                        {isActive && <Check className="w-3.5 h-3.5 text-[var(--red)] flex-shrink-0" />}
                                    </button>
                                );
                            })
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}

// ─── STUDENT PICKER (Uses unassignedStudents + Client-side search) ───────────

function StudentPicker({
    placeholder,
    value,
    onSelect,
}: {
    placeholder: string;
    value: UserSimpleResponse | null;
    onSelect: (user: UserSimpleResponse) => void;
}) {
    const [open, setOpen] = useState(false);
    const [search, setSearch] = useState("");
    const ref = useRef<HTMLDivElement>(null);

    const { data: unassignedStudents = [], isLoading } = useGetUnassignedStudents();

    useEffect(() => {
        const handler = (e: MouseEvent) => {
            if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
        };
        document.addEventListener("mousedown", handler);
        return () => document.removeEventListener("mousedown", handler);
    }, []);

    const handleSelect = (u: UserSimpleResponse) => {
        setOpen(false);
        setSearch("");
        onSelect(u);
    };

    // Клиентский поиск по полученным свободным студентам
    const filteredStudents = unassignedStudents.filter((u) => {
        const fullName = `${u.firstName ?? ""} ${u.lastName ?? ""}`.toLowerCase();
        return fullName.includes(search.toLowerCase());
    });

    const displayName = value ? `${value.firstName} ${value.lastName}` : null;

    return (
        <div ref={ref} className="relative w-full">
            <button
                type="button"
                onClick={() => setOpen((p) => !p)}
                className="w-full h-10 flex items-center justify-between gap-2 px-3 bg-white/60 border border-black/10 rounded-xl text-sm font-semibold text-left focus:outline-none focus:ring-2 focus:ring-[var(--red)] transition"
            >
                <span className={displayName ? "text-[var(--navy)]" : "text-black/30 font-normal"}>
                    {displayName ?? placeholder}
                </span>
                <ChevronDown className={`w-4 h-4 text-black/30 transition-transform ${open ? "rotate-180" : ""}`} />
            </button>

            {open && (
                <div className="absolute z-50 mt-1 w-full rounded-2xl bg-white border border-black/8 shadow-xl overflow-hidden flex flex-col max-h-60">
                    <div className="p-2 border-b border-black/6 flex-shrink-0">
                        <div className="relative">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-black/30" />
                            <input
                                autoFocus
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                placeholder="Поиск свободного ученика..."
                                className="w-full pl-8 pr-3 py-1.5 text-sm bg-black/4 rounded-lg outline-none placeholder:text-black/30 font-medium"
                            />
                        </div>
                    </div>
                    <div className="overflow-y-auto flex-1">
                        {isLoading ? (
                            <div className="flex justify-center py-4">
                                <Loader2 className="w-4 h-4 animate-spin text-black/30" />
                            </div>
                        ) : filteredStudents.length === 0 ? (
                            <p className="text-xs text-black/30 text-center py-4 font-semibold">Нет свободных учеников</p>
                        ) : (
                            filteredStudents.map((u) => {
                                const name = `${u.firstName} ${u.lastName}`;
                                const isActive = value?.id === u.id;
                                return (
                                    <button
                                        key={u.id}
                                        type="button"
                                        onClick={() => handleSelect(u)}
                                        className={`w-full flex items-center gap-3 px-3 py-2.5 text-sm hover:bg-black/4 transition text-left ${isActive ? "bg-[var(--red-light)]/40" : ""}`}
                                    >
                                        <Avatar name={name} />
                                        <span className="font-semibold text-[var(--navy)] flex-1 truncate">{name}</span>
                                        {isActive && <Check className="w-3.5 h-3.5 text-[var(--red)] flex-shrink-0" />}
                                    </button>
                                );
                            })
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}

// ─── Main modal ──────────────────────────────────────────────────────────────

export default function ClassDetailModal({ classId, className, onClose }: ClassDetailModalProps) {
    const [newTeacher, setNewTeacher] = useState<UserSimpleResponse | null>(null);
    const [newStudent, setNewStudent] = useState<UserSimpleResponse | null>(null);

    const { data: details, isLoading } = useGetClassDetails(classId);
    const addMutation = useAddStudentToClass();
    const removeMutation = useRemoveStudentFromClass();
    const assignTeacherMutation = useAssignTeacherToClass();

    const isOpen = classId !== null;

    const handleAddStudent = () => {
        if (!classId || !newStudent) return;
        addMutation.mutate(
            { classId, studentId: newStudent.id },
            { onSuccess: () => setNewStudent(null) }
        );
    };

    const handleChangeTeacher = () => {
        if (!classId || !newTeacher) return;
        assignTeacherMutation.mutate(
            { classId, teacherId: newTeacher.id },
            { onSuccess: () => setNewTeacher(null) }
        );
    };

    const handleRemove = (studentId: number) => {
        if (!classId) return;
        removeMutation.mutate({ classId, studentId });
    };

    return (
        <>
            {/* Backdrop */}
            <div
                onClick={onClose}
                className={`fixed inset-0 z-40 bg-black/30 backdrop-blur-[3px] transition-opacity duration-200 ${isOpen ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none"
                    }`}
            />

            {/* Modal Wrapper */}
            <div className="fixed inset-0 z-50 flex items-center justify-center p-4 pointer-events-none">
                <div
                    className={`w-full max-w-[560px] max-h-[85vh] flex flex-col
                        bg-white/90 backdrop-blur-2xl rounded-[32px] border border-black/8 shadow-2xl
                        transition-all duration-300 ease-[cubic-bezier(.32,.72,0,1)] overflow-hidden
                        
                        ${isOpen
                            ? "opacity-100 scale-100 translate-y-0 pointer-events-auto"
                            : "opacity-0 scale-95 translate-y-4 pointer-events-none"
                        }`}
                >
                    {/* Header */}
                    <div className="flex items-center justify-between px-6 pt-6 pb-4 border-b border-black/6 flex-shrink-0">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 rounded-[14px] bg-[var(--red-light)]/60 flex items-center justify-center ring-1 ring-[var(--red)]/10">
                                <GraduationCap className="w-5 h-5 text-[var(--red)]" />
                            </div>
                            <div>
                                <h2 className="font-serif font-black text-xl text-[var(--navy)] leading-tight">
                                    {className ?? "Класс"}
                                </h2>
                                <p className="text-xs text-black/40 mt-0.5">
                                    {isLoading ? "Загрузка..." : `${details?.students.length ?? 0} учеников`}
                                </p>
                            </div>
                        </div>
                        <Button
                            size="icon" variant="ghost" onClick={onClose}
                            className="w-9 h-9 rounded-xl text-black/40 hover:text-black hover:bg-black/6"
                        >
                            <X className="w-4 h-4" />
                        </Button>
                    </div>

                    {/* Body */}
                    {isLoading ? (
                        <div className="flex-1 flex items-center justify-center py-16 text-black/25">
                            <Loader2 className="w-7 h-7 animate-spin" />
                        </div>
                    ) : details ? (
                        <div className="flex-1 overflow-y-auto min-h-0 px-6 py-5 pb-16 flex flex-col gap-6">

                            {/* ── Классный руководитель ── */}
                            <section>
                                <p className="text-[11px] font-black text-black/35 uppercase tracking-widest mb-3">
                                    Классный руководитель
                                </p>
                                <div className="rounded-[20px] bg-white/70 border border-black/6 p-4 flex items-center gap-4 shadow-sm mb-3">
                                    <div className="w-11 h-11 rounded-[14px] bg-gradient-to-br from-[var(--navy)]/10 to-[var(--navy)]/20 flex items-center justify-center ring-1 ring-[var(--navy)]/10 flex-shrink-0">
                                        <UserRound className="w-5 h-5 text-[var(--navy)]" />
                                    </div>
                                    <div className="min-w-0 flex-1">
                                        {(details.teacher) ? (
                                            <>
                                                <p className="font-bold text-sm text-[var(--navy)] truncate">
                                                    {details.teacher.user.firstName} {details.teacher.user.lastName}
                                                </p>
                                                {details.teacher.teacherDetails?.email && (
                                                    <p className="text-xs text-black/40 mt-0.5 truncate">
                                                        {details.teacher.teacherDetails.email}
                                                    </p>
                                                )}
                                            </>
                                        ) : (
                                            <p className="font-bold text-sm text-[var(--navy)]">
                                                Не назначен
                                            </p>
                                        )}

                                    </div>
                                </div>
                                <div className="flex gap-2">
                                    <TeacherPicker
                                        placeholder="Выбрать нового учителя..."
                                        value={newTeacher}
                                        onSelect={setNewTeacher}
                                    />
                                    <Button
                                        onClick={handleChangeTeacher}
                                        disabled={!newTeacher || assignTeacherMutation.isPending}
                                        className="h-10 px-4 rounded-xl bg-[var(--navy)] hover:bg-[var(--navy)]/90 text-white font-bold text-sm gap-1.5 disabled:opacity-40 flex-shrink-0"
                                    >
                                        {assignTeacherMutation.isPending
                                            ? <Loader2 className="w-4 h-4 animate-spin" />
                                            : <Check className="w-4 h-4" />}
                                        Сменить
                                    </Button>
                                </div>
                                {assignTeacherMutation.isError && (
                                    <p className="text-xs text-red-500 font-semibold mt-2">Ошибка при смене учителя</p>
                                )}
                            </section>

                            {/* ── Добавить ученика ── */}
                            <section>
                                <p className="text-[11px] font-black text-black/35 uppercase tracking-widest mb-3">
                                    Добавить ученика
                                </p>
                                <div className="flex gap-2">
                                    <StudentPicker
                                        placeholder="Выбрать свободного ученика..."
                                        value={newStudent}
                                        onSelect={setNewStudent}
                                    />
                                    <Button
                                        onClick={handleAddStudent}
                                        disabled={!newStudent || addMutation.isPending}
                                        className="h-10 px-4 rounded-xl bg-[var(--red)] hover:bg-[var(--red)]/90 text-white font-bold text-sm gap-1.5 disabled:opacity-40 flex-shrink-0"
                                    >
                                        {addMutation.isPending
                                            ? <Loader2 className="w-4 h-4 animate-spin" />
                                            : <UserPlus className="w-4 h-4" />}
                                        Добавить
                                    </Button>
                                </div>
                                {addMutation.isError && (
                                    <p className="text-xs text-red-500 font-semibold mt-2">Ошибка при добавлении</p>
                                )}
                            </section>

                            {/* ── Список учеников ── */}
                            <section>
                                <div className="flex items-center justify-between mb-3">
                                    <p className="text-[11px] font-black text-black/35 uppercase tracking-widest">
                                        Ученики
                                    </p>
                                    <span className="text-[11px] font-bold text-black/30 bg-black/5 rounded-full px-2.5 py-0.5">
                                        {details.students.length}
                                    </span>
                                </div>

                                {details.students.length === 0 ? (
                                    <div className="flex flex-col items-center justify-center py-10 text-black/20">
                                        <Users className="w-10 h-10 mb-2 opacity-40" />
                                        <p className="text-xs font-bold">Нет учеников</p>
                                    </div>
                                ) : (
                                    <div className="flex flex-col gap-2">
                                        {details.students.map((student, idx) => {
                                            const fullName = `${student.firstName ?? ""} ${student.lastName ?? ""}`.trim();
                                            return (
                                                <div
                                                    key={student.id}
                                                    className="group flex items-center gap-3 rounded-[16px] bg-white/60 border border-black/5 px-4 py-3 shadow-sm hover:shadow-md hover:bg-white/80 transition-all duration-150"
                                                    style={{ animationDelay: `${idx * 20}ms` }}
                                                >
                                                    <Avatar name={fullName || "?"} />
                                                    <div className="min-w-0 flex-1">
                                                        <p className="font-bold text-sm text-[var(--navy)] truncate leading-tight">
                                                            {fullName || `Ученик #${student.id}`}
                                                        </p>
                                                    </div>
                                                    <Button
                                                        size="icon" variant="ghost"
                                                        onClick={() => handleRemove(student.id)}
                                                        disabled={
                                                            removeMutation.isPending &&
                                                            removeMutation.variables?.studentId === student.id
                                                        }
                                                        className="w-8 h-8 rounded-xl text-black/25 hover:text-red-500 hover:bg-red-50 opacity-0 group-hover:opacity-100 transition-opacity flex-shrink-0"
                                                    >
                                                        {removeMutation.isPending && removeMutation.variables?.studentId === student.id
                                                            ? <Loader2 className="w-3.5 h-3.5 animate-spin" />
                                                            : <UserMinus className="w-3.5 h-3.5" />}
                                                    </Button>
                                                </div>
                                            );
                                        })}
                                    </div>
                                )}
                            </section>

                        </div>
                    ) : (
                        <div className="flex-1 flex items-center justify-center py-16 text-black/25">
                            <p className="text-sm font-semibold">Не удалось загрузить данные</p>
                        </div>
                    )}
                </div>
            </div>
        </>
    );
}