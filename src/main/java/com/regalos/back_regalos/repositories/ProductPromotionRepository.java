package com.regalos.back_regalos.repositories;

import com.regalos.back_regalos.models.ProductPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductPromotionRepository extends JpaRepository<ProductPromotion, Long> {
    @Query("SELECT p FROM ProductPromotion p WHERE p.isActive = true AND p.startDate <= :now AND p.endDate >= :now")
    List<ProductPromotion> findActivePromotions(LocalDateTime now);
}
