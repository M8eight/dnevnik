import { useParentInfo } from "@/hooks/use-parent";
import { useStudentInfo } from "@/hooks/use-student";
import { useTeacherInfo } from "@/hooks/use-teacher";
import { useUserById } from "@/hooks/use-user";
import type { SchoolClassResponse } from "@/services/school-class-service";
import { User, GraduationCap, BookOpen, Users, Phone, Mail, ChevronLeft } from "lucide-react";
import { useParams } from "react-router-dom";


function getInitials(firstName: string, lastName: string) {
    return `${firstName[0]}${lastName[0]}`.toUpperCase();
}

function Skeleton({ className }: { className?: string }) {
    return <div className={`animate-pulse rounded-xl bg-black/8 ${className}`} />;
}

function InfoRow({ icon: Icon, value }: { icon: React.ElementType; value: string }) {
    return (
        <div className="flex items-center gap-2.5 text-black/65">
            <div className="w-7 h-7 rounded-[10px] bg-(--red-light)/50 flex items-center justify-center shrink-0">
                <Icon className="w-3.5 h-3.5 text-(--red)" />
            </div>
            <span className="text-xs font-semibold">{value}</span>
        </div>
    );
}

function Avatar({
    firstName,
    lastName,
    size = "md",
    color = "red",
}: {
    firstName: string;
    lastName: string;
    size?: "sm" | "md" | "lg";
    color?: "red" | "navy" | "green";
}) {
    const sizeMap = {
        sm: "w-11 h-11 rounded-[12px] text-sm",
        md: "w-14 h-14 rounded-[16px] text-base",
        lg: "w-28 h-28 rounded-[36px] text-3xl",
    };
    const colorMap = {
        red: "bg-(--red-light)/50 text-(--red)",
        navy: "bg-(--navy)/8 text-(--navy)",
        green: "bg-emerald-100/70 text-emerald-700",
    };
    return (
        <div className={`${sizeMap[size]} ${colorMap[color]} flex items-center justify-center shrink-0`}>
            <span className="font-serif font-black">{getInitials(firstName, lastName)}</span>
        </div>
    );
}

function SectionCard({ children, className = "" }: { children: React.ReactNode; className?: string }) {
    return (
        <div className={`glass-card rounded-[28px] p-5 border-none shadow-lg backdrop-blur-md ${className}`}>
            {children}
        </div>
    );
}

function SectionTitle({
    icon: Icon,
    children,
}: {
    icon?: React.ElementType;
    children: React.ReactNode;
}) {
    return (
        <h3 className="font-serif font-black text-base text-(--navy) tracking-tight flex items-center gap-2 mb-5">
            {Icon && <Icon className="w-4 h-4 text-(--red)" />}
            {children}
        </h3>
    );
}

function PersonCard({
    firstName,
    lastName,
    username,
    avatarColor = "navy",
    email,
    phone,
    title,
    icon,
}: {
    firstName: string;
    lastName: string;
    username: string;
    avatarColor?: "red" | "navy" | "green";
    email?: string;
    phone?: string;
    title: string;
    icon: React.ElementType;
}) {
    return (
        <SectionCard className="flex flex-col justify-between">
            <div>
                <SectionTitle icon={icon}>{title}</SectionTitle>
                <div className="flex items-center gap-3 mb-5">
                    <Avatar firstName={firstName} lastName={lastName} size="md" color={avatarColor} />
                    <div>
                        <p className="font-bold text-sm text-(--navy) leading-tight">
                            {firstName} <br /> {lastName}
                        </p>
                        <p className="text-xs text-black/40 font-medium mt-0.5">@{username}</p>
                    </div>
                </div>
            </div>
            {(email || phone) && (
                <div className="border-t border-black/5 pt-4 space-y-2">
                    {email && <InfoRow icon={Mail} value={email} />}
                    {phone && <InfoRow icon={Phone} value={phone} />}
                </div>
            )}
        </SectionCard>
    );
}

function StudentView({ userId }: { userId: number }) {
    const { data, isLoading } = useStudentInfo(userId);

    if (isLoading) return <RightColumnSkeleton />;
    if (!data) return null;

    const { studyProfile, parent, schoolClass, classTeacher } = data;

    return (
        <>
            {studyProfile && (
                <div className="glass-card rounded-[24px] p-4 flex items-center gap-4 border-none shadow-lg backdrop-blur-md">
                    <div className="w-12 h-12 rounded-[14px] bg-(--red-light)/40 flex items-center justify-center shrink-0">
                        <BookOpen className="w-5 h-5 text-(--red)" />
                    </div>
                    <div>
                        <span className="text-[10px] font-bold uppercase tracking-widest text-black/35 block">
                            Направление
                        </span>
                        <span className="font-serif font-black text-xl text-(--navy) tracking-tight block mt-0.5">
                            {studyProfile}
                        </span>
                    </div>
                </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {classTeacher && (
                    <PersonCard
                        firstName={classTeacher.user.firstName}
                        lastName={classTeacher.user.lastName}
                        username={classTeacher.user.username}
                        avatarColor="navy"
                        email={classTeacher.details.email}
                        phone={classTeacher.details.phoneNumber}
                        title="Классный руководитель"
                        icon={GraduationCap}
                    />
                )}
                {parent && (
                    <PersonCard
                        firstName={parent.firstName}
                        lastName={parent.lastName}
                        username={parent.username}
                        avatarColor="green"
                        title="Родитель"
                        icon={Users}
                    />
                )}
            </div>

            {schoolClass && (
                <SectionCard className="lg:hidden">
                    <ClassBlock schoolClass={schoolClass} />
                </SectionCard>
            )}
        </>
    );
}

function ParentView({ userId }: { userId: number }) {
    const { data, isLoading } = useParentInfo(userId);

    if (isLoading) return <RightColumnSkeleton />;
    if (!data?.children?.length) {
        return (
            <SectionCard className="flex items-center justify-center min-h-30">
                <p className="text-sm text-black/40 font-semibold">Нет привязанных детей</p>
            </SectionCard>
        );
    }

    return (
        <SectionCard>
            <SectionTitle icon={Users}>Дети</SectionTitle>
            <div className="space-y-3">
                {data.children.map((child) => (
                    <div key={child.id} className="flex items-center gap-3 p-3 rounded-[16px] bg-black/3">
                        <Avatar firstName={child.firstName} lastName={child.lastName} size="sm" color="red" />
                        <div>
                            <p className="font-bold text-sm text-(--navy) leading-tight">
                                {child.firstName} {child.lastName}
                            </p>
                            <p className="text-xs text-black/40 font-medium mt-0.5">@{child.username}</p>
                        </div>
                    </div>
                ))}
            </div>
        </SectionCard>
    );
}

function TeacherView({ userId }: { userId: number }) {
    const { data, isLoading } = useTeacherInfo(userId);

    if (isLoading) return <RightColumnSkeleton />;
    if (!data) return null;

    const { phoneNumber, email, schoolDetails } = data;
    const { subjects, classes, assignments } = schoolDetails;

    return (
        <>
            {(email || phoneNumber) && (
                <SectionCard>
                    <SectionTitle>Контакты</SectionTitle>
                    <div className="space-y-2">
                        {email && <InfoRow icon={Mail} value={email} />}
                        {phoneNumber && <InfoRow icon={Phone} value={phoneNumber} />}
                    </div>
                </SectionCard>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {subjects?.length > 0 && (
                    <SectionCard>
                        <SectionTitle icon={BookOpen}>Предметы</SectionTitle>
                        <div className="flex flex-wrap gap-2">
                            {subjects.map((s) => (
                                <span
                                    key={s.subject.id}
                                    className="text-xs font-bold px-3 py-1.5 rounded-xl bg-(--red-light)/60 text-(--red)"
                                >
                                    {s.subject.name}
                                </span>
                            ))}
                        </div>
                    </SectionCard>
                )}

                {classes?.length > 0 && (
                    <SectionCard>
                        <SectionTitle icon={GraduationCap}>Классы</SectionTitle>
                        <div className="flex flex-wrap gap-2">
                            {classes.map((c) => (
                                <span
                                    key={c.id}
                                    className="text-xs font-bold px-3 py-1.5 rounded-xl bg-(--navy)/8 text-(--navy)"
                                >
                                    {c.name}
                                </span>
                            ))}
                        </div>
                    </SectionCard>
                )}
            </div>

            {assignments?.length > 0 && (
                <SectionCard>
                    <SectionTitle>Назначения</SectionTitle>
                    <div className="space-y-2">
                        {assignments.map((a) => (
                            <div
                                key={a.id}
                                className="flex items-center justify-between p-3 rounded-[14px] bg-black/3"
                            >
                                <span className="text-sm font-bold text-(--navy)">{a.subject.name}</span>
                                <span className="text-xs font-bold px-2.5 py-1 rounded-lg bg-(--navy)/8 text-(--navy)">
                                    {a.schoolClass.name}
                                </span>
                            </div>
                        ))}
                    </div>
                </SectionCard>
            )}
        </>
    );
}

function ClassBlock({ schoolClass }: { schoolClass: SchoolClassResponse }) {
    const { name, academicYear } = schoolClass;
    return (
        <>
            <SectionTitle icon={GraduationCap}>Класс</SectionTitle>
            <div className="flex items-center justify-between mb-5">
                <span className="font-serif font-black text-4xl text-(--red) tracking-tight">{name}</span>
            </div>
            <div className="space-y-2.5">
                <div className="flex items-center justify-between">
                    <span className="text-xs font-semibold text-black/35 uppercase tracking-wider">Учебный год</span>
                    <div className="flex items-center gap-1.5">
                        {academicYear.isActive && <span className="w-1.5 h-1.5 rounded-full bg-green-500" />}
                        <span className="text-sm font-bold text-(--navy)">{academicYear.name}</span>
                    </div>
                </div>
                <div className="flex items-center justify-between">
                    <span className="text-xs font-semibold text-black/35 uppercase tracking-wider">Начало</span>
                    <span className="text-sm font-semibold text-black/55">{academicYear.startDate}</span>
                </div>
                <div className="flex items-center justify-between">
                    <span className="text-xs font-semibold text-black/35 uppercase tracking-wider">Конец</span>
                    <span className="text-sm font-semibold text-black/55">{academicYear.endDate}</span>
                </div>
            </div>
        </>
    );
}

function LeftColumnSkeleton() {
    return (
        <div className="lg:col-span-1 flex flex-col gap-6">
            <div className="glass-card rounded-[32px] p-6 flex flex-col items-center border-none shadow-lg backdrop-blur-md">
                <Skeleton className="w-28 h-28 rounded-[36px] mt-4 mb-4" />
                <Skeleton className="h-7 w-36 mb-2" />
                <Skeleton className="h-4 w-24 mb-5" />
                <Skeleton className="h-6 w-20" />
            </div>
            <div className="glass-card rounded-[28px] p-5 border-none shadow-lg backdrop-blur-md">
                <Skeleton className="h-5 w-24 mb-4" />
                <Skeleton className="h-10 w-16 mb-5" />
                <div className="space-y-2.5">
                    <Skeleton className="h-4 w-full" />
                    <Skeleton className="h-4 w-full" />
                    <Skeleton className="h-4 w-full" />
                </div>
            </div>
        </div>
    );
}

function RightColumnSkeleton() {
    return (
        <>
            <Skeleton className="h-20 w-full rounded-[24px]" />
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Skeleton className="h-52 rounded-[28px]" />
                <Skeleton className="h-52 rounded-[28px]" />
            </div>
        </>
    );
}

function StudentClassBlock({ userId }: { userId: number }) {
    const { data } = useStudentInfo(userId);
    if (!data?.schoolClass) return null;
    return (
        <SectionCard>
            <ClassBlock schoolClass={data.schoolClass} />
        </SectionCard>
    );
}

export default function ProfilePage() {
    const { id } = useParams<{ id: string }>();

    const pathId = Number(id);

    const { data: user, isLoading: userLoading } = useUserById(pathId);

    if (!id) {
        return <div>Неверный id</div>;
    }
    const role = user?.roles?.[0];

    return (
        <div className="relative z-10 min-h-screen px-4 md:px-10 pt-5 pb-14">
            <div className="max-w-350 mx-auto mb-6">
                <div className="glass-card rounded-[24px] p-4 flex items-center justify-between border-none shadow-lg backdrop-blur-md">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-[14px] bg-(--red-light)/60 flex items-center justify-center ring-1 ring-(--red)/10">
                            <User className="w-5 h-5 text-(--red)" />
                        </div>
                        <div>
                            <h1 className="font-serif font-black text-xl text-(--navy) tracking-tight">Профиль</h1>
                            <p className="text-xs text-black/40 mt-0.5">Личная информация</p>
                        </div>
                    </div>
                    <button className="glass-pill flex items-center gap-2 px-4 py-2 rounded-2xl text-sm font-bold text-(--navy) border-none shadow-sm">
                        <ChevronLeft className="w-4 h-4 text-(--red)" />
                        Назад
                    </button>
                </div>
            </div>

            <div className="max-w-350 mx-auto grid grid-cols-1 lg:grid-cols-3 gap-6">
                {userLoading ? (
                    <LeftColumnSkeleton />
                ) : (
                    <div className="lg:col-span-1 flex flex-col gap-6">
                        <div className="glass-card rounded-[32px] p-6 flex flex-col items-center text-center border-none shadow-lg backdrop-blur-md relative">
                            <span className="absolute top-5 right-6 text-[11px] font-bold text-black/25">
                                #{user?.id}
                            </span>
                            {user && (
                                <div className="mt-4 mb-4 ring-2 ring-(--red)/15 rounded-[36px]">
                                    <Avatar
                                        firstName={user.firstName}
                                        lastName={user.lastName}
                                        size="lg"
                                        color="red"
                                    />
                                </div>
                            )}
                            <div className="w-full">
                                <h2 className="font-serif font-black text-2xl text-(--navy) tracking-tight leading-tight mb-1">
                                    {user?.firstName} <br /> {user?.lastName}
                                </h2>
                                <p className="text-sm text-black/40 font-medium mb-5">@{user?.username}</p>
                                <div className="flex justify-center gap-2 flex-wrap">
                                    {user?.roles.map((r) => (
                                        <span
                                            key={r}
                                            className="text-[11px] font-black uppercase tracking-widest px-4 py-1.5 rounded-xl bg-(--red-light)/60 text-(--red)"
                                        >
                                            {r === "STUDENT" ? "Ученик" : r === "PARENT" ? "Родитель" : r === "TEACHER" ? "Учитель" : r === "ADMIN" ? "Админ" : r}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        </div>

                        {role === "STUDENT" && <StudentClassBlock userId={pathId} />}
                    </div>
                )}

                <div className="lg:col-span-2 flex flex-col gap-6">
                    {!userLoading && role === "STUDENT" && <StudentView userId={pathId} />}
                    {!userLoading && role === "PARENT" && <ParentView userId={pathId} />}
                    {!userLoading && role === "TEACHER" && <TeacherView userId={pathId} />}
                </div>
            </div>
        </div>
    );
}