import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { assignStudentToParent, getStudentFullDetails, unassignStudentFromParent, type StudentFullDetailsResponse } from "@/services/student-service";

export const useStudentFullDetails = (id: number) => {
    return useQuery<StudentFullDetailsResponse>({
        queryKey: ['student', id],
        queryFn: () => getStudentFullDetails(id),
        enabled: !!id,
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
