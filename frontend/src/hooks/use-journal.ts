import { getGradesLessonsByStudentId, getPeriodFinalGradesByStudentId, getTeacherJournal, type TeacherJournalResponse } from "@/services/journal-service";
import { useQuery } from "@tanstack/react-query";

export const useGradesLessonsByStudentId = (academicPeriodId: number) => {
    return useQuery({
        queryKey: ['gradesLessonsByStudentId', academicPeriodId],
        queryFn: () => getGradesLessonsByStudentId(academicPeriodId),
        enabled: !!academicPeriodId,
    });
};

export const usePeriodFinalGradesByStudentId = (academicYearId: number) => {
    return useQuery({
        queryKey: ['periodFinalGradesByStudentId', academicYearId],
        queryFn: () => getPeriodFinalGradesByStudentId(academicYearId),
        enabled: !!academicYearId,
    });
};

export const useTeacherJournal = (teachingAssignmentId: number, academicPeriodId: number) => {
    return useQuery<TeacherJournalResponse>({
        queryKey: ['teacherJournal', teachingAssignmentId, academicPeriodId],
        queryFn: () => getTeacherJournal(teachingAssignmentId, academicPeriodId),
        enabled: !!teachingAssignmentId && !!academicPeriodId,
        staleTime: 1000 * 60 * 5,
    });
};