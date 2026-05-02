import { type Homework, type HomeworkRequest, type HomeworkResponse, type PageResponse, createHomeworks, getHomeworkByDate, getHomeworksByTeachingAssignment } from "@/services/homework-service"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"

export const useHomeworkByDate = (date: string, studentId: number) => {
    return useQuery<Homework[]>({
        queryKey: ['homework', date, studentId],
        queryFn: () => getHomeworkByDate(date, studentId),
        enabled: !!date && !!studentId,
    })
}

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