import { useQuery } from "@tanstack/react-query"
import { getAvgGradeByStudentId } from "../services/grade-service"
import type { AvgGrade } from "@/services/grade-service"


export const useAvgGrade = (studentId: number, academicPeriodId: number) => {
    return useQuery<AvgGrade>({
        queryKey: ['avgGrade', studentId, academicPeriodId],
        queryFn: () => getAvgGradeByStudentId(studentId, academicPeriodId),
        enabled: !!studentId && !!academicPeriodId,
    })
}