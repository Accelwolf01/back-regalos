package com.regalos.back_regalos.controllers;

import com.regalos.back_regalos.models.StoreSetting;
import com.regalos.back_regalos.repositories.StoreSettingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Tag(name = "Configuración", description = "Endpoints para obtener datos de la tienda")
@CrossOrigin(origins = "*")
public class SettingController {

    private final StoreSettingRepository settingRepository;

    @GetMapping
    @Operation(summary = "Obtiene todas las configuraciones públicas de la tienda")
    public ResponseEntity<List<StoreSetting>> getAll() {
        return ResponseEntity.ok(settingRepository.findAll());
    }

    @GetMapping("/{key}")
    @Operation(summary = "Obtiene una configuración específica por su clave")
    public ResponseEntity<StoreSetting> getByKey(@PathVariable String key) {
        return settingRepository.findByConfigKey(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
