import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    createParent,
    createStudent,
    createTeacher,
    deleteUser,
    findUsersByFilter,
    getUserById,
    updateUser,
    type CreateParentRequest,
    type CreateStudentRequest,
    type CreateTeacherRequest,
    type UserResponse,
    type UserRole,
    type UserUpdateRequest,
} from "@/services/user-service";
import type { PageResponse } from "@/helpers/helper-interfaces";

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
    size: number,
    role?: UserRole,
    searchName?: string
) => {
    return useInfiniteQuery<PageResponse<UserResponse>>({
        queryKey: ['users', 'userFilter', { size, role, searchName }],
        initialPageParam: 0,
        queryFn: ({ pageParam }) => findUsersByFilter(pageParam as number, size, role, searchName),
        getNextPageParam: (lastPage) => {
            if (lastPage.last) return undefined;
            return lastPage.number + 1;
        },
    });
};

export const useUserById = (id: number) =>
    useQuery<UserResponse>({
        queryKey: [QUERY_KEY, id],
        queryFn: () => getUserById(id),
    });

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
