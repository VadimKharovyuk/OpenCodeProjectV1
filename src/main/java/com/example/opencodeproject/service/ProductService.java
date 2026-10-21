package com.example.opencodeproject.service;

import com.example.opencodeproject.dto.ProductForm;
import com.example.opencodeproject.exception.ProductNotFoundException;
import com.example.opencodeproject.model.Product;
import com.example.opencodeproject.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Product getBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> ProductNotFoundException.bySlug(slug));
    }

    /**
     * Отдаёт товар и увеличивает счётчик просмотров.
     */
    @Transactional
    public Product viewBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> ProductNotFoundException.bySlug(slug));
        product.setViewCount(product.getViewCount() + 1);
        return product;
    }

    @Transactional(readOnly = true)
    public ProductForm getForm(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ProductNotFoundException.byId(id));

        ProductForm form = new ProductForm();
        form.setId(product.getId());
        form.setName(product.getName());
        form.setSlug(product.getSlug());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setStock(product.getStock());
        return form;
    }

    @Transactional
    public Product create(ProductForm form) {
        Product product = new Product();
        applyForm(product, form);
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductForm form) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ProductNotFoundException.byId(id));
        applyForm(product, form);
        return product;
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw ProductNotFoundException.byId(id);
        }
        productRepository.deleteById(id);
    }

    private void applyForm(Product product, ProductForm form) {
        product.setName(form.getName().trim());
        product.setDescription(normalizeNullable(form.getDescription()));
        product.setPrice(form.getPrice());
        product.setStock(form.getStock());

        String base = isBlank(form.getSlug()) ? slugify(form.getName()) : slugify(form.getSlug());
        product.setSlug(uniqueSlug(base, product.getId()));
    }

    private String uniqueSlug(String base, Long currentId) {
        String safeBase = base.length() > 120 ? base.substring(0, 120) : base;
        if (safeBase.isBlank()) {
            safeBase = "product";
        }

        String candidate = safeBase;
        int suffix = 2;
        while (true) {
            Optional<Product> existing = productRepository.findBySlug(candidate);
            if (existing.isEmpty() || Objects.equals(existing.get().getId(), currentId)) {
                return candidate;
            }
            candidate = safeBase + "-" + suffix++;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static String slugify(String input) {
        if (input == null) {
            return "";
        }

        String lower = input.toLowerCase(Locale.ROOT).trim();
        StringBuilder out = new StringBuilder(lower.length());

        for (char ch : lower.toCharArray()) {
            String transliterated = transliterate(ch);
            if (transliterated != null) {
                out.append(transliterated);
            } else if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
                out.append(ch);
            } else {
                out.append(' ');
            }
        }

        return out.toString()
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }

    private static String transliterate(char ch) {
        return switch (ch) {
            case 'а' -> "a";
            case 'б' -> "b";
            case 'в' -> "v";
            case 'г' -> "g";
            case 'д' -> "d";
            case 'е' -> "e";
            case 'ё' -> "e";
            case 'ж' -> "zh";
            case 'з' -> "z";
            case 'и' -> "i";
            case 'й' -> "i";
            case 'к' -> "k";
            case 'л' -> "l";
            case 'м' -> "m";
            case 'н' -> "n";
            case 'о' -> "o";
            case 'п' -> "p";
            case 'р' -> "r";
            case 'с' -> "s";
            case 'т' -> "t";
            case 'у' -> "u";
            case 'ф' -> "f";
            case 'х' -> "h";
            case 'ц' -> "ts";
            case 'ч' -> "ch";
            case 'ш' -> "sh";
            case 'щ' -> "sch";
            case 'ъ' -> "";
            case 'ы' -> "y";
            case 'ь' -> "";
            case 'э' -> "e";
            case 'ю' -> "yu";
            case 'я' -> "ya";
            default -> null;
        };
    }
}
