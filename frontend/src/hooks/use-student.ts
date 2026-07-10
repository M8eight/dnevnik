import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { assignStudentToParent, getStudentDetails, getStudentFullDetails, getStudentInfo, unassignStudentFromParent, type StudentFullDetailsResponse, type StudentInfoResponse } from "@/services/student-service";
import type { StudentDetailsResponse } from "@/services/user-service";

const QUERY_KEY = ["student"];

export const useStudentFullDetails = () => {
    return useQuery<StudentFullDetailsResponse>({
        queryKey: [QUERY_KEY],
        queryFn: () => getStudentFullDetails(),
    })
}

export const useAssignStudentToParent = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: { studentId: number; parentId: number }) => assignStudentToParent(request.studentId, request.parentId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ["parent"] }),
    });
};

export const useUnassignStudentFromParent = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: { studentId: number; parentId: number }) => unassignStudentFromParent(request.studentId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ["parent"] }),
    });
};

export const useStudentDetails = (id: number | null) =>
    useQuery<StudentDetailsResponse>({
        queryKey: ["users", "details", QUERY_KEY, id],
        queryFn: () => getStudentDetails(id!),
        enabled: id !== null,
    });

export const useStudentInfo = (id: number) =>
    useQuery<StudentInfoResponse>({
        queryKey: [QUERY_KEY, id],
        queryFn: () => getStudentInfo(id),
    });
