import api from "../axios/axios";

export const getStudentReport = async (periodId: number) => {
    return api.get<Blob>(`/academic-service/api/v1/pdf/student/grade-report/report?periodId=${periodId}`,
        { responseType: 'blob' }
    );
}

export const getStudentPeriodFinalReport = async (academicYearId: number) => {
    return api.get<Blob>(`/academic-service/api/v1/pdf/student/grade-period-report/report?studentId=27&academicYearId=${academicYearId}`,
        { responseType: 'blob' }
    );
}

export const getTeacherReport = async ({ teachingAssignmentId, periodId }: { teachingAssignmentId: number; periodId: number }) => {
    return api.get<Blob>(`/academic-service/api/v1/pdf/teacher/student-grade-report/report?teachingAssignmentId=${teachingAssignmentId}&periodId=${periodId}`,
        { responseType: 'blob' }
    );
}