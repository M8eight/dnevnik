import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    assignStudentToParent,
    getStudentDetails,
    getStudentFullDetails,
    getStudentInfo,
    getStudentsByParentId,
    getStudentWithParent,
    getUnassignedToParentStudents,
    unassignStudentFromParent,
    type StudentFullDetailsResponse,
    type StudentInfoResponse,
    type StudentWithParentDto,
} from "@/services/student-service";
import type { StudentDetailsResponse, UserResponse } from "@/services/user-service";
import type { PageResponse } from "@/helpers/helper-interfaces";

const QUERY_KEY = "student";

export const useStudentFullDetails = () => {
    return useQuery<StudentFullDetailsResponse>({
        queryKey: [QUERY_KEY, "full"],
        queryFn: () => getStudentFullDetails(),
    });
};

export const useUnassignedToParentStudents = (
    size: number,
    searchName?: string
) => {
    return useInfiniteQuery<PageResponse<UserResponse>>({
        queryKey: ["students", "unassignedToParent", { size, searchName }],
        initialPageParam: 0,
        queryFn: ({ pageParam }) =>
            getUnassignedToParentStudents(pageParam as number, size, searchName),
        getNextPageParam: (lastPage) => {
            if (lastPage.last) return undefined;
            return lastPage.number + 1;
        },
    });
};

export const useStudentWithParent = (userId: number) => {
    return useQuery<StudentWithParentDto>({
        queryKey: [QUERY_KEY, "with-parent", userId],
        queryFn: () => getStudentWithParent(userId),
        enabled: !!userId,
    });
};

export const useStudentsByParentId = (options?: { enabled?: boolean }) => {
    return useQuery<UserResponse[]>({
        queryKey: [QUERY_KEY, "by-parent"],
        queryFn: () => getStudentsByParentId(),
        enabled: options?.enabled ?? true,
    });
};

export const useAssignStudentToParent = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: { studentId: number; parentId: number }) =>
            assignStudentToParent(request.studentId, request.parentId),
        onSuccess: (_data, variables) => {
            queryClient.invalidateQueries({ queryKey: ["parent"] });
            queryClient.invalidateQueries({
                queryKey: [QUERY_KEY, "with-parent", variables.studentId],
            });
            queryClient.invalidateQueries({
                queryKey: ["students", "unassignedToParent"],
            });
            queryClient.invalidateQueries({
                queryKey: ["parents", "unassignedToStudent"],
            });
        },
    });
};

export const useUnassignStudentFromParent = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: { studentId: number; parentId: number }) =>
            unassignStudentFromParent(request.studentId),
        onSuccess: (_data, variables) => {
            queryClient.invalidateQueries({ queryKey: ["parent"] });
            queryClient.invalidateQueries({
                queryKey: [QUERY_KEY, "with-parent", variables.studentId],
            });
            queryClient.invalidateQueries({
                queryKey: ["students", "unassignedToParent"],
            });
            queryClient.invalidateQueries({
                queryKey: ["parents", "unassignedToStudent"],
            });
        },
    });
};

export const useStudentDetails = (id: number | null) =>
    useQuery<StudentDetailsResponse>({
        queryKey: [QUERY_KEY, "details", id],
        queryFn: () => getStudentDetails(id!),
        enabled: id !== null,
    });

export const useStudentInfo = (id: number) =>
    useQuery<StudentInfoResponse>({
        queryKey: [QUERY_KEY, "info", id],
        queryFn: () => getStudentInfo(id),
    });