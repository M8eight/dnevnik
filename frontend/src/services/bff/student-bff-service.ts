import api from "@/axios/axios";
import type { HomeworkWithSubjectResponse } from "../homework-service";
import type { ScheduleItem, ScheduleResponse } from "../schedule-service";
import type { GradeWithSubjectNameResponse } from "../grade-service";

export interface HomeAggregation {
    todayHomework: HomeworkWithSubjectResponse[];
    weekSchedule: ScheduleResponse;
    todaySchedule: ScheduleItem[];
    todayGrades: GradeWithSubjectNameResponse[];
    todayAverage: number;
}

export const homeAggregation = async (date: string): Promise<HomeAggregation> => {
    const response = await api.get(`/academic-service/api/v1/bff/students/home`, {
        params: { date }
    });
    return response.data;
};
