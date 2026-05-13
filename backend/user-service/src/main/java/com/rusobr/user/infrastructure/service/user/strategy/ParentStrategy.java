package com.rusobr.user.infrastructure.service.user.strategy;

import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.infrastructure.exception.ConflictException;
import com.rusobr.user.infrastructure.service.parent.ParentService;
import com.rusobr.user.infrastructure.service.user.UserProfileDetails;
import com.rusobr.user.web.dto.parent.ParentDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class ParentStrategy implements UserRoleStrategy {
    private final ParentService parentService;

    @Override
    public void save(Long userId, UserProfileDetails userDetails) {
        if (userDetails instanceof ParentDetails parentDetails) {
            Optional<Parent> parentOptional = parentService.findByIdWithDeleted(userId);
            if (parentOptional.isPresent()) {
                Parent parent = parentOptional.get();
                parent.setDeletedAt(null);
            } else {
                parentService.createParent(userId, parentDetails);
            }
        } else {
            throw new ConflictException("Invalid user profile details");
        }
    }

    @Override
    public void delete(Long userId) {
        parentService.deleteById(userId);
    }

    @Override
    public void update(Long userId, UserProfileDetails userDetails) {
        if (userDetails instanceof ParentDetails parentDetails) {
            parentService.updateParent(userId, parentDetails);
        } else {
            throw new ConflictException("Invalid user profile details");
        }
    }

    @Override
    public UserRole getRole() {
        return UserRole.PARENT;
    }

}
