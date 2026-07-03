import api from "@/axios/axios";

export interface AcademicYearResponse {
    id: number;
    name: string;
    description?: string;
    startDate: string;
    endDate: string;
    closed: boolean;
}

export interface AcademicYearRequest {
    name: string;
    description?: string;
    startDate: string;
    endDate: string;
}

export const getAcademicYears = async (): Promise<AcademicYearResponse[]> => {
    const { data } = await api.get<AcademicYearResponse[]>(`/academic-service/api/v1/academic-years`);
    return data;
};

export const getAcademicYearById = async (id: number): Promise<AcademicYearResponse> => {
    const { data } = await api.get<AcademicYearResponse>(`/academic-service/api/v1/academic-years/${id}`);
    return data;
};

export const createAcademicYear = async (request: AcademicYearRequest): Promise<AcademicYearResponse> => {
    const { data } = await api.post<AcademicYearResponse>(`/academic-service/api/v1/academic-years`, request);
    return data;
};

export const openAcademicYear = async (id: number): Promise<void> => {
    await api.patch(`/academic-service/api/v1/academic-years/${id}/open`);
}

export const closeAcademicYear = async (id: number): Promise<void> => {
    await api.patch(`/academic-service/api/v1/academic-years/${id}/close`);
}


export const updateAcademicYear = async (id: number, request: Partial<AcademicYearRequest>): Promise<void> => {
    await api.put(`/academic-service/api/v1/academic-years/${id}`, request);
};

export const deleteAcademicYear = async (id: number): Promise<void> => {
    await api.delete(`/academic-service/api/v1/academic-years/${id}`);
};
