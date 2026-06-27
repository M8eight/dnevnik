import api from "../axios/axios";
import type { SchoolClassResponse } from "./school-class-service";
import type { SubjectResponse } from "./subject-service";
import type { TeachingAssignmentResponse } from "./teaching-assignment-service";
import type {  TeacherDetailsResponse } from "./user-service";

export interface TeacherInfoResponse {
    phoneNumber?: string;
    email?: string;
    schoolDetails: {
        subjects: TeacherSubjectInfo[];
        classes: SchoolClassResponse[];
        assignments: TeachingAssignmentResponse[]
    }
}

export interface TeacherSubjectInfo {
    subject: SubjectResponse;
}

export const getTeacherDetails = async (id: number): Promise<TeacherDetailsResponse> => {
    const { data } = await api.get(`/user-service/api/v1/teachers/${id}/details`);
    return data;
};

export const getTeacherInfo = async (teacherId: number): Promise<TeacherInfoResponse> => {
    const { data } = await api.get<TeacherInfoResponse>(`/user-service/api/v1/teachers/${teacherId}/info`);
    return data;
}
