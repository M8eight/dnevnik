import { createSubject, deleteSubject, getSubjects, type PageResponse, type SubjectRequest, type SubjectResponse } from "@/services/subject-service";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

export const useGetAllSubjects = (page: number, size: number) => {
    return useQuery<PageResponse<SubjectResponse>>({
        queryKey: ['subjects', page, size],
        queryFn: () => getSubjects(page, size),
        enabled: page !== undefined && size !== undefined,
        placeholderData: (previousData) => previousData,
    });
};

export const useCreateSubject = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: SubjectRequest) => createSubject(request),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['subjects'] });
        }
    });
};

export const useDeleteSubject = () => {
    const queryClient = useQueryClient();

    return useMutation<void, Error, number>({
        mutationFn: (subjectId: number) => deleteSubject(subjectId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['subjects'] });
        }
    });
};