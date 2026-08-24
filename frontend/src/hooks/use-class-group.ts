import {
    addStudentToClassGroup,
    createClassGroup,
    deleteClassGroup,
    getAllClassGroups,
    getAllClassGroupsBySchoolClass,
    getClassGroupDetails,
    getUnassignedStudentsByClassGroup,
    removeStudentToClassGroup,
    updateClassGroup,
    type ClassGroupDetails,
    type ClassGroupRequest,
    type ClassGroupResponse,
    type ClassGroupWithCountResponse,
} from "@/services/class-group-service";
import type { UserSimpleResponse } from "@/services/user-service";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

export const useAllClassGroups = () => {
    return useQuery<ClassGroupWithCountResponse[]>({
        queryKey: ['classGroups'],
        queryFn: () => getAllClassGroups(),
    });
};

export const useAllClassGroupsBySchoolClass = (schoolClassId: number) => {
    return useQuery<ClassGroupResponse[]>({
        queryKey: ['classGroups', 'by-school-class', schoolClassId],
        queryFn: () => getAllClassGroupsBySchoolClass(schoolClassId),
        enabled: Number.isFinite(schoolClassId) && schoolClassId > 0,
    });
};

export const useClassGroupDetails = (classGroupId: number) => {
    return useQuery<ClassGroupDetails>({
        queryKey: ['classGroups', 'details', classGroupId],
        queryFn: () => getClassGroupDetails(classGroupId),
        enabled: Number.isFinite(classGroupId) && classGroupId > 0,
    });
};

export const useGetUnassignedStudentsByClassGroup = (classGroupId: number) => {
    return useQuery<UserSimpleResponse[]>({
        queryKey: ['classGroups', 'unassignedStudents'],
        queryFn: () => getUnassignedStudentsByClassGroup(classGroupId),
        enabled: Number.isFinite(classGroupId) && classGroupId > 0,
    });
};

export const useCreateClassGroup = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: ClassGroupRequest) => createClassGroup(request),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['classGroups'] });
        }
    });
};

export const useUpdateClassGroup = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ classGroupId, name }: { classGroupId: number; name: string }) =>
            updateClassGroup(classGroupId, name),
        onSuccess: (_, { classGroupId }) => {
            queryClient.invalidateQueries({ queryKey: ['classGroups'] });
            queryClient.invalidateQueries({ queryKey: ['classGroups', 'details', classGroupId] });
        }
    });
};

export const useDeleteClassGroup = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (classGroupId: number) => deleteClassGroup(classGroupId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['classGroups'] });
        }
    });
};

export const useAddStudentToClassGroup = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ classGroupId, studentId }: { classGroupId: number; studentId: number }) =>
            addStudentToClassGroup(classGroupId, studentId),
        onSuccess: (_, { classGroupId }) => {
            queryClient.invalidateQueries({ queryKey: ['classGroups', 'details', classGroupId] });
            queryClient.invalidateQueries({ queryKey: ['classGroups'] });
        }
    });
};

export const useRemoveStudentFromClassGroup = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ classGroupId, studentId }: { classGroupId: number; studentId: number }) =>
            removeStudentToClassGroup(classGroupId, studentId),
        onSuccess: (_, { classGroupId }) => {
            queryClient.invalidateQueries({ queryKey: ['classGroups', 'details', classGroupId] });
            queryClient.invalidateQueries({ queryKey: ['classGroups'] });
        }
    });
};