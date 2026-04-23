import api from "@/axios/axios";

export interface TeachingAssignmentDetail {
    teachingAssignmentId: number;
    schoolClassId: number;
    schoolClassName: string;
    subjectId: number;
    subjectName: string;
}

export const getTeachingAssignmentDetail = async (teacherId: number): Promise<TeachingAssignmentDetail[]> => {
    const {data} = await api.get<TeachingAssignmentDetail[]>(`/academic-service/api/v1/teacher/assignments`,
        { params: { teacherId } });
    return data;
}