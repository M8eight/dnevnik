import api from "@/axios/axios";
import type { TeacherDetails, UserSimpleResponse } from "./user-service";
import type { AcademicYearResponse } from "./academic-year-service";

export interface SchoolClassResponse {
    id: number;
    name: string;
    academicYear: AcademicYearResponse;
    classTeacherId: number;
}

export interface SchoolClassRequest {
    name: string;
    academicYearId: number;
    classTeacherId?: number;
}

export interface SchoolClassFullResponse {
    id: number;
    name: string;
    academicYear: AcademicYearResponse;
    teacher: {
        user: UserSimpleResponse;
        teacherDetails: TeacherDetails
    }
    students: UserSimpleResponse[];
}

export interface SchoolClassUpdateRequest {
    name: string;
}

export const getSchoolClasses = async (): Promise<SchoolClassResponse[]> => {
    const { data } = await api.get<SchoolClassResponse[]>(`/academic-service/api/v1/school-classes`);
    return data;
};

export const getSchoolClassesByAcademicYear = async (academicYearId: number): Promise<SchoolClassResponse[]> => {
    const { data } = await api.get<SchoolClassResponse[]>(`/academic-service/api/v1/school-classes/by-academic-year/${academicYearId}`);
    return data;
};

export const getSchoolClassDetails = async (classId: number): Promise<SchoolClassFullResponse> => {
    const { data } = await api.get<SchoolClassFullResponse>(`/academic-service/api/v1/school-classes/${classId}/details`);
    return data;
};

export const getAllUnassignedStudents = async (): Promise<UserSimpleResponse[]> => {
    const { data } = await api.get<UserSimpleResponse[]>(`/academic-service/api/v1/school-classes/unassigned-students`);
    return data;
};

export const assignTeacherToClass = async (classId: number, teacherId: number): Promise<void> => {
    await api.put(`/academic-service/api/v1/school-classes/${classId}/teacher/${teacherId}`);
};

export const addStudentToClass = async (classId: number, studentId: number): Promise<void> => {
    await api.post(`/academic-service/api/v1/school-classes/${classId}/students/${studentId}`);
};

export const removeStudentFromClass = async (classId: number, studentId: number): Promise<void> => {
    await api.delete(`/academic-service/api/v1/school-classes/${classId}/students/${studentId}`);
};

export const createClass = async (schoolClassReq: SchoolClassRequest): Promise<SchoolClassResponse> => {
    const { data } = await api.post<SchoolClassResponse>(`/academic-service/api/v1/school-classes`, schoolClassReq);
    return data;
};

export const updateClass = async (id: number, schoolClassReq: SchoolClassUpdateRequest): Promise<void> => {
    await api.patch(`/academic-service/api/v1/school-classes/${id}`, schoolClassReq);
};

export const deleteClass = async (id: number): Promise<void> => {
    await api.delete(`/academic-service/api/v1/school-classes/${id}`);
};
