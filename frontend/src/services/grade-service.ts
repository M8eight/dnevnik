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

export interface LessonInstanceDto {
    id: number;
    lessonDate: string;
}

export interface CreateGradeRequest {
    studentId: number;
    lessonInstanceId: number;
    academicPeriodId: number;
    value: number;
    weight: number;
    gradeType: string;
}

export interface CreateGradeResponse {
    gradeId: number;
    studentId: number;
    lessonInstance: LessonInstanceDto;
    value: number;
    weight: number;
    gradeType: string;
}

export const createGrade = async (request: CreateGradeRequest): Promise<CreateGradeResponse> => {
    const { data } = await api.post<CreateGradeResponse>(
        `/academic-service/api/v1/grades`,
        request
    );
    return data;
};

export const deleteGrade = async (id: number): Promise<void> => {
    await api.delete(`/academic-service/api/v1/grades/${id}`);
};

export const getAvgGradeByStudentId = async (studentId: number, academicPeriodId: number): Promise<number> => {
    const {data} = await api.get<number>(`/academic-service/api/v1/grades/avg/by-student/${studentId}?academicPeriodId=${academicPeriodId}`);
    return data;
};

export const findAllGradesByDate = async (studentId: number, date: string): Promise<Grade[]> => {
    const {data} = await api.get<Grade[]>(`/academic-service/api/v1/grades/by-date?studentId=${studentId}&date=${date}`);
    return data;
};

export const getGradesLessonsByStudentId = async (studentId: number, academicPeriodId: number): Promise<GradesLessonsResponse> => {
    const { data } = await api.get<GradesLessonsResponse>(
        `/academic-service/api/v1/grades/by-student`, 
        { params: { academicPeriodId, studentId } }
    );
    return data;
};
