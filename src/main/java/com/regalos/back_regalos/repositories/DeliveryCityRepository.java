package com.regalos.back_regalos.repositories;

import com.regalos.back_regalos.models.DeliveryCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryCityRepository extends JpaRepository<DeliveryCity, Long> {
    List<DeliveryCity> findAllByIsActiveTrue();
    List<DeliveryCity> findAllByIsActive(Boolean isActive);
}
