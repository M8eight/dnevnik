import api from "@/axios/axios";

export interface Homework {
    id: number;
    text: string;
    subjectName: string;
}

export interface HomeworkResponse {
    id: number;
    text: string;
    lessonInstance: {
        id: number;
        lessonDate: string;
    };
}

export interface PageResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}

export interface HomeworkRequest {
    text: string;
    lessonInstanceId: number;
}

export const getHomeworkByDate = async (date: string, studentId: number): Promise<Homework[]> => {
    const { data } = await api.get<Homework[]>(`/academic-service/api/v1/homeworks/by-date?date=${date}&studentId=${studentId}`);
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