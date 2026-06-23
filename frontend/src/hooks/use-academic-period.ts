import {
    findAcademicPeriods,
    createAcademicPeriod,
    openAcademicPeriod,
    closeAcademicPeriod,
    deleteAcademicPeriod,
    type AcademicPeriodRequest,
    type AcademicPeriodResponse,
    findAcademicPeriodsByAcademicYear,
    updateAcademicPeriod,
    type AcademicPeriodUpdateRequest,
} from "@/services/academic-period-service";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";

const QUERY_KEY = ["academic-periods"];

export const useGetAcademicPeriods = () => {
    return useQuery<AcademicPeriodResponse[]>({
        queryKey: QUERY_KEY,
        queryFn: findAcademicPeriods,
    });
};

export const useGetAcademicPeriodsByAcademicYear = (academicYearId: number) => {
    return useQuery<AcademicPeriodResponse[]>({
        queryKey: [...QUERY_KEY, academicYearId],
        queryFn: () => findAcademicPeriodsByAcademicYear(academicYearId),
        enabled: !!academicYearId,
    });
};

export const useCreateAcademicPeriod = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: AcademicPeriodRequest) => createAcademicPeriod(request),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
    });
};

export const useUpdateAcademicPeriod = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, request }: { id: number; request: AcademicPeriodUpdateRequest }) =>
            updateAcademicPeriod(id, request),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
    });
};

export const useOpenAcademicPeriod = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => openAcademicPeriod(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
    });
};

export const useCloseAcademicPeriod = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => closeAcademicPeriod(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
    });
};

export const useDeleteAcademicPeriod = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => deleteAcademicPeriod(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
    });
};