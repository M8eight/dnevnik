import type { PageResponse } from "@/helpers/helper-interfaces"
import { type HomeworkRequest, type HomeworkResponse, createHomeworks, deleteHomework, getHomeworksByTeachingAssignment } from "@/services/homework-service"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"

export const useHomeworksByTeachingAssignment = (teachingAssginmentId: number, page: number, size: number) => {
    return useQuery<PageResponse<HomeworkResponse>>({
        queryKey: ['homeworkTeachingAssignment', teachingAssginmentId, page, size],
        queryFn: () => getHomeworksByTeachingAssignment(teachingAssginmentId, page, size),
        enabled: !!teachingAssginmentId && page !== undefined && size !== undefined,
    })
}

export const useCreateHomework = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: HomeworkRequest) => createHomeworks(request),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['homeworkTeachingAssignment'] });
        }
    })
}

export const useDeleteHomework = () => {
    const queryClient = useQueryClient();

    return useMutation<void, Error, number>({
        mutationFn: (homeworkId: number) => deleteHomework(homeworkId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['homeworkTeachingAssignment'] });
        }
    })
}