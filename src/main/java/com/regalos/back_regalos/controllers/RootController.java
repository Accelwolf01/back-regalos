package com.regalos.back_regalos.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Health Check", description = "Endpoints de verificación del sistema")
public class RootController {

    @GetMapping("/")
    @Operation(summary = "Verifica que el servidor esté vivo y funcionando")
    public Map<String, String> getStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "API de Tienda de Regalos corriendo correctamente");
        status.put("docs", "/swagger-ui.html");
        return status;
    }
}
