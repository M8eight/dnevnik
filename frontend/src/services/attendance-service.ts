import api from "@/axios/axios";
import type { LessonInstanceDto } from "./grade-service";

export interface CreateAttendanceResponse {
    attendanceId: number;
    studentId: number;
    status: string;
    lessonInstance: LessonInstanceDto;
}

export interface CreateAttendanceRequest {
    studentId: number;
    status: string;
    lessonInstanceId: number;
}

export const createAttendance = async (request: CreateAttendanceRequest): Promise<CreateAttendanceResponse> => {
    const { data } = await api.post<CreateAttendanceResponse>(
        `/academic-service/api/v1/attendances`,
        request
    );
    return data;
};

export const deleteAttendance = async (attendanceId: number): Promise<void> => {
    await api.delete(`/academic-service/api/v1/attendances/${attendanceId}`);
};