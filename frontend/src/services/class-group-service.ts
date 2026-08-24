import api from "@/axios/axios"
import type { BatchUserResponse, UserSimpleResponse } from "./user-service"

export interface ClassGroupResponse {
    id: number,
    name: string
}

export interface ClassGroupWithCountResponse {
    id: number,
    name: string
    studentCount: number
}

export interface ClassGroupDetails {
    id: number,
    name: string
    students: BatchUserResponse
}

export interface ClassGroupRequest {
    name: string,
    schoolClassId: number
}

export const getAllClassGroups = async (): Promise<ClassGroupWithCountResponse[]> => {
    const {data} = await api.get<ClassGroupWithCountResponse[]>(`/academic-service/api/v1/class-groups`);
    return data;
};

export const getAllClassGroupsBySchoolClass = async (schoolClassId: number): Promise<ClassGroupResponse[]> => {
    const {data} = await api.get<ClassGroupResponse[]>(`/academic-service/api/v1/class-groups/by-school-class/${schoolClassId}`);
    return data;
};

export const getClassGroupDetails = async (schoolClassId: number): Promise<ClassGroupDetails> => {
    const {data} = await api.get<ClassGroupDetails>(`/academic-service/api/v1/class-groups/${schoolClassId}`);
    return data;
};

export const getUnassignedStudentsByClassGroup = async (classGroupId: number): Promise<UserSimpleResponse[]> => {
    const {data} = await api.get<UserSimpleResponse[]>(`/academic-service/api/v1/class-groups/${classGroupId}/unassigned-students`);
    return data;
};

export const createClassGroup = async (request: ClassGroupRequest): Promise<ClassGroupResponse> => {
    const { data } = await api.post<ClassGroupResponse>(
        `/academic-service/api/v1/class-groups`,
        request
    );
    return data;
};

export const deleteClassGroup = async (classGroupId: number): Promise<void> => {
    const { data } = await api.delete<void>(
        `/academic-service/api/v1/class-groups/${classGroupId}`
    );
    return data;
};

export const updateClassGroup = async (classGroupId: number, name: string): Promise<void> => {
    const { data } = await api.patch<void>(
        `/academic-service/api/v1/class-groups/${classGroupId}`, {
            params: {
                name
            }
        }
    );
    return data;
};

export const addStudentToClassGroup = async (classGroupId: number, studentId: number): Promise<void> => {
    const { data } = await api.post<void>(
        `/academic-service/api/v1/class-groups/${classGroupId}/students/${studentId}`
    );
    return data;
};

export const removeStudentToClassGroup = async (classGroupId: number, studentId: number): Promise<void> => {
    const { data } = await api.delete<void>(
        `/academic-service/api/v1/class-groups/${classGroupId}/students/${studentId}`
    );
    return data;
};
