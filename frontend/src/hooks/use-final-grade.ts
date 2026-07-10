import { createFinalGrade, deleteFinalGrade, getFinalGradesByAssignment, getFinalGradesByStudent, type FinalGradeRequest, type FinalGradesStudentResponse, type FinalGradeTeacherResponse } from "@/services/final-grade-service";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";


export const useFinalGradesByStudent = ( academicYearId: number ) => {
  return useQuery<FinalGradesStudentResponse>({
    queryKey: ["finalGrades", academicYearId],
    queryFn: () => getFinalGradesByStudent(academicYearId),
    enabled: !!academicYearId,
  });
};

export const useFinalGradesByAssignment = ( teachingAssignmentId: number, academicYearId: number ) => {
  return useQuery<FinalGradeTeacherResponse[]>({
    queryKey: ["finalGrades", teachingAssignmentId, academicYearId],
    queryFn: () => getFinalGradesByAssignment(teachingAssignmentId, academicYearId),
    enabled: !!teachingAssignmentId && !!academicYearId,
  });
};

export const useCreateFinalGrade = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: FinalGradeRequest) => createFinalGrade(request),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['finalGrades'] });
        }
    })
}

export const useDeleteFinalGrade = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (finalGradeId: number) => deleteFinalGrade(finalGradeId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['finalGrades'] });
        }
    })
}