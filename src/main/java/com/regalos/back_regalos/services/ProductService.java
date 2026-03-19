package com.regalos.back_regalos.services;

import com.regalos.back_regalos.models.Product;
import com.regalos.back_regalos.models.ProductImage;
import com.regalos.back_regalos.repositories.ProductImageRepository;
import com.regalos.back_regalos.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        // Solo productos activos y con stock > 0
        List<Product> products = productRepository.findAllByIsActive(true).stream()
                .filter(p -> p.getStock() > 0)
                .toList();
        
        // Inicializar imágenes para el catálogo
        products.forEach(p -> {
            if (p.getImages() != null) p.getImages().size(); 
        });
        
        return products;
    }

    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductIdAndIsActiveOrderByDisplayOrderAsc(productId, true);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public void reduceStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productId));
        
        if (product.getStock() < quantity) {
            throw new RuntimeException("Stock insuficiente para el producto: " + product.getName());
        }
        
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}
