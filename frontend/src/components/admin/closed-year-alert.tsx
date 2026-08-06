import { AlertTriangle } from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";

interface ClosedYearAlertProps {
    yearName?: string;
    description: string;
}

export default function ClosedYearAlert({ yearName, description }: ClosedYearAlertProps) {
    return (
        <div className="max-w-350 mx-auto mb-6 animate-in fade-in slide-in-from-top-2 duration-300">
            <Alert variant="destructive" className="rounded-[24px] bg-linear-to-r from-red-50 to-red-50/50 border-red-200/80 shadow-lg backdrop-blur-sm">
                <div className="flex items-start gap-4">
                    <div className="shrink-0 mt-0.5 w-10 h-10 rounded-[14px] bg-red-100/60 flex items-center justify-center">
                        <AlertTriangle className="h-5 w-5 text-yellow-600" />
                    </div>
                    <div className="flex-1">
                        <AlertTitle className="font-serif font-black tracking-tight text-base text-yellow-900 mb-1">
                            Учебный год <span className="font-bold text-yellow-900">({yearName})</span> закрыт
                        </AlertTitle>
                        <AlertDescription className="text-sm text-yellow-800/85 font-medium leading-relaxed">
                            {description}
                        </AlertDescription>
                    </div>
                </div>
            </Alert>
        </div>
    );
}
