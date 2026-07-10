import api from "../axios/axios";
import type { SchoolClassResponse } from "./school-class-service";
import type { StudentDetailsResponse, TeacherDetails, UserResponse } from "./user-service";

export interface StudentFullDetailsResponse {
    id: number;
    userId: number;
    keycloakId: string;
    firstName: string;
    lastName: string;
    studyProfile: string;
    schoolClass: {
        id: number;
        name: string;
        year: string;
        classTeacherId: number;
    };
    schoolClassTeacher: {
        user: UserResponse;
        details: TeacherDetails;
    }
}

export interface StudentInfoResponse {
    studyProfile?: string;
    parent?: UserResponse;
    schoolClass?: SchoolClassResponse;
    classTeacher?: {
        user: UserResponse;
        details: TeacherDetails;
    };
}


export const getStudentFullDetails = async (): Promise<StudentFullDetailsResponse> => {
    const { data } = await api.get<StudentFullDetailsResponse>(`/user-service/api/v1/students/with-class`);
    return data;
}

export const assignStudentToParent = async (studentId: number, parentId: number): Promise<void> => {
    await api.patch(`/user-service/api/v1/students/${studentId}/assign/${parentId}`);
}

export const unassignStudentFromParent = async (studentId: number): Promise<void> => {
    await api.patch(`/user-service/api/v1/students/${studentId}/unassign`);
}   

export const getStudentInfo = async (studentId: number): Promise<StudentInfoResponse> => {
    const { data } = await api.get<StudentInfoResponse>(`/user-service/api/v1/students/${studentId}/info`);
    return data;
}

export const getStudentDetails = async (id: number): Promise<StudentDetailsResponse> => {
    const { data } = await api.get(`/user-service/api/v1/students/${id}/details`);
    return data;
};