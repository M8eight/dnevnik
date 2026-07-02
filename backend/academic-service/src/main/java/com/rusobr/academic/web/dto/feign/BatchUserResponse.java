package com.rusobr.academic.web.dto.feign;

import java.util.List;

public record BatchUserResponse(
        List<UserFeignResponse> found,
        List<Long> notFound
) {}
