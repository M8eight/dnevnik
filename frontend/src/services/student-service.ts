import api from "../axios/axios";
import type { TeacherDetails, UserResponse } from "./user-service";

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


export const getStudentFullDetails = async (studentId: number): Promise<StudentFullDetailsResponse> => {
    const { data } = await api.get<StudentFullDetailsResponse>(`/user-service/api/v1/students/${studentId}/with-class`);
    return data;
}

export const assignStudentToParent = async (studentId: number, parentId: number): Promise<void> => {
    await api.patch(`/user-service/api/v1/students/${studentId}/assign/${parentId}`);
}

export const unassignStudentFromParent = async (studentId: number): Promise<void> => {
    await api.patch(`/user-service/api/v1/students/${studentId}/unassign`);
}   