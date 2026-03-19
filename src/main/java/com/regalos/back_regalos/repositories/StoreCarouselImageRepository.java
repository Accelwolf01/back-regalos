package com.regalos.back_regalos.repositories;

import com.regalos.back_regalos.models.StoreCarouselImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreCarouselImageRepository extends JpaRepository<StoreCarouselImage, Long> {
    List<StoreCarouselImage> findAllByIsActiveOrderByDisplayOrderAsc(Boolean isActive);
}
