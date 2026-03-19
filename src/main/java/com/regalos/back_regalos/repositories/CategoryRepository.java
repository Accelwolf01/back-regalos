package com.regalos.back_regalos.repositories;

import com.regalos.back_regalos.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByIsActive(Boolean isActive);
}
