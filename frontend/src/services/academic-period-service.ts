import api from "@/axios/axios";

export interface AcademicPeriodResponse {
    id: number;
    name: string;
    schoolYear: string;
    isClosed: boolean;
    startDate: string;
    endDate: string;
}

export const findAcademicPeriods = async (): Promise<AcademicPeriodResponse[]> => {
    const {data} = await api.get<AcademicPeriodResponse[]>(`/academic-service/api/v1/academic-periods`);
    return data;
}
