import type { PageResponse } from "@/helpers/helper-interfaces";
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
    attendanceStudentStatus: {
        absenceCount: number;        
        lateCount: number;
        lessonsCount: number;
        presencePercent: number;
    };
    periodAverage: number;
    studyProfile?: string;
    parent?: UserResponse;
    schoolClass?: SchoolClassResponse;
    classTeacher?: {
        user: UserResponse;
        details: TeacherDetails;
    };
}

export interface StudentWithParentDto {
    id: number;
    firstName: string;
    lastName: string;
    username: string;
    keycloakId: string;
    parent?: UserResponse;
}

export const getStudentFullDetails = async (): Promise<StudentFullDetailsResponse> => {
    const { data } = await api.get<StudentFullDetailsResponse>(`/user-service/api/v1/students/with-class`);
    return data;
}

export const getStudentInfo = async (studentId: number): Promise<StudentInfoResponse> => {
    const { data } = await api.get<StudentInfoResponse>(`/user-service/api/v1/students/${studentId}/info`);
    return data;
}

export const getStudentDetails = async (id: number): Promise<StudentDetailsResponse> => {
    const { data } = await api.get(`/user-service/api/v1/students/${id}/details`);
    return data;
};

export const getUnassignedToParentStudents = async (
    page: number,
    size: number,
    search?: string
): Promise<PageResponse<UserResponse>> => {
    const { data } = await api.get<PageResponse<UserResponse>>(`/user-service/api/v1/students/unasigned-to-parent`,
        { params: { page, size, search } }
    );
    return data;
}

export const getStudentWithParent = async (id: number): Promise<StudentWithParentDto> => {
    const { data } = await api.get(`/user-service/api/v1/students/${id}/with-parent`);
    return data;
};

export const getStudentsByParentId = async (): Promise<UserResponse[]> => {
    const { data } = await api.get(`/user-service/api/v1/students/by-parent`);
    return data;
};

export const assignStudentToParent = async (studentId: number, parentId: number): Promise<void> => {
    await api.patch(`/user-service/api/v1/students/${studentId}/assign/${parentId}`);
}

export const unassignStudentFromParent = async (studentId: number): Promise<void> => {
    await api.patch(`/user-service/api/v1/students/${studentId}/unassign`);
}   

