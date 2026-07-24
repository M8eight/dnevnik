package com.rusobr.common.dto;

import java.util.List;

public record BatchUserResponse(
        List<UserFeignResponse> found,
        List<Long> notFound,
        boolean degraded
) {
    public static BatchUserResponse ok(List<UserFeignResponse> found, List<Long> notFound) {
        return new BatchUserResponse(found, notFound, false);
    }

    public static BatchUserResponse degraded(List<Long> ids) {
        List<UserFeignResponse> users = ids.stream().map(userId ->
                UserFeignResponse.builder().id(userId).build()).toList();
        return new BatchUserResponse(users, List.of(), true);
    }
}
