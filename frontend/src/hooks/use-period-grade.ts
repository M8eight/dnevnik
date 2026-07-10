import {
  createPeriodGrade,
  deletePeriodGrade,
  getPeriodGradesByAssignment,
  getPeriodGradesByStudent,
  type PeriodGradeRequest,
  type PeriodGradesStudentResponse,
  type PeriodGradeTeacherResponse,
} from "@/services/period-grade-service";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
 
export const usePeriodGradesByStudent = (academicYearId: number) => {
  return useQuery<PeriodGradesStudentResponse>({
    queryKey: ["studentPeriodGrades", academicYearId],
    queryFn: () => getPeriodGradesByStudent(academicYearId),
    enabled: !!academicYearId,
  });
};

export const usePeriodGradesByAssignment = (teachingAssignmentId: number, currentAcademicPeriodId: number, academicYearId: number) => {
  return useQuery<PeriodGradeTeacherResponse[]>({
    queryKey: ["periodGrades", teachingAssignmentId, currentAcademicPeriodId, academicYearId],
    queryFn: () => getPeriodGradesByAssignment(teachingAssignmentId, currentAcademicPeriodId, academicYearId),
    enabled: !!teachingAssignmentId && !!currentAcademicPeriodId && !!academicYearId,
  });
};

export const useCreatePeriodGrade = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: PeriodGradeRequest) => createPeriodGrade(request),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['periodGrades'] });
        }
    })
}

export const useDeletePeriodGrade = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (periodGradeId: number) => deletePeriodGrade(periodGradeId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['periodGrades'] });
        }
    })
}