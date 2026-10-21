package com.example.opencodeproject.controller;

import com.example.opencodeproject.dto.ProductForm;
import com.example.opencodeproject.model.Product;
import com.example.opencodeproject.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private static final String FORM_VIEW = "admin/products/form";

    private final ProductService productService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin/products/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("formAction", "/admin/products");
        model.addAttribute("editing", false);
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("productForm") ProductForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/admin/products");
            model.addAttribute("editing", false);
            return FORM_VIEW;
        }

        Product product = productService.create(form);
        redirectAttributes.addFlashAttribute("flash", "Товар «" + product.getName() + "» создан");
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("productForm", productService.getForm(id));
        model.addAttribute("formAction", "/admin/products/" + id);
        model.addAttribute("editing", true);
        return FORM_VIEW;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("productForm") ProductForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/admin/products/" + id);
            model.addAttribute("editing", true);
            return FORM_VIEW;
        }

        Product product = productService.update(id, form);
        redirectAttributes.addFlashAttribute("flash", "Товар «" + product.getName() + "» обновлён");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("flash", "Товар удалён");
        return "redirect:/admin/products";
    }
}
