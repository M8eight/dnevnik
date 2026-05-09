import { useQuery } from "@tanstack/react-query"
import { deleteUser, findUsersByFilter, type UserResponse, type UserRole } from "../services/user-service"

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