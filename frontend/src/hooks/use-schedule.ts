import { getScheduleByDate, getScheduleByStudentId, type ScheduleItem, type ScheduleResponse } from "@/services/schedule-service"
import { useQuery } from "@tanstack/react-query"

export const useScheduleByDate = (studentId: number, dayOfWeek: string, date: string) => {
    return useQuery<ScheduleItem[]>({
        queryKey: ['schedule', studentId, dayOfWeek, date],
        queryFn: () => getScheduleByDate(studentId, dayOfWeek, date),
        enabled: !!studentId && !!dayOfWeek && !!date,
    })
}

export const useScheduleByStudentId = (studentId: number) => {
    return useQuery<ScheduleResponse>({
        queryKey: ['schedule', 'full', studentId],
        queryFn: () => getScheduleByStudentId(studentId),
        enabled: !!studentId,
    })
}