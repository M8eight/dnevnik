import { useQuery } from "@tanstack/react-query"
import { deleteUser, findUsersByFilter, getParentDetails, getStudentDetails, getTeacherDetails, updateUser, type ParentDetailsResponse, type StudentDetailsResponse, type TeacherDetailsResponse, type UserResponse, type UserRole, type UserUpdateRequest } from "../services/user-service"

import {
    createStudent,
    createParent,
    createTeacher,
    type CreateStudentRequest,
    type CreateParentRequest,
    type CreateTeacherRequest,
} from "@/services/user-service";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import  { type PageResponse } from "@/helpers/helper-interfaces";
import { getStudentFullDetails, type StudentFullDetailsResponse } from "@/services/student-service";

const QUERY_KEY = ["users"];

export const useCreateStudent = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: CreateStudentRequest) => createStudent(request),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
    });
};

export const useCreateParent = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: CreateParentRequest) => createParent(request),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
    });
};

export const useCreateTeacher = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: CreateTeacherRequest) => createTeacher(request),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
    });
};

export const useFindUsersByFilter = (
        page: number, 
        size: number,
        role?: UserRole,
        searchName?: string
) => {
    return useQuery<PageResponse<UserResponse>>({
        queryKey: ['users', 'userFilter', { page, size, role, searchName }],
        queryFn: () => findUsersByFilter(page, size, role, searchName),
    });
};

export const useDeleteUser = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (userId: number) => deleteUser(userId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
    });
}

export const useUpdateUser = (userId: number) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: UserUpdateRequest) => updateUser(userId, request),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: QUERY_KEY })
            queryClient.invalidateQueries({ queryKey: ["users", userId] })
        },
    });
}

export const useStudentDetails = (id: number | null) =>
    useQuery<StudentDetailsResponse>({
        queryKey: ["users", "details", "student", id],
        queryFn: () => getStudentDetails(id!),
        enabled: id !== null,
    });

export const useTeacherDetails = (id: number | null) =>
    useQuery<TeacherDetailsResponse>({
        queryKey: ["users", "details", "teacher", id],
        queryFn: () => getTeacherDetails(id!),
        enabled: id !== null,
    });

export const useParentDetails = (id: number) =>
    useQuery<ParentDetailsResponse>({
        queryKey: ["users", "details", "parent", id],
        queryFn: () => getParentDetails(id),
    });