import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { assignStudentToParent, getStudentDetails, unassignStudentFromParent, type StudentDetailsResponse } from "@/services/student-service";

export const useStudentDetails = (id: number) => {
    return useQuery<StudentDetailsResponse>({
        queryKey: ['student', id],
        queryFn: () => getStudentDetails(id),
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
