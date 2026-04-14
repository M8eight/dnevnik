import api from "../axios/axios";

export interface ScheduleItem {
    id: number;
    lessonNumber: number;
    subjectName: string;
    classRoom: string;
}

export interface Homework {
    id: number;
    text: string;
    subjectName: string | null;
}

export interface Grade {
    gradeId: number;
    studentId: number;
    value: number;
    gradeType: string;
}

export interface Attendance {
    id: number;
    status: string;
}

export interface DiaryLesson {
    lessonDate: string;
    subjectName: string;
    dayOfWeek: string;
    lessonNumber: number;
    classRoom: string;
    homeworks: Homework[];
    grades: Grade[];
    attendance: Attendance | null;
}

export type DiaryResponse = Record<string, DiaryLesson[]>;
export type ScheduleResponse = Record<string, ScheduleItem[]>;

export const getScheduleByDate = async (studentId: number, dayOfWeek: string, date: string): Promise<ScheduleItem[]> => {
    const { data } = await api.get<ScheduleItem[]>(`/academic-service/api/v1/schedule/by-date?studentId=${studentId}&dayOfWeek=${dayOfWeek}&date=${date}`);
    return data;
}

export const getScheduleByStudentId = async (studentId: number): Promise<ScheduleResponse> => {
    const { data } = await api.get<ScheduleResponse>(`/academic-service/api/v1/schedules/by-student`, {
        params: { studentId }
    });
    return data;
}

export const getDiaryLessonsByStudentIdAndDateRange = async (studentId: number, startDate: string, endDate: string): Promise<DiaryResponse> => {
    const { data } = await api.get<DiaryResponse>(`/academic-service/api/v1/schedule`, {
        params: {
            studentId,
            startDate,
            endDate
        }
    });
    return data;
}