package com.regalos.back_regalos.repositories;

import com.regalos.back_regalos.models.StoreSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreSettingRepository extends JpaRepository<StoreSetting, Long> {
    Optional<StoreSetting> findByConfigKey(String configKey);
}
