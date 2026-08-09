import api from "@/axios/axios";
import type { UserResponse } from "./user-service";

export interface ParentResponse {
    user: UserResponse;
    children: UserResponse[];
}

export interface ParentInfoResponse {
    children?: UserResponse[];
}

export const getParent = async (parentId: number): Promise<ParentResponse> => {
    const {data} = await api.get<ParentResponse>(`/user-service/api/v1/parents/${parentId}`);
    return data;
}

export const getParentInfo = async (parentId: number): Promise<ParentInfoResponse> => {
    const { data } = await api.get<ParentInfoResponse>(`/user-service/api/v1/parents/${parentId}/info`);
    return data;
}