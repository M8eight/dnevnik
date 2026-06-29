import { type TeacherSubjectRequest, deleteTeacherSubject, createTeacherSubject, getTeacherSubjects, type TeacherSubjectResponse } from "@/services/teacher-subject-service";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

const QUERY_KEY = "teacher-subjects";

export const useGetTeacherSubjects = () => {
    return useQuery<TeacherSubjectResponse[]>({
        queryKey: [QUERY_KEY],
        queryFn: () => getTeacherSubjects(),
    });
};

export const useCreateTeacherSubject = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: TeacherSubjectRequest) => createTeacherSubject(request),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: [QUERY_KEY] }),
    });
}

export const useDeleteTeacherSubject = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: TeacherSubjectRequest) => deleteTeacherSubject(request),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: [QUERY_KEY] }),
    });
}