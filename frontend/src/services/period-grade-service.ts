import api from "@/axios/axios";
import type { UserSimpleResponse } from "./user-service";

export interface PeriodGradeStudentResponse {
    id: number;
    value: number;
    description: string | null;
    subjectName: string;
    academicPeriodId: number;
}

export interface PeriodGradeTeacherResponse {
    user: UserSimpleResponse;
    periodGrades: PeriodGradeResponse[];
    currentAverage: number | null;
}

export interface PeriodGradeResponse {
    id: number;
    value: number;
    studentId: number;
    description: string | null;
    academicPeriodId: number;
}

export interface PeriodGradeRequest {
    value: number;
    description: string | null;
    teachingAssignmentId: number;
    studentId: number;
    academicPeriodId: number;
}

export type PeriodGradesStudentResponse = Record<string, PeriodGradeStudentResponse[]>;

export const getPeriodGradesByStudent = async (academicYearId: number): Promise<PeriodGradesStudentResponse> => {
    const { data } = await api.get<PeriodGradesStudentResponse>(
        `/academic-service/api/v1/period-grades/by-student`, {
        params: {
            academicYearId,
        }
    }
    );
    return data;
};

export const getPeriodGradesByAssignment = async (teachingAssignmentId: number, currentAcademicPeriodId: number, academicYearId: number): Promise<PeriodGradeTeacherResponse[]> => {
    const { data } = await api.get<PeriodGradeTeacherResponse[]>(
        `/academic-service/api/v1/period-grades/by-assignment`, {
        params: {
            teachingAssignmentId,
            currentAcademicPeriodId,
            academicYearId,
        }
    });
    return data;
};

export const createPeriodGrade = async (periodGradeReq: PeriodGradeRequest): Promise<PeriodGradeRequest> => {
    const { data } = await api.post<PeriodGradeRequest>(`/academic-service/api/v1/period-grades`, periodGradeReq);
    return data;
};

export const deletePeriodGrade = async (periodGradeId: number): Promise<void> => {
    await api.delete(`/academic-service/api/v1/period-grades/${periodGradeId}`);
};