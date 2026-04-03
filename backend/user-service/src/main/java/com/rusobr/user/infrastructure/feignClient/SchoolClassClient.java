package com.rusobr.user.infrastructure.feignClient;

import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "academic-service")
public interface SchoolClassClient {

    @GetMapping("/api/v1/school-classes/search/by-student")
    SchoolClassResponse getSchoolClassByStudentId(@RequestParam("studentId") Long studentId);

}
