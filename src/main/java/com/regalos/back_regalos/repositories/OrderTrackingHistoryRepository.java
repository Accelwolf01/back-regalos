package com.regalos.back_regalos.repositories;

import com.regalos.back_regalos.models.OrderTrackingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderTrackingHistoryRepository extends JpaRepository<OrderTrackingHistory, Long> {
    List<OrderTrackingHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
