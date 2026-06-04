import { createFinalGrade, deleteFinalGrade, getFinalGradesByAssignment, getFinalGradesByStudent, type FinalGradeRequest, type FinalGradesStudentResponse, type FinalGradeTeacherResponse } from "@/services/final-grade-service";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";


export const useFinalGradesByStudent = ( studentId: number, schoolYear: string ) => {
  return useQuery<FinalGradesStudentResponse>({
    queryKey: ["finalGrades", studentId, schoolYear],
    queryFn: () => getFinalGradesByStudent(studentId, schoolYear),
    enabled: !!studentId && !!schoolYear,
  });
};

export const useFinalGradesByAssignment = ( teachingAssignmentId: number, schoolYear: string ) => {
  return useQuery<FinalGradeTeacherResponse[]>({
    queryKey: ["finalGrades", teachingAssignmentId, schoolYear],
    queryFn: () => getFinalGradesByAssignment(teachingAssignmentId, schoolYear),
    enabled: !!teachingAssignmentId && !!schoolYear,
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