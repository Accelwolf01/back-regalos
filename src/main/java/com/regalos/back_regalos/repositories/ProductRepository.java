package com.regalos.back_regalos.repositories;

import com.regalos.back_regalos.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByIsActive(Boolean isActive);
    List<Product> findByCategoryIdAndIsActive(Long categoryId, Boolean isActive);
}
