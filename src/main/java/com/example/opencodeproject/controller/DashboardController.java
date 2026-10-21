package com.example.opencodeproject.controller;

import com.example.opencodeproject.model.Product;
import com.example.opencodeproject.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ProductService productService;


    @GetMapping("/dashboard")
    public String userDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Показываем последние товары пользователю
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        model.addAttribute("username", userDetails != null ? userDetails.getUsername() : "Гость");
        return "dashboard";
    }
}