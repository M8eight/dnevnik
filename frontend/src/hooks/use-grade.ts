import { useQuery } from "@tanstack/react-query"
import { getAvgGradeByStudentId, findAllGradesByDate } from "../services/grade-service"
import type { AvgGrade, Grade } from "@/services/grade-service"


export const useAvgGrade = (studentId: number, academicPeriodId: number) => {
    return useQuery<AvgGrade>({
        queryKey: ['avgGrade', studentId, academicPeriodId],
        queryFn: () => getAvgGradeByStudentId(studentId, academicPeriodId),
        enabled: !!studentId && !!academicPeriodId,
    })
}

export const useGradesByDate = (studentId: number, date: string) => {
    return useQuery<Grade[]>({
        queryKey: ['gradesByDate', studentId, date],
        queryFn: () => findAllGradesByDate(studentId, date),
        enabled: !!studentId && !!date,
    })
}