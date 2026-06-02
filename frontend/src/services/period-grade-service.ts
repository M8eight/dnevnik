import api from "@/axios/axios";
import type { AcademicPeriodResponse } from "./grade-service";
import type { UserSimpleResponse } from "./user-service";

export interface PeriodGradeStudentDto {
    id: number;
    value: number;
    description: string | null;
    subjectName: string;
    academicPeriod: AcademicPeriodResponse;
}

export interface PeriodGradeResponse {
    id: number;
    value: number;
    description: string | null;
    studentId: number;
}

export interface StudentAverageResponse {
    user: UserSimpleResponse;
    periodGrade: PeriodGradeResponse | null;
    average: number | null;
}

export interface PeriodGradeRequest {
        value: number;
        description: string | null;
        teachingAssignmentId: number;
        studentId: number;
        academicPeriodId: number;
}
    
export type PeriodGradesStudentResponse = Record<string, PeriodGradeStudentDto[]>;

export const getStudentPeriodGrades = async ( studentId: number ): Promise<PeriodGradesStudentResponse> => {
    const { data } = await api.get<PeriodGradesStudentResponse>(
        `/academic-service/api/v1/period/grades/class?studentId=${studentId}`
    );
    return data;
};

export const getStudentPeriodGradesWithAverage = async ( teachingAssignmentId: number, academicPeriodId: number ): Promise<StudentAverageResponse> => {
    const { data } = await api.get<StudentAverageResponse>(
        `/academic-service/api/v1/period/grades/by-teaching-assignment/with-avg`, {
        params: {
            teachingAssignmentId,
            academicPeriodId,
        }
    });
    return data;
};

export const createPeriodGrade = async (periodGradeReq: PeriodGradeRequest): Promise<PeriodGradeRequest> => {
    const { data } = await api.post<PeriodGradeRequest>(`/academic-service/api/v1/period/grades`, periodGradeReq);
    return data;
};

export const deletePeriodGrade = async (periodGradeId: number): Promise<void> => {
    await api.delete(`/academic-service/api/v1/period/grades/${periodGradeId}`);
};