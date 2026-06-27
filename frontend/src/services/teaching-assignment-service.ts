import api from "@/axios/axios";
import type { SubjectResponse } from "./subject-service";
import type { SchoolClassResponse } from "./school-class-service";

export interface TeachingAssignmentDetail {
    teachingAssignmentId: number;
    schoolClassId: number;
    schoolClassName: string;
    subjectId: number;
    subjectName: string;
}

export interface TeachingAssignmentResponse {
    id: number;
    subject: SubjectResponse;
    schoolClass: SchoolClassResponse;
}

export const getTeachingAssignmentDetail = async (teacherId: number): Promise<TeachingAssignmentDetail[]> => {
    const {data} = await api.get<TeachingAssignmentDetail[]>(`/academic-service/api/v1/teaching-assignments`,
        { params: { teacherId } });
    return data;
}