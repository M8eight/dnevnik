package com.rusobr.academic.infrastructure.client;

import com.rusobr.academic.config.FeignConfig;
import com.rusobr.academic.web.dto.feign.BatchUserResponse;
import com.rusobr.academic.web.dto.feign.TeacherResponse;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;

@FeignClient(value = "user-service", fallbackFactory = UserClientFallbackFactory.class, configuration = FeignConfig.class)
public interface UserClient {

    @GetMapping("/api/v1/teachers/{id}")
    TeacherResponse getTeacherById(@PathVariable Long id);

    @GetMapping("/api/v1/teachers/{id}/simple")
    UserFeignResponse getTeacherSimpleById(@PathVariable Long id);

    @PostMapping("/api/v1/teachers/batch")
    BatchUserResponse getBatchTeachers(@RequestBody List<Long> ids);

    @GetMapping("/api/v1/students/{id}/details")
    void existStudentById(@PathVariable Long id);

    @PostMapping("/api/v1/students/exclude-assigned")
    List<UserFeignResponse> getBatchStudentsExcludeAssigned(@RequestBody Set<Long> ids);

    @PostMapping("/api/v1/students/batch")
    BatchUserResponse getBatchUsers(@RequestBody List<Long> ids);

}
