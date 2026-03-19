package com.regalos.back_regalos.repositories;

import com.regalos.back_regalos.models.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdAndIsActiveOrderByDisplayOrderAsc(Long productId, Boolean isActive);
}
