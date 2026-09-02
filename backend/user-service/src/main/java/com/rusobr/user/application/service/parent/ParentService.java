package com.rusobr.user.application.service.parent;

import com.rusobr.common.enums.UserRole;
import com.rusobr.common.exception.NotFoundException;
import com.rusobr.user.application.event.UserDeletedEvent;
import com.rusobr.user.application.mapper.ParentMapper;
import com.rusobr.user.application.mapper.UserMapper;
import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.persistence.repository.ParentRepository;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.infrastructure.specification.UserSpecification;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.parent.ParentInfoResponse;
import com.rusobr.user.web.dto.parent.ParentResponse;
import com.rusobr.user.web.dto.user.UserResponse;
import com.rusobr.user.web.exception.UserExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ParentService {

    private final ParentRepository parentRepository;
    private final ParentMapper parentMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public ParentResponse getWithUserById(Long id) {
        Parent parentFetch = parentRepository.findWithUserById(id)
                .orElseThrow(() -> notFoundParent(id));
        return parentMapper.toResponse(parentFetch);
    }

    @Transactional(readOnly = true)
    public ParentDetails getDetailsById(Long id) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> notFoundParent(id));
        return parentMapper.toParentDetails(parent);
    }

    @Transactional(readOnly = true)
    public ParentInfoResponse getInfoById(Long id) {
        Parent parent = parentRepository.findParentInfoById(id)
                .orElseThrow(() -> notFoundParent(id));
        return parentMapper.toParentInfoResponse(parent);
    }

    @Transactional(readOnly = true)
    public Optional<Parent> findByIdWithDeleted(Long id) {
        return parentRepository.findByIdWithDeleted(id);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getUnassignedToStudent(Pageable pageable, String fullNameSearch) {
        Specification<User> spec = UserSpecification.findByRole(UserRole.PARENT)
                .and(UserSpecification.parentsWithoutStudents())
                .and(UserSpecification.findByFullNameFuzzy(fullNameSearch));

        return userRepository.findAll(spec, PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "lastName", "firstName")
        )).map(userMapper::toUserResponse);
    }

    @Transactional
    public void create(Long userId, ParentDetails parentDetails) {
        User user = userRepository.findById(userId).orElseThrow(() -> notFoundUser(userId));
        parentRepository.save(parentMapper.toEntity(user, parentDetails));
    }

    @Transactional
    public void update(Long userId, ParentDetails parentDetails) {
        if (!userRepository.existsById(userId)) {
            throw notFoundUser(userId);
        }
        if (!parentRepository.existsById(userId)) {
            throw notFoundParent(userId);
        }
    }

    public void delete(Long parentId) {
        if (!parentRepository.existsById(parentId)) {
            throw notFoundParent(parentId);
        }
        parentRepository.deleteById(parentId);
    }

    @EventListener
    public void handleUserDelete(UserDeletedEvent event) {
        if (event.roles().contains(UserRole.PARENT)) {
            this.delete(event.id());
        }
    }

    //helpers
    private NotFoundException notFoundParent(Long id) {
        return new NotFoundException("Parent by id: %d not found".formatted(id), UserExceptionCode.PARENT_NOT_FOUND);
    }

    private NotFoundException notFoundUser(Long id) {
        return new NotFoundException("User by id: %d not found".formatted(id), UserExceptionCode.USER_NOT_FOUND);
    }

}
