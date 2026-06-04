import api from "@/axios/axios";
import type { UserSimpleResponse } from "./user-service";

export interface FinalGradeResponse {
    id: number;
    studentId: number;
    schoolYear: string;
    value: number;
    description: string;
    subjectName: string;
}

export interface FinalGradeTeacherResponse {
    user: UserSimpleResponse;
    finalGrades: FinalGradeResponse[];
}

export interface FinalGradeCreateResponse {
    id: number;
    studentId: number;
    schoolYear: string;
    value: number;
    description: string;
}

export interface FinalGradeRequest {
        studentId: number;
        schoolYear: string;
        value: number;
        description: string;
        teachingAssignmentId: number;
}

export type FinalGradesStudentResponse = Record<string, FinalGradeResponse>;

export const getFinalGradesByStudent = async (studentId: number, schoolYear: string): Promise<FinalGradesStudentResponse> => {
    const { data } = await api.get<FinalGradesStudentResponse>(
        `/academic-service/api/v1/final-grades/by-student`, {
            params: {
                studentId,
                schoolYear
            }
        }
    );
    return data;
};

export const getFinalGradesByAssignment = async (teachingAssignmentId: number, schoolYear: string): Promise<FinalGradeTeacherResponse[]> => {
    const { data } = await api.get<FinalGradeTeacherResponse[]>(
        `/academic-service/api/v1/final-grades/by-assignment`, {
            params: {
                teachingAssignmentId,
                schoolYear
            }
        }
    );
    return data;
}

export const createFinalGrade = async (finalGradeReq: FinalGradeRequest): Promise<FinalGradeCreateResponse> => {
    const { data } = await api.post<FinalGradeCreateResponse>(
        `/academic-service/api/v1/final-grades`, finalGradeReq
    );
    return data;
}

export const deleteFinalGrade = async (finalGradeId: number): Promise<void> => {
    await api.delete(`/academic-service/api/v1/final-grades/${finalGradeId}`);
}