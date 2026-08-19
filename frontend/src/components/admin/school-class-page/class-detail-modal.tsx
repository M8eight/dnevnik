import { Avatar } from "@/components/layout/layout";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
    useAllClassGroupsBySchoolClass,
    useCreateClassGroup,
    useDeleteClassGroup
} from "@/hooks/use-class-group";
import {
    useAddStudentToClass,
    useAssignTeacherToClass,
    useGetClassDetails,
    useRemoveStudentFromClass
} from "@/hooks/use-school-class";
import type { UserSimpleResponse } from "@/services/user-service";
import {
    AlertTriangle,
    Check,
    ChevronDown,
    FolderPlus,
    GraduationCap,
    Layers,
    Loader2,
    Trash2,
    UserMinus, UserPlus,
    UserRound, Users,
    X
} from "lucide-react";
import { useEffect, useState } from "react";
import StudentPicker from "./student-picker";
import TeacherPicker from "./teacher-picker";
import ClassGroupPanel from "./class-group-panel";

function DataWarningAlert({ children }: { children: React.ReactNode }) {
    return (
        <Alert className="rounded-2xl border-amber-200 bg-amber-50/80 py-2.5 px-3.5">
            <AlertTriangle className="h-4 w-4 text-amber-500" />
            <AlertDescription className="text-xs font-semibold text-amber-700 leading-snug">
                {children}
            </AlertDescription>
        </Alert>
    );
}

interface ClassDetailModalProps {
    classId: number | null;
    className?: string;
    onClose: () => void;
}

export default function ClassDetailModal({ classId, className, onClose }: ClassDetailModalProps) {
    const [newTeacher, setNewTeacher] = useState<UserSimpleResponse | null>(null);
    const [newStudent, setNewStudent] = useState<UserSimpleResponse | null>(null);
    const [newGroupName, setNewGroupName] = useState("");
    const [expandedGroupId, setExpandedGroupId] = useState<number | null>(null);

    const { data: details, isLoading } = useGetClassDetails(classId);
    const addMutation = useAddStudentToClass();
    const removeMutation = useRemoveStudentFromClass();
    const assignTeacherMutation = useAssignTeacherToClass();

    const { data: classGroups = [], isLoading: groupsLoading } = useAllClassGroupsBySchoolClass(classId ?? 0);
    const createGroupMutation = useCreateClassGroup();
    const deleteGroupMutation = useDeleteClassGroup();

    const isOpen = classId !== null;

    useEffect(() => {
        setExpandedGroupId(null);
        setNewGroupName("");
    }, [classId]);

    const teacherDataUnavailable = !!details?.classTeacherId && !details?.teacher;
    const missingStudentsCount = details?.students.notFound.length ?? 0;

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

    const handleCreateGroup = () => {
        if (!classId || !newGroupName.trim()) return;
        createGroupMutation.mutate(
            { name: newGroupName.trim(), schoolClassId: classId },
            { onSuccess: () => setNewGroupName("") }
        );
    };

    const handleDeleteGroup = (groupId: number) => {
        deleteGroupMutation.mutate(groupId, {
            onSuccess: () => {
                setExpandedGroupId((prev) => (prev === groupId ? null : prev));
            },
        });
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
                    className={`w-full max-w-140 max-h-[85vh] flex flex-col
                        bg-white/90 backdrop-blur-2xl rounded-[32px] border border-black/8 shadow-2xl
                        transition-all duration-300 ease-[cubic-bezier(.32,.72,0,1)] overflow-hidden
                        
                        ${isOpen
                            ? "opacity-100 scale-100 translate-y-0 pointer-events-auto"
                            : "opacity-0 scale-95 translate-y-4 pointer-events-none"
                        }`}
                >
                    {/* Header */}
                    <div className="flex items-center justify-between px-6 pt-6 pb-4 border-b border-black/6 shrink-0">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 rounded-[14px] bg-(--red-light)/60 flex items-center justify-center ring-1 ring-(--red)/10">
                                <GraduationCap className="w-5 h-5 text-(--red)" />
                            </div>
                            <div>
                                <h2 className="font-serif font-black text-xl text-(--navy) leading-tight">
                                    {className ?? "Класс"}
                                </h2>
                                <p className="text-xs text-black/40 mt-0.5">
                                    {isLoading
                                        ? "Загрузка..."
                                        : missingStudentsCount > 0
                                            ? `${details?.students.found.length ?? 0} учеников (${missingStudentsCount} недоступно)`
                                            : `${details?.students.found.length ?? 0} учеников`}
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
                                    <div className="w-11 h-11 rounded-[14px] bg-linear-to-br from-(--navy)/10 to-(--navy)/20 flex items-center justify-center ring-1 ring-(--navy)/10 shrink-0">
                                        <UserRound className="w-5 h-5 text-(--navy)" />
                                    </div>
                                    <div className="min-w-0 flex-1">
                                        {details.teacher ? (
                                            <>
                                                <p className="font-bold text-sm text-(--navy) truncate">
                                                    {details.teacher.user.firstName} {details.teacher.user.lastName}
                                                </p>
                                                {details.teacher.teacherDetails?.email && (
                                                    <p className="text-xs text-black/40 mt-0.5 truncate">
                                                        {details.teacher.teacherDetails.email}
                                                    </p>
                                                )}
                                            </>
                                        ) : teacherDataUnavailable ? (
                                            <p className="font-bold text-sm text-amber-600">
                                                Данные учителя временно недоступны
                                            </p>
                                        ) : (
                                            <p className="font-bold text-sm text-(--navy)">
                                                Не назначен
                                            </p>
                                        )}
                                    </div>
                                </div>

                                {teacherDataUnavailable && (
                                    <div className="mb-3">
                                        <DataWarningAlert>
                                            Учитель назначен, но его данные сейчас недоступны. Попробуйте обновить страницу позже.
                                        </DataWarningAlert>
                                    </div>
                                )}

                                <div className="flex gap-2">
                                    <TeacherPicker
                                        placeholder="Выбрать нового учителя..."
                                        value={newTeacher}
                                        onSelect={setNewTeacher}
                                    />
                                    <Button
                                        onClick={handleChangeTeacher}
                                        disabled={!newTeacher || assignTeacherMutation.isPending}
                                        className="h-10 px-4 rounded-xl bg-(--navy) hover:bg-(--navy)/90 text-white font-bold text-sm gap-1.5 disabled:opacity-40 shrink-0"
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

                            {/* ── Группы класса ── */}
                            <section>
                                <div className="flex items-center justify-between mb-3">
                                    <p className="text-[11px] font-black text-black/35 uppercase tracking-widest">
                                        Группы класса
                                    </p>
                                    <span className="text-[11px] font-bold text-black/30 bg-black/5 rounded-full px-2.5 py-0.5">
                                        {classGroups.length}
                                    </span>
                                </div>

                                <div className="flex gap-2 mb-3">
                                    <input
                                        value={newGroupName}
                                        onChange={(e) => setNewGroupName(e.target.value)}
                                        onKeyDown={(e) => {
                                            if (e.key === "Enter") handleCreateGroup();
                                        }}
                                        placeholder="Название новой группы..."
                                        className="flex-1 h-10 px-3 bg-white/60 border border-black/10 rounded-xl text-sm font-semibold outline-none placeholder:text-black/30 placeholder:font-normal focus:ring-2 focus:ring-(--red) transition"
                                    />
                                    <Button
                                        onClick={handleCreateGroup}
                                        disabled={!newGroupName.trim() || createGroupMutation.isPending}
                                        className="h-10 px-4 rounded-xl bg-(--navy) hover:bg-(--navy)/90 text-white font-bold text-sm gap-1.5 disabled:opacity-40 shrink-0"
                                    >
                                        {createGroupMutation.isPending
                                            ? <Loader2 className="w-4 h-4 animate-spin" />
                                            : <FolderPlus className="w-4 h-4" />}
                                        Создать
                                    </Button>
                                </div>
                                {createGroupMutation.isError && (
                                    <p className="text-xs text-red-500 font-semibold mb-3">Ошибка при создании группы</p>
                                )}

                                {groupsLoading ? (
                                    <div className="flex justify-center py-6">
                                        <Loader2 className="w-5 h-5 animate-spin text-black/25" />
                                    </div>
                                ) : classGroups.length === 0 ? (
                                    <div className="flex flex-col items-center justify-center py-8 text-black/20">
                                        <Layers className="w-8 h-8 mb-2 opacity-40" />
                                        <p className="text-xs font-bold">Групп пока нет</p>
                                    </div>
                                ) : (
                                    <div className="flex flex-col gap-2">
                                        {classGroups.map((group) => {
                                            const isExpanded = expandedGroupId === group.id;
                                            const isDeletingThis =
                                                deleteGroupMutation.isPending && deleteGroupMutation.variables === group.id;
                                            return (
                                                <div key={group.id}>
                                                    <div className="flex items-center gap-2 rounded-xl bg-white/60 border border-black/6 px-3 py-2.5">
                                                        <button
                                                            type="button"
                                                            onClick={() => setExpandedGroupId(isExpanded ? null : group.id)}
                                                            className="flex-1 flex items-center gap-2 text-left min-w-0"
                                                        >
                                                            <Layers className="w-4 h-4 text-(--navy)/50 shrink-0" />
                                                            <span className="font-bold text-sm text-(--navy) truncate">{group.name}</span>
                                                            <ChevronDown
                                                                className={`w-3.5 h-3.5 text-black/30 transition-transform ml-auto shrink-0 ${isExpanded ? "rotate-180" : ""}`}
                                                            />
                                                        </button>
                                                        <Button
                                                            size="icon" variant="ghost"
                                                            onClick={() => handleDeleteGroup(group.id)}
                                                            disabled={isDeletingThis}
                                                            className="w-7 h-7 rounded-lg text-black/25 hover:text-red-500 hover:bg-red-50 shrink-0"
                                                        >
                                                            {isDeletingThis
                                                                ? <Loader2 className="w-3.5 h-3.5 animate-spin" />
                                                                : <Trash2 className="w-3.5 h-3.5" />}
                                                        </Button>
                                                    </div>
                                                    {isExpanded && (
                                                        <ClassGroupPanel
                                                            groupId={group.id}
                                                        />
                                                    )}
                                                </div>
                                            );
                                        })}
                                    </div>
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
                                        className="h-10 px-4 rounded-xl bg-(--red) hover:bg-(--red)/90 text-white font-bold text-sm gap-1.5 disabled:opacity-40 shrink-0"
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
                                        {details.students.found.length}
                                    </span>
                                </div>

                                {missingStudentsCount > 0 && (
                                    <div className="mb-3">
                                        <DataWarningAlert>
                                            {missingStudentsCount === 1
                                                ? "1 ученик не отображается — данные временно недоступны."
                                                : `${missingStudentsCount} учеников не отображаются — данные временно недоступны.`}
                                        </DataWarningAlert>
                                    </div>
                                )}

                                {details.students.found.length === 0 ? (
                                    <div className="flex flex-col items-center justify-center py-10 text-black/20">
                                        <Users className="w-10 h-10 mb-2 opacity-40" />
                                        <p className="text-xs font-bold">Нет учеников</p>
                                    </div>
                                ) : (
                                    <div className="flex flex-col gap-2">
                                        {details.students.found.map((student, idx) => {
                                            const fullName = `${student.firstName ?? ""} ${student.lastName ?? ""}`.trim();
                                            return (
                                                <div
                                                    key={student.id}
                                                    className="group flex items-center gap-3 rounded-[16px] bg-white/60 border border-black/5 px-4 py-3 shadow-sm hover:shadow-md hover:bg-white/80 transition-all duration-150"
                                                    style={{ animationDelay: `${idx * 20}ms` }}
                                                >
                                                    <Avatar firstName={student.firstName} lastName={student.lastName} />

                                                    <div className="min-w-0 flex-1">
                                                        <p className="font-bold text-sm text-(--navy) truncate leading-tight">
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
                                                        className="w-8 h-8 rounded-xl text-black/25 hover:text-red-500 hover:bg-red-50 opacity-0 group-hover:opacity-100 transition-opacity shrink-0"
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