import { useQuery } from "@tanstack/react-query"
import { getAvgGradeByStudentId, findAllGradesByDate, getGradesLessonsByStudentId, deleteGrade } from "@/services/grade-service"
import type { AvgGrade, GradeWithSubjectNameResponse } from "@/services/grade-service"

import { useMutation, useQueryClient } from "@tanstack/react-query"
import { createGrade } from "@/services/grade-service"
import type { CreateGradeRequest, CreateGradeResponse } from "@/services/grade-service"


export const useCreateGrade = () => {
    const queryClient = useQueryClient();

    return useMutation<CreateGradeResponse, Error, CreateGradeRequest>({
        mutationFn: createGrade,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['gradesByDate'] });
            queryClient.invalidateQueries({ queryKey: ['avgGrade'] });
            queryClient.invalidateQueries({ queryKey: ['gradesLessonsByStudentId'] });
            queryClient.invalidateQueries({ queryKey: ['teacherJournal'] }); // Добавлено для обновления журнала
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
            queryClient.invalidateQueries({ queryKey: ['teacherJournal'] }); // Обновляем журнал после удаления
        }
    });
};

export const useAvgGrade = (studentId: number, academicPeriodId: number) => {
    return useQuery<AvgGrade>({
        queryKey: ['avgGrade', studentId, academicPeriodId],
        queryFn: () => getAvgGradeByStudentId(studentId, academicPeriodId),
        enabled: !!studentId && !!academicPeriodId,
    });
};

export const useGradesByDate = (studentId: number, date: string) => {
    return useQuery<GradeWithSubjectNameResponse[]>({
        queryKey: ['gradesByDate', studentId, date],
        queryFn: () => findAllGradesByDate(studentId, date),
        enabled: !!studentId && !!date,
    });
};

export const useGradesLessonsByStudentId = (academicPeriodId: number) => {
    return useQuery({
        queryKey: ['gradesLessonsByStudentId', academicPeriodId],
        queryFn: () => getGradesLessonsByStudentId(academicPeriodId),
        enabled: !!academicPeriodId,
    });
};