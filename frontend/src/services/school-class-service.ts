import api from "@/axios/axios";
import type { PageResponse } from "@/helpers/helper-interfaces";
import type { TeacherDetails, UserResponse, UserSimpleResponse } from "./user-service";

export interface SchoolClassResponse {
    id: number;
    name: string;
    year: string;
    classTeacherId: number;
}
export interface SchoolClassRequest {
    name: string;
    year: string;
    classTeacherId: number;
}


export interface SchoolClassFullResponse {
    id: number;
    name: string;
    year: string;
    teacher: {
        user: UserSimpleResponse;
        teacherDetails: TeacherDetails
    }
    students: UserSimpleResponse[];
}

export const getSchoolClasses = async (page: number, size: number): Promise<PageResponse<SchoolClassResponse>> => {
    const { data } = await api.get<PageResponse<SchoolClassResponse>>(`/academic-service/api/v1/school-classes?page=${page}&size=${size}`);
    return data;
};

export const getSchoolClassDetails = async (classId: number): Promise<SchoolClassFullResponse> => {
    const { data } = await api.get<SchoolClassFullResponse>(`/academic-service/api/v1/school-classes/${classId}/details`);
    return data;
};

export const getAllUnassignedStudents = async (): Promise<UserSimpleResponse[]> => {
    const { data } = await api.get<UserSimpleResponse[]>(`/academic-service/api/v1/school-classes/unassigned`);
    return data;
};

export const assignTeacherToClass = async (classId: number, teacherId: number): Promise<void> => {
    await api.patch(`/academic-service/api/v1/school-classes/${classId}/assign-teacher/${teacherId}`);
};

export const addStudentToClass = async (classId: number, studentId: number): Promise<void> => {
    await api.patch(`/academic-service/api/v1/school-classes/${classId}/add/${studentId}`);
};

export const removeStudentFromClass = async (classId: number, studentId: number): Promise<void> => {
    await api.patch(`/academic-service/api/v1/school-classes/${classId}/remove/${studentId}`);
};

export const createClass = async (schoolClassReq: SchoolClassRequest): Promise<SchoolClassResponse> => {
    const { data } = await api.post<SchoolClassResponse>(`/academic-service/api/v1/school-classes`, schoolClassReq);
    return data;
};

export const updateClass = async (id: number, schoolClassReq: Partial<SchoolClassRequest>): Promise<void> => {
    await api.patch(`/academic-service/api/v1/school-classes/${id}`, schoolClassReq);
};

export const deleteClass = async (id: number): Promise<void> => {
    await api.delete(`/academic-service/api/v1/school-classes/${id}`);
};
