import api from "@/axios/axios";

export interface Homework {
    id: number;
    text: string;
    subjectName: string;
}

export const getHomeworkByDate = async (date: string, studentId: number): Promise<Homework[]> => {
    const {data} = await api.get<Homework[]>(`/academic-service/api/v1/homeworks/by-date?date=${date}&studentId=${studentId}`);
    return data;
}