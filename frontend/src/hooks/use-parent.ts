import type { PageResponse } from "@/helpers/helper-interfaces";
import {
    type ParentInfoResponse,
    type ParentResponse,
    getParent,
    getParentInfo,
    getUnassignedToStudentParents,
} from "@/services/parent-service";
import type { UserResponse } from "@/services/user-service";
import { useInfiniteQuery, useQuery } from "@tanstack/react-query";

const QUERY_KEY = "parent";

export const useParent = (id: number) => {
    return useQuery<ParentResponse>({
        queryKey: [QUERY_KEY, id],
        queryFn: () => getParent(id),
        enabled: !!id,
    });
};

export const useParentInfo = (id: number) =>
    useQuery<ParentInfoResponse>({
        queryKey: [QUERY_KEY, "info", id],
        queryFn: () => getParentInfo(id),
        enabled: !!id,
    });

export const useUnassignedToStudentParents = (
    size: number,
    searchName?: string
) => {
    return useInfiniteQuery<PageResponse<UserResponse>>({
        queryKey: [QUERY_KEY, "unassignedToStudent", { size, searchName }],
        initialPageParam: 0,
        queryFn: ({ pageParam }) =>
            getUnassignedToStudentParents(pageParam as number, size, searchName),
        getNextPageParam: (lastPage) => {
            if (lastPage.last) return undefined;
            return lastPage.number + 1;
        },
    });
};