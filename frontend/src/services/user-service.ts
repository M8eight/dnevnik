import api from "../axios/axios";

export interface User {
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

export const getUser = async (studentId: number): Promise<User> => {
    const {data} = await api.get<User>(`/user-service/api/v1/students/${studentId}`);
    return data;
}