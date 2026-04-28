import { createAttendance, deleteAttendance, type CreateAttendanceRequest, type CreateAttendanceResponse } from "@/services/attendance-service";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export const useCreateAttendance = () => {
    const queryClient = useQueryClient();

    return useMutation<CreateAttendanceResponse, Error, CreateAttendanceRequest>({
        mutationFn: createAttendance,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['teacherJournal'] });
        }
    });
};

export const useDeleteAttendance = () => {
    const queryClient = useQueryClient();

    return useMutation<void, Error, number>({
        mutationFn: deleteAttendance,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['teacherJournal'] });
        }
    });
};