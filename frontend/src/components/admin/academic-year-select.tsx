import { CalendarClock } from "lucide-react";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { useGetAcademicYears } from "@/hooks/use-academic-year";

interface AcademicYearSelectProps {
    value: string;
    onChange: (value: string) => void;
}

export default function AcademicYearSelect({ value, onChange }: AcademicYearSelectProps) {
    const { data: academicYears } = useGetAcademicYears();

    return (
        <Select value={value} onValueChange={onChange}>
            <SelectTrigger className="glass-pill h-10 px-5 text-[12px] font-bold rounded-2xl text-(--navy) border-0 shadow-sm gap-2 min-w-45">
                <CalendarClock className="w-4 h-4 text-(--red)" />
                <SelectValue placeholder="Выберите год" />
            </SelectTrigger>
            <SelectContent className="rounded-2xl border-none shadow-2xl bg-white/95 backdrop-blur-xl max-h-87.5">
                {academicYears?.map((academicYear) => (
                    <SelectItem key={academicYear.id} value={academicYear.id.toString()} className="font-bold text-[13px] py-3 rounded-xl cursor-pointer">
                        {academicYear.name} {academicYear.closed && "(Архив)"}
                    </SelectItem>
                ))}
            </SelectContent>
        </Select>
    );
}
