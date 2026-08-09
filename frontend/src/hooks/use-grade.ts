import { useQuery } from "@tanstack/react-query"
import { deleteGrade, getGradeDetail } from "@/services/grade-service"
import type { GradeDetailResponse } from "@/services/grade-service"

import { useMutation, useQueryClient } from "@tanstack/react-query"
import { createGrade } from "@/services/grade-service"
import type { CreateGradeRequest, CreateGradeResponse } from "@/services/grade-service"

export const useGradeDetail = (gradeId: number, enabled: boolean = true) => {
    return useQuery<GradeDetailResponse>({
        queryKey: ['gradeDetail', gradeId],
        queryFn: () => getGradeDetail(gradeId),
        enabled: enabled && !!gradeId,
    });
};

export const useCreateGrade = () => {
    const queryClient = useQueryClient();

    return useMutation<CreateGradeResponse, Error, CreateGradeRequest>({
        mutationFn: createGrade,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['gradesByDate'] });
            queryClient.invalidateQueries({ queryKey: ['avgGrade'] });
            queryClient.invalidateQueries({ queryKey: ['gradesLessonsByStudentId'] });
            queryClient.invalidateQueries({ queryKey: ['teacherJournal'] });
        }
    });
};

export const useDeleteGrade = () => {
    const queryClient = useQueryClient();

    return useMutation<void, Error, number>({
        mutationFn: deleteGrade,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['gradesByDate'] });
            queryClient.invalidateQueries({ queryKey: ['avgGrade'] });
            queryClient.invalidateQueries({ queryKey: ['gradesLessonsByStudentId'] });
            queryClient.invalidateQueries({ queryKey: ['teacherJournal'] });
        }
    });
};