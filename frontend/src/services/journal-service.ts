import api from "@/axios/axios";
import type { UserSimpleResponse } from "./user-service";
import type { AcademicPeriodResponse } from "./academic-period-service";


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