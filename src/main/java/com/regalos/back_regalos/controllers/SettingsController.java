package com.regalos.back_regalos.controllers;

import com.regalos.back_regalos.models.*;
import com.regalos.back_regalos.repositories.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Tag(name = "Ajustes", description = "Configuración de la tienda y banners")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SettingsController {

    private final StoreSettingRepository settingRepository;
    private final StoreBannerRepository bannerRepository;

    @GetMapping("/public")
    @Operation(summary = "Obtiene configuraciones públicas")
    public ResponseEntity<List<StoreSetting>> getPublicSettings() {
        return ResponseEntity.ok(settingRepository.findAll().stream()
                .filter(s -> !s.getConfigKey().contains("secret"))
                .collect(Collectors.toList()));
    }

    @GetMapping("/banners")
    @Operation(summary = "Obtiene los banners activos para el inicio")
    public ResponseEntity<List<StoreBanner>> getPublicBanners() {
        return ResponseEntity.ok(bannerRepository.findAllByIsActiveOrderByDisplayOrderAsc(true));
    }

    // --- ADMIN ENDPOINTS ---

    @GetMapping("/admin")
    @Operation(summary = "Obtiene todas las configuraciones (SÓLO ADMIN)")
    public ResponseEntity<List<StoreSetting>> getAdminSettings() {
        return ResponseEntity.ok(settingRepository.findAll());
    }

    @PutMapping("/admin")
    @Transactional
    public ResponseEntity<Void> updateSettings(@RequestBody List<StoreSetting> settings) {
        for (StoreSetting setting : settings) {
            Optional<StoreSetting> existing = settingRepository.findByConfigKey(setting.getConfigKey());
            if (existing.isPresent()) {
                StoreSetting s = existing.get();
                s.setConfigValue(setting.getConfigValue());
                if(setting.getConfigGroup() != null) s.setConfigGroup(setting.getConfigGroup());
                settingRepository.save(s);
            } else {
                settingRepository.save(setting);
            }
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/banners")
    public ResponseEntity<List<StoreBanner>> getAdminBanners() {
        return ResponseEntity.ok(bannerRepository.findAll());
    }

    @PostMapping("/admin/banners")
    @Transactional
    public ResponseEntity<StoreBanner> createBanner(@RequestBody StoreBanner banner) {
        // Validar máximo 5
        if (bannerRepository.count() >= 5) {
            throw new RuntimeException("Máximo 5 banners permitidos");
        }
        return ResponseEntity.ok(bannerRepository.save(banner));
    }

    @PutMapping("/admin/banners/{id}")
    @Transactional
    public ResponseEntity<StoreBanner> updateBanner(@PathVariable Long id, @RequestBody StoreBanner details) {
        return bannerRepository.findById(id).map(b -> {
            b.setTitle(details.getTitle());
            b.setSubtitle(details.getSubtitle());
            b.setImageBase64(details.getImageBase64());
            b.setDisplayOrder(details.getDisplayOrder());
            b.setIsActive(details.getIsActive());
            return ResponseEntity.ok(bannerRepository.save(b));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/banners/{id}")
    @Transactional
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        bannerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
