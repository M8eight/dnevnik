import { X, MapPin, BookOpen, User, Users, Calendar, School } from "lucide-react";
import { useScheduleDetails } from "@/hooks/use-schedule";
import { formatRuMonthDay } from "@/lib/date";
import { Loader2 } from "lucide-react";

interface ScheduleDetailModalProps {
    scheduleId: number | null;
    onClose: () => void;
}

export default function ScheduleDetailModal({ scheduleId, onClose }: ScheduleDetailModalProps) {
    const { data: details, isLoading } = useScheduleDetails(scheduleId ?? 0);

    if (!scheduleId) return null;

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-in fade-in duration-200"
            style={{ background: "rgba(15,20,40,0.25)", backdropFilter: "blur(8px)" }}
            onClick={onClose}
        >
            <div
                className="glass-card w-full max-w-md rounded-[36px] p-0 overflow-hidden shadow-2xl backdrop-blur-xl animate-in slide-in-from-bottom-4 fade-in duration-300"
                style={{ boxShadow: "0 32px 80px rgba(15,20,60,0.12), 0 0 0 1px rgba(255,255,255,0.5)" }}
                onClick={(e) => e.stopPropagation()}
            >
                {/* Шапка */}
                <div className="px-8 pt-7 pb-5 flex items-center justify-between border-b border-black/5">
                    <div className="flex items-center gap-3">
                        <div className="w-11 h-11 rounded-[14px] bg-(--red-light)/60 flex items-center justify-center ring-1 ring-(--red)/10">
                            <BookOpen className="w-5 h-5 text-(--red)" />
                        </div>
                        <div>
                            <p className="font-black text-(--navy) text-lg leading-none">
                                Детализация урока
                            </p>
                        </div>
                    </div>
                    <button
                        onClick={onClose}
                        className="w-9 h-9 rounded-xl bg-black/5 hover:bg-black/10 flex items-center justify-center text-black/30 hover:text-black/60 transition-all cursor-pointer"
                    >
                        <X className="w-4 h-4" />
                    </button>
                </div>

                <div className="px-8 py-6">
                    {isLoading ? (
                        <div className="flex items-center justify-center py-12">
                            <Loader2 className="w-6 h-6 animate-spin text-(--red)" />
                        </div>
                    ) : details ? (
                        <div className="space-y-4">
                            {/* Предмет */}
                            <div className="flex items-center gap-3 p-3 rounded-2xl bg-white/40 border border-white/60">
                                <div className="w-9 h-9 rounded-xl bg-(--red-light)/60 flex items-center justify-center ring-1 ring-(--red)/10 shrink-0">
                                    <BookOpen className="w-4 h-4 text-(--red)" />
                                </div>
                                <div>
                                    <p className="text-[10px] font-black uppercase tracking-widest text-black/30">Предмет</p>
                                    <p className="text-sm font-black text-(--navy)">{details.subject.name}</p>
                                </div>
                            </div>

                            {/* Учитель */}
                            <div className="flex items-center gap-3 p-3 rounded-2xl bg-white/40 border border-white/60">
                                <div className="w-9 h-9 rounded-xl bg-blue-50 flex items-center justify-center ring-1 ring-blue-200 shrink-0">
                                    <User className="w-4 h-4 text-blue-500" />
                                </div>
                                <div>
                                    <p className="text-[10px] font-black uppercase tracking-widest text-black/30">Преподаватель</p>
                                    <p className="text-sm font-black text-(--navy)">
                                        {details.teacher.lastName} {details.teacher.firstName} {details.teacher.patronymic ?? ""}
                                    </p>
                                </div>
                            </div>

                            {/* Кабинет */}
                            <div className="flex items-center gap-3 p-3 rounded-2xl bg-white/40 border border-white/60">
                                <div className="w-9 h-9 rounded-xl bg-green-50 flex items-center justify-center ring-1 ring-green-200 shrink-0">
                                    <MapPin className="w-4 h-4 text-green-500" />
                                </div>
                                <div>
                                    <p className="text-[10px] font-black uppercase tracking-widest text-black/30">Кабинет</p>
                                    <p className="text-sm font-black text-(--navy)">{details.classRoom}</p>
                                </div>
                            </div>

                            {/* Класс */}
                            <div className="flex items-center gap-3 p-3 rounded-2xl bg-white/40 border border-white/60">
                                <div className="w-9 h-9 rounded-xl bg-purple-50 flex items-center justify-center ring-1 ring-purple-200 shrink-0">
                                    <School className="w-4 h-4 text-purple-500" />
                                </div>
                                <div>
                                    <p className="text-[10px] font-black uppercase tracking-widest text-black/30">Класс</p>
                                    <p className="text-sm font-black text-(--navy)">{details.schoolClass.name}</p>
                                </div>
                            </div>

                            {/* Группа */}
                            {details.classGroup && (
                                <div className="flex items-center gap-3 p-3 rounded-2xl bg-white/40 border border-white/60">
                                    <div className="w-9 h-9 rounded-xl bg-orange-50 flex items-center justify-center ring-1 ring-orange-200 shrink-0">
                                        <Users className="w-4 h-4 text-orange-500" />
                                    </div>
                                    <div>
                                        <p className="text-[10px] font-black uppercase tracking-widest text-black/30">Группа</p>
                                        <p className="text-sm font-black text-(--navy)">{details.classGroup.name}</p>
                                    </div>
                                </div>
                            )}

                            {/* Действует с/по */}
                            <div className="flex items-center gap-3 p-3 rounded-2xl bg-white/40 border border-white/60">
                                <div className="w-9 h-9 rounded-xl bg-yellow-50 flex items-center justify-center ring-1 ring-yellow-200 shrink-0">
                                    <Calendar className="w-4 h-4 text-yellow-600" />
                                </div>
                                <div>
                                    <p className="text-[10px] font-black uppercase tracking-widest text-black/30">Действует</p>
                                    <p className="text-sm font-black text-(--navy)">
                                        {formatRuMonthDay(details.validFrom)}
                                        {details.validTo ? ` — ${formatRuMonthDay(details.validTo)}` : " — бессрочно"}
                                    </p>
                                </div>
                            </div>
                        </div>
                    ) : null}
                </div>
            </div>
        </div>
    );
}
