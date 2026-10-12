import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    deleteInboxMail,
    deleteSentMail,
    getInbox,
    getMail,
    getSent,
    getUnreadCount,
    markMailRead,
    sendDirectMail,
    type DirectMailRequest,
    type MailResponse,
    type MailWithUserResponse,
} from "@/services/mail-service";
import type { PageResponse } from "@/helpers/helper-interfaces";

const QUERY_KEY = ["mails"];

export const useMail = (mailId: number, enabled: boolean = true) =>
    useQuery<MailWithUserResponse>({
        queryKey: [...QUERY_KEY, "detail", mailId],
        queryFn: () => getMail(mailId),
        enabled: enabled && !!mailId,
    });

export const useInbox = (page = 0, size = 20, enabled: boolean = true) =>
    useQuery<PageResponse<MailWithUserResponse>>({
        queryKey: [...QUERY_KEY, "inbox", { page, size }],
        queryFn: () => getInbox(page, size),
        enabled,
        staleTime: 0,
        refetchOnWindowFocus: false,
    });

export const useSent = (page = 0, size = 20, enabled: boolean = true) =>
    useQuery<PageResponse<MailWithUserResponse>>({
        queryKey: [...QUERY_KEY, "sent", { page, size }],
        queryFn: () => getSent(page, size),
        enabled,
        staleTime: 0,
        refetchOnWindowFocus: false,
    });

export const useUnreadCount = (enabled: boolean = true) =>
    useQuery<number>({
        queryKey: [...QUERY_KEY, "unread-count"],
        queryFn: getUnreadCount,
        enabled,
        staleTime: 1000 * 30,
        refetchInterval: 1000 * 30,
    });

export const useSendDirectMail = () => {
    const queryClient = useQueryClient();
    return useMutation<MailResponse, Error, DirectMailRequest>({
        mutationFn: sendDirectMail,
        onSuccess: () => queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
    });
};

export const useMarkMailRead = () => {
    const queryClient = useQueryClient();
    return useMutation<void, Error, number>({
        mutationFn: markMailRead,
        onSuccess: () =>
            queryClient.invalidateQueries({ queryKey: [...QUERY_KEY, "unread-count"] }),
    });
};

export const useDeleteInboxMail = () => {
    const queryClient = useQueryClient();
    return useMutation<void, Error, number>({
        mutationFn: deleteInboxMail,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [...QUERY_KEY, "inbox"] });
            queryClient.invalidateQueries({ queryKey: [...QUERY_KEY, "unread-count"] });
        },
    });
};

export const useDeleteSentMail = () => {
    const queryClient = useQueryClient();
    return useMutation<void, Error, number>({
        mutationFn: deleteSentMail,
        onSuccess: () => queryClient.invalidateQueries({ queryKey: [...QUERY_KEY, "sent"] }),
    });
};
