import type { SchoolClassFullResponse } from "@/services/school-class-service";
import { AlertTriangle, GraduationCap, Mail, Phone, Users } from "lucide-react";
import { Avatar } from "../layout";

export function Skeleton({ className }: { className?: string }) {
    return <div className={`animate-pulse rounded-xl bg-black/8 ${className}`} />;
}

export function InfoRow({ icon: Icon, value }: { icon: React.ElementType; value: string }) {
    return (
        <div className="flex items-center gap-2.5 text-black/65">
            <div className="w-7 h-7 rounded-[10px] bg-(--red-light)/50 flex items-center justify-center shrink-0">
                <Icon className="w-3.5 h-3.5 text-(--red)" />
            </div>
            <span className="text-xs font-semibold">{value}</span>
        </div>
    );
}

export function SectionCard({ children, className = "" }: { children: React.ReactNode; className?: string }) {
    return (
        <div className={`glass-card rounded-[28px] p-5 border-none shadow-lg backdrop-blur-md ${className}`}>
            {children}
        </div>
    );
}

export function SectionTitle({
    icon: Icon,
    children,
    trailing,
}: {
    icon?: React.ElementType;
    children: React.ReactNode;
    trailing?: React.ReactNode;
}) {
    return (
        <div className="flex items-center justify-between mb-5">
            <h3 className="font-serif font-black text-base text-(--navy) tracking-tight flex items-center gap-2">
                {Icon && <Icon className="w-4 h-4 text-(--red)" />}
                {children}
            </h3>
            {trailing}
        </div>
    );
}

export function LeftColumnSkeleton() {
    return (
        <div className="lg:col-span-1 flex flex-col gap-6">
            <div className="glass-card rounded-[32px] p-6 flex flex-col items-center border-none shadow-lg backdrop-blur-md">
                <Skeleton className="w-28 h-28 rounded-[36px] mt-4 mb-4" />
                <Skeleton className="h-9 w-24 mb-5" />
                <div className="w-full space-y-2.5">
                    <Skeleton className="h-4 w-full" />
                    <Skeleton className="h-4 w-full" />
                    <Skeleton className="h-4 w-full" />
                </div>
            </div>
        </div>
    );
}

export function RightColumnSkeleton() {
    return (
        <>
            <Skeleton className="h-52 rounded-[28px]" />
            <Skeleton className="h-64 rounded-[28px]" />
        </>
    );
}

export function TeacherCard({
    teacher,
    classTeacherId,
}: {
    teacher:
    | {
        user: { id: number; username: string; firstName: string; lastName: string };
        details?: { email?: string; phoneNumber?: string };
        email?: string;
        phoneNumber?: string;
    }
    | null;
    classTeacherId: number;
}) {
    if (!teacher) {
        return (
            <SectionCard>
                <SectionTitle icon={GraduationCap}>Классный руководитель</SectionTitle>
                <p className="text-sm text-black/40 font-semibold">
                    Не удалось загрузить учителя (id: {classTeacherId})
                </p>
            </SectionCard>
        );
    }

    const { user, details, email, phoneNumber } = teacher;
    const teacherEmail = details?.email ?? email;
    const teacherPhone = details?.phoneNumber ?? phoneNumber;

    return (
        <SectionCard>
            <SectionTitle icon={GraduationCap}>Классный руководитель</SectionTitle>
            <div className="flex items-center gap-3 mb-5">
                <Avatar firstName={user.firstName} lastName={user.lastName} size="md" color="navy" />
                <div>
                    <p className="font-bold text-sm text-(--navy) leading-tight">
                        {user.firstName} {user.lastName}
                    </p>
                    <p className="text-xs text-black/40 font-medium mt-0.5">@{user.username}</p>
                </div>
            </div>
            {(teacherEmail || teacherPhone) && (
                <div className="border-t border-black/5 pt-4 space-y-2">
                    {teacherEmail && <InfoRow icon={Mail} value={teacherEmail} />}
                    {teacherPhone && <InfoRow icon={Phone} value={teacherPhone} />}
                </div>
            )}
        </SectionCard>
    );
}

export function StudentsSection({
    students,
    withLinks = true,
}: {
    students: {
        found: {
            id: number;
            username: string;
            firstName: string;
            lastName: string;
        }[];
        notFound: number[];
        degraded: boolean;
    };
    withLinks?: boolean;
}) {
    const { found, notFound, degraded } = students;

    return (
        <SectionCard>
            <SectionTitle
                icon={Users}
                trailing={
                    <span className="text-xs font-bold px-3 py-1 rounded-xl bg-(--navy)/8 text-(--navy)">
                        {found.length}
                    </span>
                }
            >
                Ученики
            </SectionTitle>

            {(degraded || notFound.length > 0) && (
                <div className="flex items-center gap-2.5 mb-4 p-3 rounded-[14px] bg-amber-50 text-amber-700">
                    <AlertTriangle className="w-4 h-4 shrink-0" />
                    <span className="text-xs font-semibold">
                        {degraded
                            ? "Не удалось получить полные данные по части учеников"
                            : `Не найдено учеников: ${notFound.length}`}
                    </span>
                </div>
            )}

            {found.length === 0 ? (
                <p className="text-sm text-black/40 font-semibold">
                    В классе пока нет учеников
                </p>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    {found.map((student) => {
                        const content = (
                            <div
                                key={student.id}
                                className="flex items-center gap-3 p-3 rounded-[16px] bg-black/3"
                            >
                                <Avatar
                                    firstName={student.firstName}
                                    lastName={student.lastName}
                                    size="sm"
                                    color="red"
                                />
                                <div className="min-w-0">
                                    <p className="font-bold text-sm text-(--navy) leading-tight truncate">
                                        {student.firstName} {student.lastName}
                                    </p>
                                    <p className="text-xs text-black/40 font-medium mt-0.5 truncate">
                                        @{student.username}
                                    </p>
                                </div>
                            </div>
                        );

                        return withLinks ? (
                            <a key={student.id} href={`/user/${student.id}/info`}>
                                {content}
                            </a>
                        ) : (
                            <div key={student.id}>{content}</div>
                        );
                    })}
                </div>
            )}
        </SectionCard>
    );
}

export interface SchoolClassDetailsLayoutProps {
    data: SchoolClassFullResponse;
}

export function SchoolClassCard({ data }: SchoolClassDetailsLayoutProps) {
    return (
        <div className="lg:col-span-1 flex flex-col gap-6">
            <div className="glass-card rounded-[32px] p-6 flex flex-col items-center text-center border-none shadow-lg backdrop-blur-md relative">
                <span className="absolute top-5 right-6 text-[11px] font-bold text-black/25">
                    #{data.id}
                </span>
                <div className="mt-4 mb-4 ring-2 ring-(--red)/15 rounded-[36px] w-28 h-28 bg-(--red-light)/50 flex items-center justify-center">
                    <span className="font-serif font-black text-3xl text-(--red)">{data.name}</span>
                </div>
                <div className="w-full">
                    <div className="flex items-center justify-center gap-2 mb-5">
                    </div>
                    <div className="space-y-2.5 text-left border-t border-black/5 pt-4">
                        <div className="flex items-center justify-between">
                            <span className="text-xs font-semibold text-black/35 uppercase tracking-wider">
                                Учеников
                            </span>
                            <span className="text-sm font-semibold text-black/55">
                                {data.students.found.length}
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}