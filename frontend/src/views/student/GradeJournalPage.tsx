import { useState, useMemo } from "react";
import { Button } from "@/components/ui/button";
import {
    Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import { Download, CalendarDays, CalendarClock } from "lucide-react";
import { cn } from "@/lib/utils";
import { useGetAcademicPeriodsByAcademicYear } from "@/hooks/use-academic-period";
import StudentNavbar from "@/components/layout/navbars/StudentNavbar";
import GradeJournalPeriodGradeTab from "./GradeJournalPeriodGradeTab";
import GradeJournalGradeTab from "./GradeJournalGradeTab";
import { useGetAcademicYears } from "@/hooks/use-academic-year";

type Tab = "current" | "period";

function TabSwitcher({ active, onChange }: { active: Tab; onChange: (t: Tab) => void }) {
    const tabs = [
        { id: "current" as Tab, label: "Текущие оценки" },
        { id: "period" as Tab, label: "Итоговые оценки" },
    ];

    return (
        <div className="flex items-center gap-1 glass-pill rounded-2xl p-1 w-fit">
            {tabs.map((tab) => (
                <button
                    key={tab.id}
                    onClick={() => onChange(tab.id)}
                    className={cn(
                        "px-5 h-9 rounded-xl text-[12px] font-extrabold uppercase tracking-wider transition-all duration-200",
                        active === tab.id
                            ? "bg-white/70 text-(--navy) shadow-sm"
                            : "text-black/30 hover:text-(--navy) hover:bg-white/20"
                    )}
                >
                    {tab.label}
                </button>
            ))}
        </div>
    );
}

export default function GradeTablePage() {
    const [activeTab, setActiveTab] = useState<Tab>("current");

    const { data: academicYears } = useGetAcademicYears();
    const [selectedAcademicYearId, setSelectedAcademicYearId] = useState<string>("");

    const defaultAcademicYearId = useMemo(() => {
        if (!academicYears?.length) return "";
        return academicYears[0].id.toString();
    }, [academicYears]);

    const resolvedAcademicYearId = selectedAcademicYearId || defaultAcademicYearId;

    const { data: periods } = useGetAcademicPeriodsByAcademicYear(parseInt(resolvedAcademicYearId, 10));
    const [selectedPeriodId, setSelectedPeriodId] = useState<string>("");

    const defaultPeriodId = useMemo(() => {
        if (!periods?.length) return "";
        const activePeriod = periods.find((p) => !p.isClosed) ?? periods[periods.length - 1];
        return activePeriod.id.toString();
    }, [periods]);

    const resolvedPeriodId = selectedPeriodId || defaultPeriodId;
    const academicPeriodId = resolvedPeriodId ? parseInt(resolvedPeriodId, 10) : 0;
    const academicYearIdNumber = resolvedAcademicYearId ? parseInt(resolvedAcademicYearId, 10) : 0;

    return (
        <div className="relative z-10 min-h-screen px-6 md:px-10 pt-2 pb-14 max-w-400 mx-auto">

            <StudentNavbar />

            <header className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-8 pb-6 border-b border-black/8 anim-in">
                <div>
                    <p className="text-[10px] font-extrabold tracking-[0.25em] text-(--red) uppercase mb-2 flex items-center gap-2">
                        <span className="inline-block w-4 h-0.5 bg-(--red) rounded-full" />
                        Учебный процесс
                    </p>
                    <h1 className="font-serif font-black text-[clamp(2rem,4vw,3rem)] text-(--navy) leading-[0.95]">
                        Успеваемость{" "}
                        <em className="not-italic relative">
                            <span className="relative z-10 text-(--red)">ученика</span>
                            <span className="absolute bottom-0 left-0 right-0 h-1.25 rounded-full opacity-15 bg-(--red)" />
                        </em>
                    </h1>
                </div>

                <div className="flex gap-3 items-center flex-wrap">

                    <Select
                        value={resolvedAcademicYearId}
                        onValueChange={setSelectedAcademicYearId}
                    >
                        <SelectTrigger className="glass-pill h-10 px-5 text-[12px] font-bold rounded-2xl text-(--navy) border-0 shadow-sm gap-2 min-w-45">
                            <CalendarClock className="w-4 h-4 text-(--red)" />
                            <SelectValue placeholder="Выберите год" />
                        </SelectTrigger>
                        <SelectContent className="rounded-2xl border-none shadow-2xl bg-white/95 backdrop-blur-xl max-h-87.5">
                            {academicYears?.map((academicYear) => (
                                <SelectItem key={academicYear.id} value={academicYear.id.toString()} className="font-bold text-[13px] py-3 rounded-xl cursor-pointer">
                                    {academicYear.name}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>

                    {activeTab === "current" && (
                        <Select value={resolvedPeriodId} onValueChange={setSelectedPeriodId}>
                            <SelectTrigger className="glass-pill w-60 h-11 font-bold text-[13px] rounded-2xl text-(--navy) px-4 border-0 shadow-none">
                                <div className="flex items-center gap-2">
                                    <CalendarDays className="w-4 h-4 text-(--red) shrink-0" />
                                    <SelectValue placeholder="Выберите четверть" />
                                </div>
                            </SelectTrigger>
                            <SelectContent className="rounded-2xl border border-white/60 shadow-2xl p-1 bg-white/90 backdrop-blur-2xl">
                                {periods?.map((p) => (
                                    <SelectItem
                                        key={p.id}
                                        value={p.id.toString()}
                                        className="font-bold text-[13px] text-(--navy) py-2.5 px-3 rounded-xl cursor-pointer"
                                    >
                                        {p.name}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    )}

                    <Button
                        variant="outline"
                        className="glass-pill h-11 border-0 text-[11px] font-extrabold uppercase tracking-widest rounded-2xl text-(--navy) px-5 hover:scale-[1.02] transition-transform"
                    >
                        <Download className="mr-2 h-4 w-4 text-(--red)" /> PDF
                    </Button>

                </div>
            </header>

            {/* Tab switcher */}
            <div className="mb-6 anim-in anim-delay-1">
                <TabSwitcher active={activeTab} onChange={setActiveTab} />
            </div>

            {activeTab === "current" && academicPeriodId > 0 && (
                <GradeJournalGradeTab academicPeriodId={academicPeriodId} />
            )}
            {activeTab === "period" && academicYearIdNumber > 0 && (
                <GradeJournalPeriodGradeTab academicYearId={academicYearIdNumber} />
            )}

            {/* Легенда */}
            <div className="mt-6 flex flex-wrap gap-6 anim-in anim-delay-5">
                {[
                    { color: "#16a34a", bg: "#f0fdf4", label: "Отлично (5)" },
                    { color: "#d97706", bg: "#fffbeb", label: "Хорошо (4)" },
                    { color: "#ea580c", bg: "#fff7ed", label: "Удовл. (3)" },
                    { color: "#dc2626", bg: "#fef2f2", label: "Неудовл. (2)" },
                ].map((item) => (
                    <div
                        key={item.label}
                        className="flex items-center gap-2 text-[9px] font-extrabold uppercase tracking-[0.22em] text-black/35"
                    >
                        <span
                            className="w-5 h-5 rounded-[6px] ring-1 ring-black/6 flex items-center justify-center shrink-0"
                            style={{ background: item.bg }}
                        >
                            <span
                                className="font-serif font-black text-[11px]"
                                style={{ color: item.color }}
                            >
                                {item.label[item.label.indexOf("(") + 1]}
                            </span>
                        </span>
                        {item.label}
                    </div>
                ))}
            </div>
        </div>
    );
}