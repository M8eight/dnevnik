import type { PageResponse } from "@/helpers/helper-interfaces";
import api from "../axios/axios";


// ─── User create types ────────────────────────────────────────────────────────────────────

export type UserRole = "STUDENT" | "PARENT" | "TEACHER";

export interface UserBase {
    username: string;
    password: string;
    firstName: string;
    lastName: string;
}

export interface StudentDetails {
    studentDetails: string;
}

export interface TeacherDetails {
    email: string;
    phoneNumber: string;
}

export interface ParentDetails {}

export interface CreateStudentRequest {
    user: UserBase;
    role: "STUDENT";
    details: StudentDetails;
}

export interface CreateParentRequest {
    user: UserBase;
    role: "PARENT";
    details: ParentDetails;
}

export interface CreateTeacherRequest {
    user: UserBase;
    role: "TEACHER";
    details: TeacherDetails;
}

export type CreateUserRequest =
    | CreateStudentRequest
    | CreateParentRequest
    | CreateTeacherRequest;

export interface UserResponse {
    id: number;
    username: string;
    firstName: string;
    lastName: string;
    keycloakId: string;
    roles: UserRole[];
}

export interface UserSimpleResponse {
    id: number;
    username: string;
    firstName: string;
    lastName: string;
    keycloakId: string;
}

export interface StudentDetailsResponse {
    studyProfile: string;
}

export interface TeacherDetailsResponse {
    email: string;
    phoneNumber: string;
}

export interface ParentDetailsResponse {}

// ─── User update types ────────────────────────────────────────────────────────────────────

export interface UserUpdateData {
    username?: string;
    firstName?: string;
    lastName?: string;
}

export type UserProfileDetails = 
    | {studyProfile: string} // For students
    | {email: string; phoneNumber: string} // For teachers
    | Record<string, never> // For parents

export interface UserUpdateRequest {
    userId: number;
    user: UserUpdateData;
    password?: string;
    roles?: UserRole[];
    details: Partial<Record<UserRole, UserProfileDetails>>;
}

// ─── User req ────────────────────────────────────────────────────────────────────

export const createStudent = async (
    request: CreateStudentRequest
): Promise<UserResponse> => {
    const { data } = await api.post<UserResponse>(
        `/user-service/api/v1/users/students`,
        request
    );
    return data;
};

export const createParent = async (
    request: CreateParentRequest
): Promise<UserResponse> => {
    const { data } = await api.post<UserResponse>(
        `/user-service/api/v1/users/parents`,
        request
    );
    return data;
};

export const createTeacher = async (
    request: CreateTeacherRequest
): Promise<UserResponse> => {
    const { data } = await api.post<UserResponse>(
        `/user-service/api/v1/users/teacher`,
        request
    );
    return data;
};

export const findUsersByFilter = async (
    page: number, 
    size: number,
    role?: UserRole,
    search?: string
): Promise<PageResponse<UserResponse>> => {
    const { data } = await api.get<PageResponse<UserResponse>>(`/user-service/api/v1/users`, 
        { params: { page, size, role, search } }
    );
    return data;
}

export const deleteUser = async (userId: number): Promise<void> => {
    await api.delete(`/user-service/api/v1/users/${userId}`);
}

export const updateUser = async (userId: number, request: UserUpdateRequest): Promise<UserResponse> => {
    const { data } = await api.put<UserResponse>(`/user-service/api/v1/users/${userId}`, request); 
    return data;
}

export const getStudentDetails = async (id: number): Promise<StudentDetailsResponse> => {
    const { data } = await api.get(`/user-service/api/v1/students/${id}/details`);
    return data;
};

export const getTeacherDetails = async (id: number): Promise<TeacherDetailsResponse> => {
    const { data } = await api.get(`/user-service/api/v1/teachers/${id}/details`);
    return data;
};

export const getParentDetails = async (id: number): Promise<ParentDetailsResponse> => {
    const { data } = await api.get(`/user-service/api/v1/parents/${id}/details`);
    return data;
};