import type { LessonInstanceDto } from "@/services/grade-service"
import { getLessonInstancesByTeachingAssignment } from "@/services/lesson-instance-service"
import { useQuery } from "@tanstack/react-query"


export const useLessonInstancesByTeachingAssignment = (teachingAssignmentId: number, academicPeriodId: number) => {
    return useQuery<LessonInstanceDto[]>({
        queryKey: ['lesson-instances', teachingAssignmentId, academicPeriodId],
        queryFn: () => getLessonInstancesByTeachingAssignment(teachingAssignmentId, academicPeriodId),
        enabled: !!teachingAssignmentId && academicPeriodId > 0,
    })
}