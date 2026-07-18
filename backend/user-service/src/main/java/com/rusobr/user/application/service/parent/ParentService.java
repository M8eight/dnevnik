package com.rusobr.user.application.service.parent;

import com.rusobr.common.enums.UserRole;
import com.rusobr.common.exception.NotFoundException;
import com.rusobr.user.application.event.UserDeletedEvent;
import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.web.dto.parent.ParentInfoResponse;
import com.rusobr.user.web.exception.UserExceptionCode;
import com.rusobr.user.application.mapper.ParentMapper;
import com.rusobr.user.infrastructure.persistence.repository.ParentRepository;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.parent.ParentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ParentService {

    private final ParentRepository parentRepository;
    private final ParentMapper parentMapper;
    private final UserRepository userRepository;

    public ParentResponse getWithUserById(Long id) {
        Parent parentFetch = parentRepository.findWithUserById(id)
                .orElseThrow(() -> notFoundParent(id));
        return parentMapper.toResponse(parentFetch);
    }

    public ParentDetails getDetailsById(Long id) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> notFoundParent(id));
        return parentMapper.toParentDetails(parent);
    }

    public ParentInfoResponse getInfoById(Long id) {
        Parent parent = parentRepository.findParentInfoById(id)
                .orElseThrow(() -> notFoundParent(id));
        return parentMapper.toParentInfoResponse(parent);
    }

    public Optional<Parent> findByIdWithDeleted(Long id) {
        return parentRepository.findByIdWithDeleted(id);
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
