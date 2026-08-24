import api from "@/axios/axios";
import type { SchoolClassResponse } from "./school-class-service";
import type { SubjectResponse } from "./subject-service";

export interface TeachingAssignmentDetail {
    teachingAssignmentId: number;
    schoolClassId: number;
    schoolClassName: string;
    subjectId: number;
    subjectName: string;
    classGroupName: string;
}

export interface TeachingAssignmentResponse {
    id: number;
    subject: SubjectResponse;
    schoolClass: SchoolClassResponse;
}

export const getTeachingAssignmentDetail = async (): Promise<TeachingAssignmentDetail[]> => {
    const {data} = await api.get<TeachingAssignmentDetail[]>(`/academic-service/api/v1/teaching-assignments`);
    return data;
}