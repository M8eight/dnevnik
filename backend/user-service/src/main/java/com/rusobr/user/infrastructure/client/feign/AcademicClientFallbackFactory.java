package com.rusobr.user.infrastructure.client.feign;

import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.feign.TeacherAcademicFeignDto;
import com.rusobr.user.web.exception.AcademicServiceUnavailableException;
import com.rusobr.user.web.exception.ExceptionCode;
import com.rusobr.user.web.exception.NotFoundException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AcademicClientFallbackFactory implements FallbackFactory<AcademicClient> {
    @Override
    public AcademicClient create(Throwable cause) {
        return new AcademicClient() {

            @Override
            public SchoolClassResponse getSchoolClassByStudentId(Long studentId) {
                if (cause instanceof FeignException.NotFound) {
                    throw new NotFoundException("School class with %s not found".formatted(studentId),
                            ExceptionCode.SCHOOL_CLASS_BY_STUDENT_NOT_FOUND);
                }
                log.error("Fallback: getSchoolClassByStudentId id={}", studentId, cause);
                throw new AcademicServiceUnavailableException("Не удалось получить класс по studentId %s".formatted(studentId), cause,
                        ExceptionCode.ACADEMIC_SERVICE_UNAVAILABLE);
            }

            @Override
            public TeacherAcademicFeignDto getTeacherAcademicInfo(Long teacherId) {
                if (cause instanceof FeignException.NotFound) {
                    throw new NotFoundException("Teacher info with id: %s not found".formatted(teacherId),
                            ExceptionCode.ACADEMIC_SERVICE_TEACHER_INFO_NOT_FOUND);
                }
                log.error("Fallback: getTeacherAcademicInfo id={}", teacherId, cause);
                throw new AcademicServiceUnavailableException("Не удалось получить информацию о учителе с id: %s".formatted(teacherId), cause,
                        ExceptionCode.ACADEMIC_SERVICE_UNAVAILABLE);
            }

        };
    }
}
