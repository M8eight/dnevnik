function getInitials(firstName: string, lastName: string) {
    return `${firstName[0]}${lastName[0]}`.toUpperCase();
}

export function Avatar({
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
