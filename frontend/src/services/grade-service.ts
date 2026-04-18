import api from "@/axios/axios";

export type AvgGrade = number;
export interface Grade {
    id: number;
    value: number;
    gradeType: string;
    subjectName: string;
}

export interface AcademicPeriodResponse {
    id: number;
    name: string;
    schoolYear: string;
    isClosed: boolean;
    startDate: string;
    endDate: string;
}
export interface GradeLessonDto {
    gradeId: number;
    value: number;
    weight: number;
    gradeType: string;
    date: string;
}
export interface DatesGradesDto {
    subject: string;
    grades: GradeLessonDto[];
}
export interface GradesLessonsResponse {
    academicPeriod: AcademicPeriodResponse;
    dates: string[]; 
    subjects: DatesGradesDto[];
}


export const getAvgGradeByStudentId = async (studentId: number, academicPeriodId: number): Promise<number> => {
    const {data} = await api.get<number>(`/academic-service/api/v1/grades/avg/by-student/${studentId}?academicPeriodId=${academicPeriodId}`);
    return data;
}

export const findAllGradesByDate = async (studentId: number, date: string): Promise<Grade[]> => {
    const {data} = await api.get<Grade[]>(`/academic-service/api/v1/grades/by-date?studentId=${studentId}&date=${date}`);
    return data;
}

export const getGradesLessonsByStudentId = async (studentId: number, academicPeriodId: number): Promise<GradesLessonsResponse> => {
    const { data } = await api.get<GradesLessonsResponse>(
        `/academic-service/api/v1/grades/by-student`, 
        { params: { academicPeriodId, studentId } }
    );
    return data;
};