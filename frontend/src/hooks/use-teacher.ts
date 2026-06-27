import { getTeacherDetails, getTeacherInfo, type TeacherInfoResponse } from "@/services/teacher-service";
import type { TeacherDetailsResponse } from "@/services/user-service";
import { useQuery } from "@tanstack/react-query";


const QUERY_KEY = ["teachers"];

export const useTeacherDetails = (id: number | null) =>
    useQuery<TeacherDetailsResponse>({
        queryKey: ["users", "details", "teacher", id],
        queryFn: () => getTeacherDetails(id!),
        enabled: id !== null,
    });

export const useTeacherInfo = (id: number) =>
    useQuery<TeacherInfoResponse>({
        queryKey: [QUERY_KEY, id],
        queryFn: () => getTeacherInfo(id),
    });