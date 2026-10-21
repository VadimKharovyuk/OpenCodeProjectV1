package com.example.opencodeproject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }

    public static ProductNotFoundException bySlug(String slug) {
        return new ProductNotFoundException("Товар со slug «" + slug + "» не найден");
    }

    public static ProductNotFoundException byId(Long id) {
        return new ProductNotFoundException("Товар с id " + id + " не найден");
    }
}
