import api from "../axios/axios";

export interface User {
    id: number;
    firstName: string;
    lastName: string;
    keycloackId: string;
}

export const getUser = async (userId: number): Promise<User> => {
    const {data} = await api.get<User>(`/user-service/api/v1/users/${userId}`);
    return data;
}