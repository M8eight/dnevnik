import api from "../axios/axios";

export interface StudentDetailsResponse {
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
        id: number;
        userId: number;
        keycloakId: string;
        firstName: string;
        lastName: string;
        phoneNumber: string;
        email: string;
    }
}


export const getStudentDetails = async (studentId: number): Promise<StudentDetailsResponse> => {
    const {data} = await api.get<StudentDetailsResponse>(`/user-service/api/v1/students/${studentId}`);
    return data;
}

export const assignStudentToParent = async (studentId: number, parentId: number): Promise<void> => {
    await api.patch(`/user-service/api/v1/students/${studentId}/assign/${parentId}`);
}

export const unassignStudentFromParent = async (studentId: number): Promise<void> => {
    await api.patch(`/user-service/api/v1/students/${studentId}/unassign`);
}   