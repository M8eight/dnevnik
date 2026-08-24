import { Avatar } from "@/components/layout/layout";
import { Button } from "@/components/ui/button";
import {
    useAddStudentToClassGroup,
    useClassGroupDetails,
    useGetUnassignedStudentsByClassGroup,
    useRemoveStudentFromClassGroup,
} from "@/hooks/use-class-group";
import type { UserSimpleResponse } from "@/services/user-service";
import {
    Loader2,
    UserMinus,
    UserPlus,
} from "lucide-react";
import { useState } from "react";
import GroupStudentPicker from "./group-student-picker";

export default function ClassGroupPanel({
    groupId,
}: {
    groupId: number;
}) {
    const [newGroupStudent, setNewGroupStudent] =
        useState<UserSimpleResponse | null>(null);

    const { data: groupDetails, isLoading: isGroupLoading } =
        useClassGroupDetails(groupId);

    const {
        data: unassignedStudents = [],
        isLoading: isUnassignedLoading,
    } = useGetUnassignedStudentsByClassGroup(groupId);

    const addMutation = useAddStudentToClassGroup();
    const removeMutation = useRemoveStudentFromClassGroup();

    const groupStudentIds = new Set(
        (groupDetails?.students.found ?? []).map((student) => student.id)
    );

    const availableStudents = unassignedStudents?.filter(
        (student) => !groupStudentIds.has(student.id)
    );

    const handleAdd = () => {
        if (!newGroupStudent) return;

        addMutation.mutate(
            {
                classGroupId: groupId,
                studentId: newGroupStudent.id,
            },
            {
                onSuccess: () => setNewGroupStudent(null),
            }
        );
    };

    const handleRemove = (studentId: number) => {
        removeMutation.mutate({
            classGroupId: groupId,
            studentId,
        });
    };

    if (isGroupLoading || isUnassignedLoading) {
        return (
            <div className="flex justify-center py-4">
                <Loader2 className="w-4 h-4 animate-spin text-black/30" />
            </div>
        );
    }

    return (
        <div className="mt-2 mb-1 pl-4 border-l-2 border-black/8 flex flex-col gap-3">
            <div className="flex gap-2">
                <GroupStudentPicker
                    placeholder="Добавить ученика в группу..."
                    value={newGroupStudent}
                    onSelect={setNewGroupStudent}
                    options={availableStudents}
                />

                <Button
                    onClick={handleAdd}
                    disabled={!newGroupStudent || addMutation.isPending}
                    className="h-10 px-3 rounded-xl bg-(--red) hover:bg-(--red)/90 text-white font-bold text-sm gap-1.5 disabled:opacity-40 shrink-0"
                >
                    {addMutation.isPending ? (
                        <Loader2 className="w-4 h-4 animate-spin" />
                    ) : (
                        <UserPlus className="w-4 h-4" />
                    )}
                </Button>
            </div>

            {addMutation.isError && (
                <p className="text-xs text-red-500 font-semibold -mt-1">
                    Ошибка при добавлении ученика в группу
                </p>
            )}

            {groupDetails && groupDetails.students.found.length > 0 ? (
                <div className="flex flex-col gap-1.5">
                    {groupDetails.students.found.map((student) => (
                        <div
                            key={student.id}
                            className="group/item flex items-center gap-3 rounded-xl bg-white/50 border border-black/5 px-3 py-2"
                        >
                            <Avatar
                                firstName={student.firstName}
                                lastName={student.lastName}
                            />

                            <span className="font-semibold text-sm text-(--navy) flex-1 truncate">
                                {`${student.firstName ?? ""} ${student.lastName ?? ""}`.trim()
                                    || `Ученик #${student.id}`}
                            </span>

                            <Button
                                size="icon"
                                variant="ghost"
                                onClick={() => handleRemove(student.id)}
                                disabled={
                                    removeMutation.isPending &&
                                    removeMutation.variables?.studentId === student.id
                                }
                                className="w-7 h-7 rounded-lg text-black/25 hover:text-red-500 hover:bg-red-50 opacity-0 group-hover/item:opacity-100 transition-opacity shrink-0"
                            >
                                {removeMutation.isPending &&
                                removeMutation.variables?.studentId === student.id ? (
                                    <Loader2 className="w-3 h-3 animate-spin" />
                                ) : (
                                    <UserMinus className="w-3 h-3" />
                                )}
                            </Button>
                        </div>
                    ))}
                </div>
            ) : (
                <p className="text-xs text-black/30 font-semibold py-1">
                    В группе пока нет учеников
                </p>
            )}
        </div>
    );
}