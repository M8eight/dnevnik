import api from "../axios/axios";

export interface ScheduleItem {
    id: number;
    lessonNumber: number;
    subjectName: string;
    classRoom: string;
}

export type ScheduleResponse = Record<string, ScheduleItem[]>;

export const getScheduleByDate = async (studentId: number, dayOfWeek: string, date: string): Promise<ScheduleItem[]> => {
    const {data} = await api.get<ScheduleItem[]>(`/academic-service/api/v1/schedule/by-date?studentId=${studentId}&dayOfWeek=${dayOfWeek}&date=${date}`);
    return data;
}

export const getScheduleByStudentId = async (studentId: number): Promise<ScheduleResponse> => {
    const { data } = await api.get<ScheduleResponse>(`/academic-service/api/v1/schedules/by-student`, {
        params: { studentId }
    });
    return data;
}