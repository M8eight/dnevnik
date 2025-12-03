package com.rusobr.user.domain.service;

import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.mapper.UserMapper;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.web.dto.RequestUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public RequestUserDto getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toRequestUserDto(user);
    }

    public Iterable<User> getAll() {
        return userRepository.findAll();
    }

    public User create(RequestUserDto user) {
        User userEntity = userMapper.toUser(user);
        return userRepository.save(userEntity);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

}
