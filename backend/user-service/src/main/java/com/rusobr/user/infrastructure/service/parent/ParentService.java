package com.rusobr.user.infrastructure.service.parent;

import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.exception.NotFoundException;
import com.rusobr.user.infrastructure.mapper.ParentMapper;
import com.rusobr.user.infrastructure.persistence.repository.ParentRepository;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.parent.ParentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ParentService {

    private final ParentRepository parentRepository;
    private final ParentMapper parentMapper;
    private final UserRepository userRepository;

    @Transactional
    public void createParent(Long userId, ParentDetails parentDetails) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        parentRepository.save(parentMapper.toEntity(user, parentDetails));
    }

    public ParentResponse getParent(Long userId) {
        Parent parentFetch = parentRepository.findWithUserById(userId).orElseThrow(() -> new NotFoundException("Parent not found: " + userId));
        return parentMapper.toResponse(parentFetch);
    }

}
