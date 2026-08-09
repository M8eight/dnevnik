import { useMemo, useState } from "react";
import { useGetAcademicYears } from "@/hooks/use-academic-year";

export const useAcademicYearSelection = () => {
    const { data: academicYears } = useGetAcademicYears();
    const [selectedAcademicYearId, setSelectedAcademicYearId] = useState<string>("");

    const defaultAcademicYearId = useMemo(() => {
        if (!academicYears?.length) return "";
        return academicYears[0].id.toString();
    }, [academicYears]);

    const resolvedAcademicYearId = selectedAcademicYearId || defaultAcademicYearId;

    const currentAcademicYear = useMemo(() => {
        return academicYears?.find((year) => year.id.toString() === resolvedAcademicYearId);
    }, [academicYears, resolvedAcademicYearId]);

    const isYearClosed = currentAcademicYear ? currentAcademicYear.closed : false;

    return {
        academicYears,
        selectedAcademicYearId,
        setSelectedAcademicYearId,
        resolvedAcademicYearId,
        currentAcademicYear,
        isYearClosed,
    };
};
