import type { ReactNode } from "react";
import type { LucideIcon } from "lucide-react";

interface PageHeaderProps {
    icon: LucideIcon;
    title: string;
    subtitle?: ReactNode;
    children?: ReactNode;
}

export default function PageHeader({ icon: Icon, title, subtitle, children }: PageHeaderProps) {
    return (
        <div className="max-w-350 mx-auto mb-6">
            <div className="glass-card rounded-[24px] p-5 flex flex-col lg:flex-row justify-between lg:items-center gap-5 border-none shadow-lg backdrop-blur-md">
                <div className="flex items-center gap-4">
                    <div className="hidden sm:flex w-12 h-12 rounded-[18px] bg-(--red-light)/60 items-center justify-center ring-1 ring-(--red)/10">
                        <Icon className="w-6 h-6 text-(--red)" />
                    </div>
                    <div>
                        <h1 className="font-serif font-black text-2xl lg:text-3xl text-(--navy) tracking-tight">
                            {title}
                        </h1>
                        {subtitle && (
                            <p className="text-sm text-black/40 mt-0.5">{subtitle}</p>
                        )}
                    </div>
                </div>
                {children && (
                    <div className="flex flex-col lg:flex-row items-start lg:items-center gap-4 w-full lg:w-auto">
                        {children}
                    </div>
                )}
            </div>
        </div>
    );
}
