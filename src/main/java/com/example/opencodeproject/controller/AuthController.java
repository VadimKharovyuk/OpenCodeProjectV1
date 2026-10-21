package com.example.opencodeproject.controller;

import com.example.opencodeproject.dto.RegisterForm;
import com.example.opencodeproject.model.User;
import com.example.opencodeproject.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        log.info("Register endpoint called: email={}", form.getEmail());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            return "register";
        }

        try {
            User user = authService.register(form);
            log.info("User registered successfully: id={}", user.getId());
            redirectAttributes.addFlashAttribute("flash", "Регистрация прошла успешно. Войдите в аккаунт.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            bindingResult.rejectValue("email", "error", e.getMessage());
            return "register";
        } catch (Exception e) {
            log.error("Unexpected error during registration", e);
            bindingResult.rejectValue("email", "error", "Произошла ошибка: " + e.getMessage());
            return "register";
        }
    }
}