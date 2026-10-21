package com.example.opencodeproject.controller;

import com.example.opencodeproject.dto.ProductForm;
import com.example.opencodeproject.model.Product;
import com.example.opencodeproject.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Test
    void rendersCatalog() throws Exception {
        productService.create(form("Кофеварка Bravilor", "45990.00", 3));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(content().string(containsString("Кофеварка Bravilor")));
    }

    @Test
    void rendersProductCardAndCountsView() throws Exception {
        Product product = productService.create(form("Лампа Nordic", "2490.00", 4));

        mockMvc.perform(get("/products/" + product.getSlug()))
                .andExpect(status().isOk())
                .andExpect(view().name("products/detail"))
                .andExpect(content().string(containsString("Лампа Nordic")));

        assertThat(productService.getBySlug(product.getSlug()).getViewCount()).isEqualTo(1);
    }

    @Test
    void returnsNotFoundForUnknownProduct() throws Exception {
        mockMvc.perform(get("/products/no-such-product"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createsProductFromAdminForm() throws Exception {
        mockMvc.perform(post("/admin/products")
                        .param("name", "Пылесос Dyson")
                        .param("price", "19990.00")
                        .param("stock", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"));

        assertThat(productService.findAll()).hasSize(1);
        assertThat(productService.findAll().get(0).getSlug()).isEqualTo("pylesos-dyson");
    }

    @Test
    void showsValidationErrorsOnInvalidForm() throws Exception {
        mockMvc.perform(post("/admin/products")
                        .param("name", "")
                        .param("price", "-5"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/products/form"))
                .andExpect(content().string(containsString("Укажите название товара")));
    }

    private static ProductForm form(String name, String price, int stock) {
        ProductForm form = new ProductForm();
        form.setName(name);
        form.setPrice(new BigDecimal(price));
        form.setStock(stock);
        return form;
    }
}
