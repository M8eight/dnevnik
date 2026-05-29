import api from "../axios/axios";
import type { SubjectResponse } from "./subject-service";
import type { UserSimpleResponse } from "./user-service";

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

export interface ScheduleLessonDto {
    id: number;
    dayOfWeek: string;
    lessonNumber: number;
    classRoom: string;
    validFrom: string;
    validTo: string;
    subject: SubjectResponse;
    teacher: UserSimpleResponse;
}

export interface ScheduleRequest {
    classId: number;
    subjectId: number;
    teacherId: number;
    dayOfWeek: string;
    lessonNumber: number;
    classRoom: string;
    validFrom: string;
}

export interface DiaryScheduleDto {
    id: number;
    dayOfWeek : string;
    lessonNumber : number;
    classRoom : string;
    validFrom : string;
    validTo : string;
    subject : SubjectResponse;
    instance : DiaryLessonInstanceDto;
}

export interface DiaryLessonInstanceDto {
    id: number;
    scheduleId: number;
    lessonDate: string;
    attendances: {
        id: number ;
        status: string ;
        studentId: number ;    
    }[];
    grades: {
        gradeId: number;
        studentId: number;
        value: number;
        weight: number;
        gradeType: string;  
    }[];
    homework: {
        id: number;
        text: string;
    };
}


export type DiaryResponse = Record<string, DiaryLesson[]>;
export type ScheduleResponse = Record<string, ScheduleItem[]>;
export type ScheduleClassResponse = Record<string, ScheduleLessonDto[]>;


export const getDiaryScheduleByStudentId = async (studentId: number, startDate: string, endDate: string): Promise<DiaryScheduleDto[]> => {
    const { data } = await api.get<DiaryScheduleDto[]>(`/academic-service/api/v1/schedules/diary`, {
        params: { studentId, startDate, endDate }
    });
    return data;
}

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

export const getScheduleByClassId = async (classId: number, date: string): Promise<ScheduleClassResponse> => {
    const { data } = await api.get<ScheduleClassResponse>(`/academic-service/api/v1/schedules/by-class`, {
        params: { classId, date }
    });
    return data;
}

export const createSchedule = async (request: ScheduleRequest): Promise<ScheduleRequest> => {
    const { data } = await api.post<ScheduleRequest>(
        `/academic-service/api/v1/schedules`, request
    );
    return data;
};

export const closeSchedule = async (scheduleId: number, closeDate: string): Promise<void> => {
    await api.patch(`/academic-service/api/v1/schedules/${scheduleId}/close?closeDate=${closeDate}`);
}

export const loadLessonInsance = async (classId: number, fromDate: string, toDate: string): Promise<void> => {
    await api.patch(`/academic-service/api/v1/schedules/load?classId=${classId}&fromDate=${fromDate}&toDate=${toDate}`);
}

