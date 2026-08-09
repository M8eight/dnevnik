import api from "../axios/axios";
import type { AttendanceSimpleResponse } from "./attendance-service";
import type { GradeResponse } from "./grade-service";
import type { HomeworkSimpleResponse } from "./homework-service";
import type { lessonInstance } from "./lesson-instance-service";
import type { SchoolClassResponse } from "./school-class-service";
import type { SubjectResponse } from "./subject-service";
import type { UserSimpleResponse } from "./user-service";

export interface ScheduleItem {
    id: number;
    lessonNumber: number;
    subjectName: string;
    classRoom: string;
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

export interface DiaryWeekResponse {
    weekStart: string;
    weekEnd: string;
    days: DiaryDayDto[];
}

export interface DiaryDayDto {
    dayOfWeek: string;
    date: string;
    lessons: DiaryLessonDto[]
};
export interface DiaryLessonDto {
    lessonNumber: number;
    subject: SubjectResponse;
    scheduleId: number;
    lessonInstanceId: number;
    classRoom: string;
    grades: GradeResponse[];
    attendance: AttendanceSimpleResponse;
    homeworks: HomeworkSimpleResponse[];
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
        id: number;
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

export interface TeacherScheduleItem {
    lessonInstance: lessonInstance;
    subject: SubjectResponse;
    scheduleLesson: ScheduleItem;
    schoolClass: SchoolClassResponse;
    dayOfWeek: string;
}


export type ScheduleResponse = Record<string, ScheduleItem[]>;
export type ScheduleClassResponse = Record<string, ScheduleLessonDto[]>;
export type TeacherScheduleItemPeriod = Record<string, TeacherScheduleItem[]>;


export const getDiaryScheduleByStudentId = async (startDate: string, endDate: string): Promise<DiaryWeekResponse> => {
    const { data } = await api.get<DiaryWeekResponse>(`/academic-service/api/v1/schedules/diary`, {
        params: { startDate, endDate }
    });
    return data;
}

export const getScheduleByClassId = async (classId: number, date: string): Promise<ScheduleClassResponse> => {
    const { data } = await api.get<ScheduleClassResponse>(`/academic-service/api/v1/schedules/by-class`, {
        params: { classId, date }
    });
    return data;
}

export const getTeacherScheduleDate = async (date: string): Promise<TeacherScheduleItem[]> => {
    const { data} = await api.get<TeacherScheduleItem[]>(`/academic-service/api/v1/schedules/by-teacher/date`, {
        params: { date }
    });
    return data;
}

export const getTeacherSchedulePeriod = async (startDate: string, endDate: string): Promise<TeacherScheduleItemPeriod> => {
    const { data } = await api.get<TeacherScheduleItemPeriod>(`/academic-service/api/v1/schedules/by-teacher/period`, {
        params: { startDate, endDate }
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

