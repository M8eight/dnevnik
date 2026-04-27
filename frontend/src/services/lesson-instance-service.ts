import api from "@/axios/axios";

export interface lessonInstance {
    id: number;
    lessonDate: string;
};

export const getLessonInstancesByTeachingAssignment = async (teachingAssignmentId: number, academicPeriodId: number): Promise<lessonInstance[]> => {
    const { data } = await api.get<lessonInstance[]>(`/academic-service/api/v1/lesson-instances/by-teaching-assignment`, {
        params: {
            teachingAssignmentId,
            academicPeriodId
        }
    });
    return data;
}