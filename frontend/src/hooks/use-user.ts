import { useQuery } from "@tanstack/react-query"
import { getUser, type User } from "../services/user-service"


export const useUser = (id: number) => {
    return useQuery<User>({
        queryKey: ['user', id],
        queryFn: () => getUser(id),
        enabled: !!id,
    })
}