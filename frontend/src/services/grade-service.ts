import api from "@/axios/axios";

export type AvgGrade = number;
export interface Grade {
    id: number;
    value: number;
    gradeType: string;
    subjectName: string;
}

export const getAvgGradeByStudentId = async (studentId: number, academicPeriodId: number): Promise<number> => {
    const {data} = await api.get<number>(`/academic-service/api/v1/grades/avg/by-student/${studentId}?academicPeriodId=${academicPeriodId}`);
    return data;
}

export const findAllGradesByDate = async (studentId: number, date: string): Promise<Grade[]> => {
    const {data} = await api.get<Grade[]>(`/academic-service/api/v1/grades/by-date?studentId=${studentId}&date=${date}`);
    return data;
}