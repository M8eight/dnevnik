package com.rusobr.user.infrastructure.service.parent;

import com.rusobr.user.domain.event.UserDeletedEvent;
import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.infrastructure.exception.NotFoundException;
import com.rusobr.user.infrastructure.mapper.ParentMapper;
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

    public ParentResponse getWithUserById(Long userId) {
        Parent parentFetch = parentRepository.findWithUserById(userId).orElseThrow(() -> new NotFoundException("Parent not found: " + userId));
        return parentMapper.toResponse(parentFetch);
    }

    public ParentDetails getDetailsById(Long id) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Parent not found: " + id));
        return parentMapper.toParentDetails(parent);
    }

    public Optional<Parent> findByIdWithDeleted(Long id) {
        return parentRepository.findByIdWithDeleted(id);
    }

    @Transactional
    public void create(Long userId, ParentDetails parentDetails) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        parentRepository.save(parentMapper.toEntity(user, parentDetails));
    }

    @Transactional
    public void update(Long userId, ParentDetails parentDetails) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
        if (!parentRepository.existsById(userId)) {
            throw new NotFoundException("Parent not found: " + userId);
        }
    }

    public void delete(Long parentId) {
        parentRepository.deleteById(parentId);
    }

    @EventListener
    public void handleUserDelete(UserDeletedEvent event) {
        if (event.roles().contains(UserRole.PARENT)) {
            this.delete(event.id());
        }
    }

}
