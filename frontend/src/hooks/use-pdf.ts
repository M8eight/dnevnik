import { getStudentPeriodFinalReport, getStudentReport, getTeacherReport } from "@/services/pdf-service";
import { useMutation } from "@tanstack/react-query";

export const useCreateStudentReport = () => {
    return useMutation({
        mutationFn: (periodId: number) => getStudentReport(periodId),
        onSuccess: (response) => {
            const blob = response.data;
            const contentDisposition = response.headers['content-disposition'];
            let fileName = 'grade-report.pdf';

            if (contentDisposition) {
                const fileNameMatch = contentDisposition.match(/filename="?([^";]+)"?/);
                if (fileNameMatch && fileNameMatch[1]) {
                    fileName = fileNameMatch[1];
                }
            }

            const fileURL = window.URL.createObjectURL(blob);
            const link = document.createElement("a");

            link.href = fileURL;
            link.setAttribute('download', fileName);

            document.body.appendChild(link);
            link.click();

            link.parentNode?.removeChild(link);
            window.URL.revokeObjectURL(fileURL);
        },
        onError: (error) => {
            console.error('Ошибка при генерации PDF:', error);
        }
    });
};

export const useCreateStudentPeriodFinalReport = () => {
    return useMutation({
        mutationFn: (academicYearId: number) => getStudentPeriodFinalReport(academicYearId),
        onSuccess: (response) => {
            const blob = response.data;
            const contentDisposition = response.headers['content-disposition'];
            let fileName = 'grade-report.pdf';

            if (contentDisposition) {
                const fileNameMatch = contentDisposition.match(/filename="?([^";]+)"?/);
                if (fileNameMatch && fileNameMatch[1]) {
                    fileName = fileNameMatch[1];
                }
            }

            const fileURL = window.URL.createObjectURL(blob);
            const link = document.createElement("a");

            link.href = fileURL;
            link.setAttribute('download', fileName);

            document.body.appendChild(link);
            link.click();

            link.parentNode?.removeChild(link);
            window.URL.revokeObjectURL(fileURL);
        },
        onError: (error) => {
            console.error('Ошибка при генерации PDF:', error);
        }
    });
};

export const useCreateTeacherReport = () => {
    return useMutation({
        mutationFn: ({ teachingAssignmentId, periodId }: { teachingAssignmentId: number; periodId: number }) => getTeacherReport({ teachingAssignmentId, periodId }),
        onSuccess: (response) => {
            const blob = response.data;
            const contentDisposition = response.headers['content-disposition'];
            let fileName = 'grade-report.pdf';

            if (contentDisposition) {
                const fileNameMatch = contentDisposition.match(/filename="?([^";]+)"?/);
                if (fileNameMatch && fileNameMatch[1]) {
                    fileName = fileNameMatch[1];
                }
            }

            const fileURL = window.URL.createObjectURL(blob);
            const link = document.createElement("a");

            link.href = fileURL;
            link.setAttribute('download', fileName);

            document.body.appendChild(link);
            link.click();

            link.parentNode?.removeChild(link);
            window.URL.revokeObjectURL(fileURL);
        },
        onError: (error) => {
            console.error('Ошибка при генерации PDF:', error);
        }
    });
};