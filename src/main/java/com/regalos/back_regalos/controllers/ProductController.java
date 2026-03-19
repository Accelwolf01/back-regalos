package com.regalos.back_regalos.controllers;

import com.regalos.back_regalos.models.Product;
import com.regalos.back_regalos.models.ProductImage;
import com.regalos.back_regalos.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Catálogo", description = "Endpoints para ver productos y categorías")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Lista todos los productos activos y con stock")
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(productService.getActiveProducts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de un producto")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/images")
    @Operation(summary = "Obtiene el carrusel de imágenes (Base64) de un producto")
    public ResponseEntity<List<ProductImage>> getImages(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductImages(id));
    }
}
