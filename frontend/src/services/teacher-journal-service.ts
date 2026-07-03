import api from "@/axios/axios";
import type { AcademicYearResponse } from "./academic-year-service";
import type { BatchUserResponse } from "./user-service";


export interface StudentMetadata {
    id: number;
    firstName: string;
    lastName: string;
    keycloakId: string;
}


export interface LessonInstanceDto {
    id: number;
    lessonDate: string;
}

export interface GradeJournalDto {
    gradeId: number;
    value: number;
    weight: number;
    gradeType: string;
    lessonInstanceId: number;
}

export interface AttendanceJournalDto {
    attendanceId: number;
    status: string;
    lessonInstanceId: number;
}

export interface StudentJournalEntry {
    studentId: number;
    grades: GradeJournalDto[];
    gradesAverage : number | null;
    attendances: AttendanceJournalDto[];
}

export interface TeacherJournalResponse {
    academicPeriod: {
        id: number;
        name: string;
        academicYear: AcademicYearResponse;
        isClosed: boolean;
        startDate: string;
        endDate: string;
    };
    students: BatchUserResponse;
    lessonInstances: LessonInstanceDto[];
    studentsJournal: StudentJournalEntry[];
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