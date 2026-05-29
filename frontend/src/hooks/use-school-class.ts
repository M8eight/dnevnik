import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
    addStudentToClass,
    createClass,
    deleteClass,
    getSchoolClasses,
    getSchoolClassDetails,
    removeStudentFromClass,
    updateClass,
    type SchoolClassResponse,
    type SchoolClassRequest,
    getAllUnassignedStudents,
    assignTeacherToClass,
} from "@/services/school-class-service";
import type { UserSimpleResponse } from "@/services/user-service";

const CLASS_QUERY_KEY = ["classes"];

export const useGetAllClasses = () => {
    return useQuery<SchoolClassResponse[]>({
        queryKey: [...CLASS_QUERY_KEY],
        queryFn: () => getSchoolClasses(),
        staleTime: 1000 * 60 * 5,
    });
};

export const useGetClassDetails = (classId: number | null) => {
    return useQuery({
        queryKey: [...CLASS_QUERY_KEY, "details", classId],
        queryFn: () => getSchoolClassDetails(classId!),
        enabled: classId !== null,
        staleTime: 1000 * 60 * 2,
    });
};

export const useGetUnassignedStudents = () => {
    return useQuery<UserSimpleResponse[]>({
        queryKey: [...CLASS_QUERY_KEY, "unassigned-students"],
        queryFn: () => getAllUnassignedStudents(),
        enabled: true,
        staleTime: 1000 * 60 * 10,
    });
};

export const useCreateClass = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (data: SchoolClassRequest) => createClass(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: CLASS_QUERY_KEY });
        },
    });
};

export const useUpdateClass = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ id, data }: { id: number; data: Partial<SchoolClassRequest> }) =>
            updateClass(id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: CLASS_QUERY_KEY });
        },
    });
};

export const useDeleteClass = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (classId: number) => deleteClass(classId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: CLASS_QUERY_KEY });
        },
    });
};

export const useAssignTeacherToClass = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ classId, teacherId }: { classId: number; teacherId: number }) =>
            assignTeacherToClass(classId, teacherId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: CLASS_QUERY_KEY });
        },
    });
}

export const useAddStudentToClass = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ classId, studentId }: { classId: number; studentId: number }) =>
            addStudentToClass(classId, studentId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: CLASS_QUERY_KEY });
        },
    });
};

export const useRemoveStudentFromClass = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ classId, studentId }: { classId: number; studentId: number }) =>
            removeStudentFromClass(classId, studentId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: CLASS_QUERY_KEY });
        },
    });
};