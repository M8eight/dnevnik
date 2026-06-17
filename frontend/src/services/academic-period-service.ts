import api from "@/axios/axios";
import type { AcademicYearResponse } from "./academic-year-service";

export interface AcademicPeriodResponse {
    id: number;
    name: string;
    academicYear: AcademicYearResponse;
    isClosed: boolean;
    startDate: string;
    endDate: string;
}

export interface AcademicPeriodRequest {
    name: string;
    academicYearId: number;
    startDate: string;
    endDate: string;
}

export const findAcademicPeriods = async (): Promise<AcademicPeriodResponse[]> => {
    const {data} = await api.get<AcademicPeriodResponse[]>(`/academic-service/api/v1/academic-periods`);
    return data;
}

export const findAcademicPeriodsByAcademicYear = async (academicYearId: number): Promise<AcademicPeriodResponse[]> => {
    const {data} = await api.get<AcademicPeriodResponse[]>(`/academic-service/api/v1/academic-periods/by-academic-year/${academicYearId}`);
    return data;
}

export const createAcademicPeriod = async (request: AcademicPeriodRequest): Promise<AcademicPeriodResponse> => {
    const {data} = await api.post<AcademicPeriodResponse>(`/academic-service/api/v1/academic-periods`, request);
    return data;
}

export const openAcademicPeriod = async (id: number): Promise<void> => {
    await api.patch(`/academic-service/api/v1/academic-periods/${id}/open`);
}

export const closeAcademicPeriod = async (id: number): Promise<void> => {
    await api.patch(`/academic-service/api/v1/academic-periods/${id}/close`);
}

export const deleteAcademicPeriod = async (id: number): Promise<void> => {
    await api.delete(`/academic-service/api/v1/academic-periods/${id}`);
}