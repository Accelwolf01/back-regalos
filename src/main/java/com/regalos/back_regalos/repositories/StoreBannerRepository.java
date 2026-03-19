package com.regalos.back_regalos.repositories;

import com.regalos.back_regalos.models.StoreBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreBannerRepository extends JpaRepository<StoreBanner, Long> {
    List<StoreBanner> findAllByIsActiveOrderByDisplayOrderAsc(Boolean isActive);
}
