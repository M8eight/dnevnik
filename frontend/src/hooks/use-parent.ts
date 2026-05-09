import { type ParentResponse, getParent } from "@/services/parent-service"
import { useQuery } from "@tanstack/react-query"


export const useParent = (id: number) => {
    return useQuery<ParentResponse>({
        queryKey: ['parent', id],
        queryFn: () => getParent(id),
        enabled: !!id,
    })
}