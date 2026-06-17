import api from "@/axios/axios";
import { type PageResponse } from "@/helpers/helper-interfaces";

export interface HomeworkWithSubjectResponse {
    id: number,
    text: string,
    subjectName: string,
}

export interface HomeworkResponse {
    id: number;
    text: string;
    lessonInstance: {
        id: number;
        lessonDate: string;
    };
}

export interface HomeworkRequest {
    text: string;
    lessonInstanceId: number;
}

export const getHomeworkByDate = async (date: string, studentId: number): Promise<HomeworkWithSubjectResponse[]> => {
    const { data } = await api.get<HomeworkWithSubjectResponse[]>(`/academic-service/api/v1/homeworks/by-date?date=${date}&studentId=${studentId}`);
    return data;
}

export const getHomeworksByTeachingAssignment = async (teachingAssignmentId: number, page: number, size: number): Promise<PageResponse<HomeworkResponse>> => {
    const { data } = await api.get<PageResponse<HomeworkResponse>>(`/academic-service/api/v1/homeworks/by-assignment`, {
        params: {
            teachingAssignmentId,
            page,
            size
        }
    })
    return data;
}

export const createHomeworks = async (request: HomeworkRequest): Promise<HomeworkResponse> => {
    const { data } = await api.post<HomeworkResponse>(
        `/academic-service/api/v1/homeworks`, request
    );
    return data;
};

export const deleteHomework = async (homeworkId: number): Promise<void> => {
    const { data } = await api.delete(`/academic-service/api/v1/homeworks/${homeworkId}`);
    return data;
}