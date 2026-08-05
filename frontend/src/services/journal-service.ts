import api from "@/axios/axios";
import type { UserSimpleResponse } from "./user-service";
import type { AcademicPeriodResponse } from "./academic-period-service";
import type { GradeLessonDto } from "./grade-service";
import type { FinalGradeResponse } from "./final-grade-service";


export interface LessonInstanceDto {
    id: number;
    lessonDate: string;
}

export interface GradeLessonTeacherDto {
    gradeId: number;
    value: number;
    weight: number;
    gradeType: string;
    lessonInstanceId: number;
}

export interface AttendanceLessonTeacherDto {
    attendanceId: number;
    status: string;
    lessonInstanceId: number;
}

export interface StudentJournalDto {
    student: UserSimpleResponse;
    gradesByLesson: Record<number, GradeLessonTeacherDto[]>;
    attendancesByLesson: Record<number, AttendanceLessonTeacherDto>;
    gradesAverage : number | null;
}

export interface TeacherJournalResponse {
    academicPeriod: AcademicPeriodResponse;
    lessonInstances: LessonInstanceDto[];
    studentsJournal: StudentJournalDto[];
    isDegradedStudents: boolean;
}

export interface GradesLessonsResponse {
    academicPeriod: AcademicPeriodResponse;
    dates: string[]; 
    gradesBySubjects: DatesGradesDto[];
}

export interface DatesGradesDto {
    subject: string;
    grades: GradeLessonDto[];
    average: number;
}

export interface PeriodFinalGradeResponse {
    subjectName: string;
    periodGrades: PeriodGradeStudentResponse[];
    finalGrade: FinalGradeResponse;
}

export interface PeriodGradeStudentResponse {
    id: number;
    value: number;
    description: string | null;
    subjectName: string;
    academicPeriodId: number;
}

export const getGradesLessonsByStudentId = async (academicPeriodId: number): Promise<GradesLessonsResponse> => {
    const { data } = await api.get<GradesLessonsResponse>(
        `/academic-service/api/v1/grades/by-student`, 
        { params: { academicPeriodId } }
    );
    return data;
};

export const getPeriodFinalGradesByStudentId = async (academicYearId: number): Promise<PeriodFinalGradeResponse[]> => {
    const { data } = await api.get<PeriodFinalGradeResponse[]>(
        `/academic-service/api/v1/period-final-grades/by-student`,
        { params: { academicYearId } }
    );
    return data;
};

export const getTeacherJournal = async (
    teachingAssignmentId: number, 
    academicPeriodId: number
): Promise<TeacherJournalResponse> => {
    const { data } = await api.get<TeacherJournalResponse>(
        `/academic-service/api/v1/journal/by-assignment`, 
        { params: { teachingAssignmentId, academicPeriodId } }
    );
    return data;
};
