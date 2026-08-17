package com.flowforge.core.service;

import com.flowforge.core.domain.User;
import com.flowforge.core.dto.LoginRequest;
import com.flowforge.core.dto.RegisterRequest;
import com.flowforge.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.flowforge.core.util.UserFacingMessages.EMAIL_TAKEN;
import static com.flowforge.core.util.UserFacingMessages.INVALID_CREDENTIALS;
import static com.flowforge.core.util.UserFacingMessages.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(EMAIL_TAKEN);
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .build();

        User saved = userRepository.save(user);
        auditService.log(saved.getId(), "REGISTER", "USER", saved.getId(), "User registered");
        return saved;
    }

    public User authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException(INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException(INVALID_CREDENTIALS);
        }

        auditService.log(user.getId(), "LOGIN", "USER", user.getId(), "User logged in");
        return user;
    }

    public User findById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
    }
}
