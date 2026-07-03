package com.rusobr.user.application.service.user.strategy;

import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.enums.UserRole;
import com.rusobr.user.web.exception.ConflictException;
import com.rusobr.user.application.service.parent.ParentService;
import com.rusobr.user.web.dto.user.UserProfileDetails;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.user.UserRoleStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.rusobr.user.web.exception.ExceptionCode.PARENT_PROFILE_DETAILS_CONFLICT;

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
                parentService.create(userId, parentDetails);
            }
        } else {
            throw new ConflictException("Invalid parent profile details", PARENT_PROFILE_DETAILS_CONFLICT);
        }
    }

    @Override
    public void delete(Long userId) {
        parentService.delete(userId);
    }

    @Override
    public void update(Long userId, UserProfileDetails userDetails) {
        if (userDetails instanceof ParentDetails parentDetails) {
            parentService.update(userId, parentDetails);
        } else {
            throw new ConflictException("Invalid parent profile details", PARENT_PROFILE_DETAILS_CONFLICT);
        }
    }

    @Override
    public UserRole getRole() {
        return UserRole.PARENT;
    }

}
