package com.smartwallet.user.controller;

import com.smartwallet.common.security.UserContext;
import com.smartwallet.user.entity.User;
import com.smartwallet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.smartwallet.common.exception.BusinessException;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public User getCurrentUser() {
        String userId = UserContext.getUserId();
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
    }
}
