package com.example.opencodeproject.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Данные формы создания/редактирования товара.
 * Slug необязателен: если пустой, сервис сгенерирует его из названия.
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductForm {

    private Long id;

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 140)
    private String slug;

    @Size(max = 2000)
    private String description;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    @PositiveOrZero
    private int stock;
}
