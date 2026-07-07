import { homeAggregation, type HomeAggregation } from "@/services/bff/student-bff-service";
import { useQuery } from "@tanstack/react-query";


export const useHomeAggregation = (date: string) => {
    return useQuery<HomeAggregation>({
        queryKey: ['homeAggregation', date],
        queryFn: () => homeAggregation(date),
        enabled: !!date,
    });
};
