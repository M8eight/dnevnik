import { getTeacherJournal, type TeacherJournalResponse } from "@/services/teacher-journal-service";
import { useQuery } from "@tanstack/react-query";

export const useTeacherJournal = (teachingAssignmentId: number, academicPeriodId: number) => {
    return useQuery<TeacherJournalResponse>({
        queryKey: ['teacherJournal', teachingAssignmentId, academicPeriodId],
        queryFn: () => getTeacherJournal(teachingAssignmentId, academicPeriodId),
        enabled: !!teachingAssignmentId && !!academicPeriodId,
        staleTime: 1000 * 60 * 5,
    });
};