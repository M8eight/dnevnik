import api from "@/axios/axios";
import type { ParentDetailsResponse, UserResponse } from "./user-service";

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
    const { data } = await api.get<ParentInfoResponse>(`/user-service/api/v1/parent/${parentId}/info`);
    return data;
}

export const getParentDetails = async (id: number): Promise<ParentDetailsResponse> => {
    const { data } = await api.get(`/user-service/api/v1/parents/${id}/details`);
    return data;
};