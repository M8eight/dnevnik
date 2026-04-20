import { findAcademicPeriods, type AcademicPeriodResponse } from "@/services/academic-period-service"
import { useQuery } from "@tanstack/react-query"


export const useAcademicPeriods = () => {
    return useQuery<AcademicPeriodResponse[]>({
        queryKey: [],
        queryFn: () => findAcademicPeriods(),
    })
}
