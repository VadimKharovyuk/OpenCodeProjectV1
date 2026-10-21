package com.example.opencodeproject.repository;

import com.example.opencodeproject.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    List<Product> findAllByOrderByCreatedAtDesc();

    boolean existsBySlug(String slug);
}
