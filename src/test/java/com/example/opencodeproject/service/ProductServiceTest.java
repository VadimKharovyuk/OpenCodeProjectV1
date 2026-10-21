package com.example.opencodeproject.service;

import com.example.opencodeproject.dto.ProductForm;
import com.example.opencodeproject.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Test
    void createsProductAndGeneratesSlugFromName() {
        Product product = productService.create(form("Кофеварка Bravilor", "45990.00", 3));

        assertThat(product.getId()).isNotNull();
        assertThat(product.getSlug()).isEqualTo("kofevarka-bravilor");
        assertThat(product.getViewCount()).isZero();
        assertThat(product.getStock()).isEqualTo(3);
    }

    @Test
    void keepsSlugUnique() {
        productService.create(form("Кофеварка Bravilor", "45990.00", 3));
        Product second = productService.create(form("Кофеварка Bravilor", "45990.00", 3));

        assertThat(second.getSlug()).isEqualTo("kofevarka-bravilor-2");
    }

    @Test
    void incrementsViewCountOnEveryView() {
        Product product = productService.create(form("Механическая клавиатура", "12990.00", 7));

        assertThat(productService.viewBySlug(product.getSlug()).getViewCount()).isEqualTo(1);
        assertThat(productService.viewBySlug(product.getSlug()).getViewCount()).isEqualTo(2);
    }

    @Test
    void updatesProductAndKeepsCustomSlug() {
        Product product = productService.create(form("Лампа", "1990.00", 1));

        ProductForm update = new ProductForm();
        update.setName("Лампа Nordic");
        update.setSlug("lamp-nordic");
        update.setDescription("Стекло и дерево");
        update.setPrice(new BigDecimal("2490.00"));
        update.setStock(4);

        Product saved = productService.update(product.getId(), update);

        assertThat(saved.getName()).isEqualTo("Лампа Nordic");
        assertThat(saved.getSlug()).isEqualTo("lamp-nordic");
        assertThat(saved.getDescription()).isEqualTo("Стекло и дерево");
        assertThat(saved.getPrice()).isEqualByComparingTo("2490.00");
        assertThat(saved.getStock()).isEqualTo(4);
    }

    @Test
    void deletesProduct() {
        Product product = productService.create(form("Пылесос", "19990.00", 2));

        productService.delete(product.getId());

        assertThat(productService.findAll()).isEmpty();
    }

    @Test
    void generatesSlugFromCustomValue() {
        ProductForm form = form("Лампа", "1990.00", 1);
        form.setSlug("  Lampa Nordik  ");

        Product product = productService.create(form);

        assertThat(product.getSlug()).isEqualTo("lampa-nordik");
    }

    private static ProductForm form(String name, String price, int stock) {
        ProductForm form = new ProductForm();
        form.setName(name);
        form.setPrice(new BigDecimal(price));
        form.setStock(stock);
        return form;
    }
}
