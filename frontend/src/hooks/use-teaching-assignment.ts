import { getTeachingAssignmentDetail, type TeachingAssignmentDetail } from "@/services/teaching-assignment";
import { useQuery } from "@tanstack/react-query"

export const useTeachingAssignmentDetail = (teacherId: number) => {
    return useQuery<TeachingAssignmentDetail[]>({
        queryKey: ['teachingAssignmentDetail', teacherId],
        queryFn: () => getTeachingAssignmentDetail(teacherId),
        enabled: !!teacherId,
    })
}