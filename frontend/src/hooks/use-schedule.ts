import { closeSchedule, createSchedule, getDiaryLessonsByStudentIdAndDateRange, getDiaryScheduleByStudentId, getScheduleByClassId, getScheduleByDate, getScheduleByStudentId, loadLessonInsance, type DiaryResponse, type DiaryScheduleDto, type ScheduleClassResponse, type ScheduleItem, type ScheduleRequest, type ScheduleResponse } from "@/services/schedule-service"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"

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

export const useDiaryScheduleByStudentId = (startDate: string, endDate: string) => {
    return useQuery<DiaryScheduleDto[]>({
        queryKey: ['schedule', startDate, endDate],
        queryFn: () => getDiaryScheduleByStudentId(startDate, endDate),
        enabled: !!startDate && !!endDate,
    })
}

export const useDiaryLessonsByStudentIdAndDateRange = (
    studentId: number, 
    startDate: string, 
    endDate: string
) => {
    return useQuery<DiaryResponse>({
        queryKey: ['schedule', 'diary', studentId, startDate, endDate],
        queryFn: () => getDiaryLessonsByStudentIdAndDateRange(studentId, startDate, endDate),
        enabled: !!studentId && !!startDate && !!endDate,
    })
}

export const useScheduleByClassId = (classId: number, date: string) => {
    return useQuery<ScheduleClassResponse>({
        queryKey: ['classSchedule', classId, date],
        queryFn: () => getScheduleByClassId(classId, date),
        enabled: !!classId && !!date,
    })
}

export const useCreateSchedule = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: ScheduleRequest) => createSchedule(request),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['schedule'] });
        }
    })
}

export const useCloseSchedule = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ scheduleId, closeDate }: { scheduleId: number; closeDate: string }) => closeSchedule(scheduleId, closeDate),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['schedule'] });
        }
    })
}

export const useLoadLessonInstance = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ classId, fromDate, toDate }: { classId: number; fromDate: string; toDate: string }) => loadLessonInsance(classId, fromDate, toDate),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['schedule'] });
        }
    })
}