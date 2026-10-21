package com.example.opencodeproject.service;

import com.example.opencodeproject.dto.RegisterForm;
import com.example.opencodeproject.model.User;
import com.example.opencodeproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterForm form) {
        log.info("Registering user: email={}, name={}", form.getEmail(), form.getName());

        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            log.warn("Password mismatch for email={}", form.getEmail());
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        if (userRepository.existsByEmail(form.getEmail())) {
            log.warn("Email already exists: {}", form.getEmail());
            throw new IllegalArgumentException("Этот email уже зарегистрирован");
        }

        User user = new User();
        user.setName(form.getName().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRoles(EnumSet.of(User.Role.USER));

        User saved = userRepository.save(user);
        userRepository.flush();
        log.info("User saved with id={}", saved.getId());
        return saved;
    }
}