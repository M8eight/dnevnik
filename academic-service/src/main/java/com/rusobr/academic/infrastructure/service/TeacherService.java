package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.infrastructure.feignClient.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.web.dto.userService.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {
    private final SchoolClassRepository schoolClassRepository;
    private final UserClient userClient;

    public Set<UserResponse> getUsersIdFromClass(Long classId) {
        Set<Long> userIds = schoolClassRepository.getStudentIdsFromSchoolClasses(classId);
        log.info("userIds: {}", userIds);
        return userClient.getBatchUsers(userIds);
    }

}
