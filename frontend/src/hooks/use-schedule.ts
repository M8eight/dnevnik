import { closeSchedule, createSchedule, deleteSchedule, getDiaryScheduleByStudentId, getScheduleByClassId, getTeacherScheduleDate, getTeacherSchedulePeriod, loadLessonInsance, type DiaryWeekResponse, type ScheduleClassResponse, type ScheduleRequest, type TeacherScheduleItem, type TeacherScheduleItemPeriod } from "@/services/schedule-service"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"

export const useDiaryScheduleByStudentId = (startDate: string, endDate: string) => {
    return useQuery<DiaryWeekResponse>({
        queryKey: ['schedule', startDate, endDate],
        queryFn: () => getDiaryScheduleByStudentId(startDate, endDate),
        enabled: !!startDate && !!endDate,
    })
}

export const useScheduleByClassId = (classId: number, date: string) => {
    return useQuery<ScheduleClassResponse>({
        queryKey: ['classSchedule', classId, date],
        queryFn: () => getScheduleByClassId(classId, date),
        enabled: !!classId && !!date,
    })
}

export const useTeacherScheduleDate = (date: string) => {
    return useQuery<TeacherScheduleItem[]>({
        queryKey: ['classSchedule', date],
        queryFn: () => getTeacherScheduleDate(date),
        enabled: !!date,
    })
}

export const useTeacherSchedulePeriod = (startDate: string, endDate: string) => {
    return useQuery<TeacherScheduleItemPeriod>({
        queryKey: ['teacherSchedule', startDate, endDate],
        queryFn: () => getTeacherSchedulePeriod(startDate, endDate),
        enabled: !!startDate && !!endDate,
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

export const useDeleteSchedule = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (scheduleId: number) => deleteSchedule(scheduleId),
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