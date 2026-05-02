import api from "@/axios/axios";

export interface SubjectResponse {
    id: number;
    name: string;
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

export interface SubjectRequest {
    subjectName: string;
}

export const getSubjects = async (page: number, size: number): Promise<PageResponse<SubjectResponse>> => {
    const {data} = await api.get<PageResponse<SubjectResponse>>(`/academic-service/api/v1/subjects?page=${page}&size=${size}`);
    return data;
};

export const createSubject = async (request: SubjectRequest): Promise<SubjectResponse> => {
    const { data } = await api.post<SubjectResponse>(
        `/academic-service/api/v1/subjects`,
        request
    );
    return data;
}

export const deleteSubject = async (subjectId: number): Promise<void> => {
    await api.delete(`/academic-service/api/v1/subjects/${subjectId}`);
}