import api from "@/axios/axios";
import type { PageResponse } from "@/helpers/helper-interfaces";
import type { UserSimpleResponse } from "./user-service";

export interface MailResponse {
    id: number;
    senderId: number;
    subject: string;
    body: string;
    sentAt: string;
}

export interface MailWithUserResponse {
    mailId: number;
    sender: UserSimpleResponse;
    subject: string;
    body: string;
    sentAt: string;
}

export interface DirectMailRequest {
    subject: string;
    body: string;
    recipientIds: number[];
}

export const getMail = async (mailId: number): Promise<MailWithUserResponse> => {
    const { data } = await api.get<MailWithUserResponse>(`/mail-service/api/v1/mails/${mailId}`);
    return data;
};

export const getInbox = async (
    page = 0,
    size = 20
): Promise<PageResponse<MailWithUserResponse>> => {
    const { data } = await api.get<PageResponse<MailWithUserResponse>>(
        `/mail-service/api/v1/mails/inbox`,
        { params: { page, size } }
    );
    return data;
};

export const getSent = async (
    page = 0,
    size = 20
): Promise<PageResponse<MailWithUserResponse>> => {
    const { data } = await api.get<PageResponse<MailWithUserResponse>>(
        `/mail-service/api/v1/mails/sent`,
        { params: { page, size } }
    );
    return data;
};

export const getUnreadCount = async (): Promise<number> => {
    const { data } = await api.patch<number>(`/mail-service/api/v1/mails/unread-count`);
    return data;
};

export const sendDirectMail = async (request: DirectMailRequest): Promise<MailResponse> => {
    const { data } = await api.post<MailResponse>(
        `/mail-service/api/v1/mails/direct-mails`,
        request
    );
    return data;
};

export const markMailRead = async (mailId: number): Promise<void> => {
    await api.patch(`/mail-service/api/v1/mails/${mailId}/read`);
};

export const deleteInboxMail = async (mailId: number): Promise<void> => {
    await api.delete(`/mail-service/api/v1/mails/${mailId}/inbox`);
};

export const deleteSentMail = async (mailId: number): Promise<void> => {
    await api.delete(`/mail-service/api/v1/mails/${mailId}/sent`);
};
