package com.regalos.back_regalos.repositories;

import com.regalos.back_regalos.models.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {
    List<OrderStatus> findAllByIsActiveOrderByDisplayOrderAsc(Boolean isActive);
}
