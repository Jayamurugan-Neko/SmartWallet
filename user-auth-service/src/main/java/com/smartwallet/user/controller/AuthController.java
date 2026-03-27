package com.smartwallet.user.controller;

import com.smartwallet.common.event.KafkaEvent;
import com.smartwallet.common.exception.BusinessException;
import com.smartwallet.user.dto.AuthResponse;
import com.smartwallet.user.dto.LoginRequest;
import com.smartwallet.user.dto.RegisterRequest;
import com.smartwallet.user.entity.User;
import com.smartwallet.user.repository.UserRepository;
import com.smartwallet.user.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMAIL_ALREADY_IN_USE", "Email is already registered.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .kycStatus("PENDING")
                .roles("ROLE_USER")
                .build();

        user = userRepository.save(user);

        // Publish event
        KafkaEvent<Map<String, String>> event = KafkaEvent.<Map<String, String>>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("user.registered")
                .occurredAt(Instant.now())
                .payload(Map.of("userId", user.getId().toString(), "email", user.getEmail()))
                .build();
        kafkaTemplate.send("user-events", user.getId().toString(), event);

        String token = jwtService.generateToken(user.getId(), user.getRoles());
        return new AuthResponse(token, "dummy-refresh-token", user.getId());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid email or password.");
        }

        String token = jwtService.generateToken(user.getId(), user.getRoles());
        return new AuthResponse(token, "dummy-refresh-token", user.getId());
    }
}
