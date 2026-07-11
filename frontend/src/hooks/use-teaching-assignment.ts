import { getTeachingAssignmentDetail, type TeachingAssignmentDetail } from "@/services/teaching-assignment-service";
import { useQuery } from "@tanstack/react-query"

export const useTeachingAssignmentDetail = () => {
    return useQuery<TeachingAssignmentDetail[]>({
        queryKey: ['teachingAssignmentDetail'],
        queryFn: () => getTeachingAssignmentDetail(),
    })
}