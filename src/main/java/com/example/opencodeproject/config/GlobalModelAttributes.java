package com.example.opencodeproject.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.Year;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("year")
    public int year() {
        return Year.now().getValue();
    }
}
