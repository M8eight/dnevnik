import { closeAcademicYear, createAcademicYear, deleteAcademicYear, getAcademicYears, openAcademicYear, type AcademicYearRequest, type AcademicYearResponse } from "@/services/academic-year-service";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";


const QUERY_KEY = ["academic-years"];

export const useGetAcademicYears = () => {
    return useQuery<AcademicYearResponse[]>({
        queryKey: QUERY_KEY,
        queryFn: getAcademicYears,
    });
};

export const useCreateAcademicYear = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: AcademicYearRequest) => createAcademicYear(request),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
    });
};

export const useOpenAcademicYear = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => openAcademicYear(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
    });
};
    
export const useCloseAcademicYear = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => closeAcademicYear(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
    });
};

export const useDeleteAcademicYear = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => deleteAcademicYear(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
    });
};
