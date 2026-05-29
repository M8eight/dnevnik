import api from "@/axios/axios";
import type { UserSimpleResponse } from "./user-service";
import type { SubjectResponse } from "./subject-service";


export interface TeacherSubjectResponse {
    teacher: UserSimpleResponse;
    subject: SubjectResponse;
}

export interface TeacherSubjectRequest {
    teacherId: number;
    subjectId: number;
}

export const getTeacherSubjects = async (): Promise<TeacherSubjectResponse[]> => {
    const { data } = await api.get<TeacherSubjectResponse[]>(`/academic-service/api/v1/teacher-subjects`);
    return data;
}

export const createTeacherSubject = async (request: TeacherSubjectRequest): Promise<TeacherSubjectResponse> => {
    const { data } = await api.post<TeacherSubjectResponse>(`/academic-service/api/v1/teacher-subjects`, request);
    return data;
}

export const deleteTeacherSubject = async (request: TeacherSubjectRequest): Promise<TeacherSubjectResponse> => {
    const { data } = await api.delete<TeacherSubjectResponse>(`/academic-service/api/v1/teacher-subjects`, { data: request });
    return data;
}