import { type Homework, getHomeworkByDate } from "@/services/homework-service"
import { useQuery } from "@tanstack/react-query"

export const useHomeworkByDate = (date: string, studentId: number) => {
    return useQuery<Homework[]>({
        queryKey: ['homework', date, studentId],
        queryFn: () => getHomeworkByDate(date, studentId),
        enabled: !!date && !!studentId,
    })
}