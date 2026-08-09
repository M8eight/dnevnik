import api from "@/axios/axios";
import type { UserSimpleResponse } from "./user-service";

export interface Grade {
    id: number;
    value: number;
    gradeType: string;
    subjectName: string;
}

export interface GradeResponse {
    id: number;
    studentId: number;
    value: number;
    weight: number;
    type: string;
}

export interface GradeLessonDto {
    gradeId: number;
    value: number;
    weight: number;
    gradeType: string;
    date: string;
}

export interface LessonInstanceDto {
    id: number;
    lessonDate: string;
}

export interface CreateGradeRequest {
    studentId: number;
    lessonInstanceId: number;
    academicPeriodId: number;
    value: number;
    weight: number;
    gradeType: string;
}

export interface CreateGradeResponse {
    gradeId: number;
    studentId: number;
    lessonInstance: LessonInstanceDto;
    value: number;
    weight: number;
    gradeType: string;
}

export interface GradeWithSubjectNameResponse {
    id: number,
    value: number,
    gradeType: string,
    subjectName: string
}

export interface GradeDetailResponse {
    id: number;
    date: string;
    type: string;
    value: number;
    weight: number;
    teacher: UserSimpleResponse
}

export const getGradeDetail = async (gradeId: number): Promise<GradeDetailResponse> => {
    const {data} = await api.get<GradeDetailResponse>(`/academic-service/api/v1/grades/${gradeId}/detail`);
    return data;
};

export const createGrade = async (request: CreateGradeRequest): Promise<CreateGradeResponse> => {
    const { data } = await api.post<CreateGradeResponse>(
        `/academic-service/api/v1/grades`,
        request
    );
    return data;
};

export const deleteGrade = async (id: number): Promise<void> => {
    await api.delete(`/academic-service/api/v1/grades/${id}`);
};