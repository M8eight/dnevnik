import { getStudentPeriodFinalReport, getStudentReport, getTeacherReport } from "@/services/pdf-service";
import { useMutation } from "@tanstack/react-query";
import { downloadBlob, logDownloadError } from "@/lib/download";

export const useCreateStudentReport = () => {
    return useMutation({
        mutationFn: (periodId: number) => getStudentReport(periodId),
        onSuccess: (response) => downloadBlob(response, 'grade-report.pdf'),
        onError: logDownloadError,
    });
};

export const useCreateStudentPeriodFinalReport = () => {
    return useMutation({
        mutationFn: (academicYearId: number) => getStudentPeriodFinalReport(academicYearId),
        onSuccess: (response) => downloadBlob(response, 'grade-report.pdf'),
        onError: logDownloadError,
    });
};

export const useCreateTeacherReport = () => {
    return useMutation({
        mutationFn: ({ teachingAssignmentId, periodId }: { teachingAssignmentId: number; periodId: number }) => getTeacherReport({ teachingAssignmentId, periodId }),
        onSuccess: (response) => downloadBlob(response, 'grade-report.pdf'),
        onError: logDownloadError,
    });
};
