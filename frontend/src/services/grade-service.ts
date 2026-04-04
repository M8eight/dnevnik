import api from "../axios/axios";

export type AvgGrade = number;

export const getAvgGradeByStudentId = async (studentId: number, academicPeriodId: number): Promise<number> => {
    const {data} = await api.get<number>(`/academic-service/api/v1/grades/avg/by-student/${studentId}?academicPeriodId=${academicPeriodId}`);
    return data;
}