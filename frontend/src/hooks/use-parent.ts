import { type ParentInfoResponse, type ParentResponse, getParent, getParentInfo } from "@/services/parent-service"
import { useQuery } from "@tanstack/react-query"


const QUERY_KEY = ["parent"];

export const useParent = (id: number) => {
    return useQuery<ParentResponse>({
        queryKey: ['parent', id],
        queryFn: () => getParent(id),
        enabled: !!id,
    })
}

export const useParentInfo = (id: number) =>
    useQuery<ParentInfoResponse>({
        queryKey: [QUERY_KEY, id],
        queryFn: () => getParentInfo(id),
    });