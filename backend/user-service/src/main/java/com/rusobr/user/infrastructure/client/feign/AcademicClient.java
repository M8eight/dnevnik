package com.rusobr.user.infrastructure.client.feign;

import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.feign.TeacherAcademicFeignDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "academic-service")
public interface AcademicClient {

    @GetMapping("/api/v1/school-classes/search/by-student")
    SchoolClassResponse getSchoolClassByStudentId(@RequestParam("studentId") Long studentId);

    @GetMapping("/api/v1/teachers/{id}/info")
    TeacherAcademicFeignDto getTeacherAcademicInfo(@PathVariable("id") Long teacherId);

}
