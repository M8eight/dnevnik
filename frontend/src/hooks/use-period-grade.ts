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
 
export const usePeriodGradesByStudent = (studentId: number, schoolYear: string) => {
  return useQuery<PeriodGradesStudentResponse>({
    queryKey: ["studentPeriodGrades", studentId, schoolYear],
    queryFn: () => getPeriodGradesByStudent(studentId, schoolYear),
    enabled: !!studentId && !!schoolYear,
  });
};

export const usePeriodGradesByAssignment = (teachingAssignmentId: number, currentAcademicPeriodId: number, schoolYear: string) => {
  return useQuery<PeriodGradeTeacherResponse[]>({
    queryKey: ["periodGrades", teachingAssignmentId, currentAcademicPeriodId, schoolYear],
    queryFn: () => getPeriodGradesByAssignment(teachingAssignmentId, currentAcademicPeriodId, schoolYear),
    enabled: !!teachingAssignmentId && !!currentAcademicPeriodId && !!schoolYear,
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