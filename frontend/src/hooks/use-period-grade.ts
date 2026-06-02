import {
  createPeriodGrade,
  deletePeriodGrade,
  getStudentPeriodGrades,
  getStudentPeriodGradesWithAverage,
  type PeriodGradeRequest,
  type PeriodGradesStudentResponse,
} from "@/services/period-grade-service";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
 
export const useStudentPeriodGrades = (studentId: number) => {
  return useQuery<PeriodGradesStudentResponse>({
    queryKey: ["studentPeriodGrades", studentId],
    queryFn: () => getStudentPeriodGrades(studentId),
    enabled: !!studentId,
  });
};

export const useStudentPeriodGradesWithAverage = (teachingAssignmentId: number, academicPeriodId: number) => {
  return useQuery({
    queryKey: ["periodGrades", teachingAssignmentId, academicPeriodId],
    queryFn: () => getStudentPeriodGradesWithAverage(teachingAssignmentId, academicPeriodId),
    enabled: !!teachingAssignmentId && !!academicPeriodId,
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