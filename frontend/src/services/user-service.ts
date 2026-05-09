import type { PageResponse } from "@/helpers/helper-interfaces";
import api from "../axios/axios";


// ─── Types ────────────────────────────────────────────────────────────────────

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