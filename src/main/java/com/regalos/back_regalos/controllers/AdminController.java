package com.regalos.back_regalos.controllers;

import com.regalos.back_regalos.dtos.FinancialReportDTO;
import com.regalos.back_regalos.models.*;
import com.regalos.back_regalos.repositories.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Administración", description = "Endpoints para finanzas y control administrativo")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdminController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductImageRepository productImageRepository;
    private final DeliveryCityRepository deliveryCityRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final StoreSettingRepository storeSettingRepository;

    @GetMapping("/reports/financial")
    @Operation(summary = "Genera un resumen financiero filtrado por fechas")
    public ResponseEntity<FinancialReportDTO> getFinancialSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        List<Order> orders;
        if (startDate != null && endDate != null) {
            java.time.LocalDateTime start = java.time.LocalDate.parse(startDate).atStartOfDay();
            java.time.LocalDateTime end = java.time.LocalDate.parse(endDate).atTime(23, 59, 59);
            orders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        } else {
            orders = orderRepository.findAll();
        }
        
        BigDecimal totalSales = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCosts = orders.stream()
                .map(Order::getTotalCostAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return ResponseEntity.ok(FinancialReportDTO.builder()
                .totalSales(totalSales)
                .totalCosts(totalCosts)
                .netProfit(totalSales.subtract(totalCosts))
                .orderCount((long) orders.size())
                .build());
    }

    @GetMapping("/reports/orders/export")
    @Operation(summary = "Obtiene lista de pedidos detallada para exportación")
    public ResponseEntity<List<Order>> getOrdersForExport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        if (startDate != null && endDate != null) {
            java.time.LocalDateTime start = java.time.LocalDate.parse(startDate).atStartOfDay();
            java.time.LocalDateTime end = java.time.LocalDate.parse(endDate).atTime(23, 59, 59);
            return ResponseEntity.ok(orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end));
        }
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @GetMapping("/orders")
    @Operation(summary = "Lista todos los pedidos realizados")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    // --- GESTIÓN DE PRODUCTOS ---

    @GetMapping("/products")
    @Operation(summary = "Lista todos los productos")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @PostMapping("/products")
    @Operation(summary = "Crea un producto con imágenes")
    @Transactional
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        List<ProductImage> images = product.getImages();
        product.setImages(null);
        Product savedProduct = productRepository.save(product);
        
        if (images != null) {
            for (ProductImage img : images) {
                img.setProduct(savedProduct);
                productImageRepository.save(img);
            }
        }
        return ResponseEntity.ok(productRepository.findById(savedProduct.getId()).orElse(savedProduct));
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Actualiza producto e imágenes")
    @Transactional
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return productRepository.findById(id).map(product -> {
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setCostPrice(productDetails.getCostPrice());
            product.setStock(productDetails.getStock());
            product.setIsActive(productDetails.getIsActive());
            product.setCategory(productDetails.getCategory());
            product.setSupplier(productDetails.getSupplier());
            
            Product savedProduct = productRepository.save(product);

            if (productDetails.getImages() != null) {
                List<ProductImage> oldImages = productImageRepository.findByProductIdAndIsActiveOrderByDisplayOrderAsc(id, true);
                for(ProductImage old : oldImages) {
                    old.setIsActive(false);
                    productImageRepository.save(old);
                }
                for (ProductImage img : productDetails.getImages()) {
                    img.setProduct(savedProduct);
                    img.setIsActive(true);
                    productImageRepository.save(img);
                }
            }
            return ResponseEntity.ok(productRepository.findById(id).orElse(savedProduct));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Desactiva producto")
    @Transactional
    public ResponseEntity<Void> deactivateProduct(@PathVariable Long id) {
        return productRepository.findById(id).map(p -> {
            p.setIsActive(false);
            productRepository.save(p);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- GESTIÓN DE CATEGORÍAS ---

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @PostMapping("/categories")
    @Transactional
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @PutMapping("/categories/{id}")
    @Transactional
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category details) {
        return categoryRepository.findById(id).map(c -> {
            c.setName(details.getName());
            c.setDescription(details.getDescription());
            c.setIsActive(details.getIsActive());
            return ResponseEntity.ok(categoryRepository.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- GESTIÓN DE CIUDADES ---

    @GetMapping("/cities")
    public ResponseEntity<List<DeliveryCity>> getCities() {
        return ResponseEntity.ok(deliveryCityRepository.findAll());
    }

    @PostMapping("/cities")
    @Transactional
    public ResponseEntity<DeliveryCity> createCity(@RequestBody DeliveryCity city) {
        return ResponseEntity.ok(deliveryCityRepository.save(city));
    }

    @PutMapping("/cities/{id}")
    @Transactional
    public ResponseEntity<DeliveryCity> updateCity(@PathVariable Long id, @RequestBody DeliveryCity details) {
        return deliveryCityRepository.findById(id).map(c -> {
            c.setDepartment(details.getDepartment());
            c.setName(details.getName());
            c.setDeliveryCost(details.getDeliveryCost());
            c.setIsActive(details.getIsActive());
            return ResponseEntity.ok(deliveryCityRepository.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- GESTIÓN DE ESTADOS ---

    @GetMapping("/statuses")
    public ResponseEntity<List<OrderStatus>> getOrderStatuses() {
        return ResponseEntity.ok(orderStatusRepository.findAll());
    }

    @PutMapping("/orders/{orderId}/status/{statusId}")
    @Transactional
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long orderId, @PathVariable Long statusId) {
        return orderRepository.findById(orderId).flatMap(o -> 
            orderStatusRepository.findById(statusId).map(s -> {
                o.setOrderStatus(s);
                return ResponseEntity.ok(orderRepository.save(o));
            })
        ).orElse(ResponseEntity.notFound().build());
    }

    // --- GESTIÓN DE USUARIOS ---

    @GetMapping("/users")
    @Operation(summary = "Lista todos los usuarios")
    public ResponseEntity<List<AppUser>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/users/{id}/deactivate")
    @Transactional
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            user.setIsActive(false);
            userRepository.save(user);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/reset-password")
    @Transactional
    public ResponseEntity<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newPassword = payload.get("password");
        return userRepository.findById(id).map(user -> {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/role/{roleId}")
    @Transactional
    public ResponseEntity<Void> changeUserRole(@PathVariable Long id, @PathVariable Long roleId) {
        return userRepository.findById(id).flatMap(user ->
            roleRepository.findById(roleId).map(role -> {
                user.setRole(role);
                userRepository.save(user);
                return ResponseEntity.noContent().<Void>build();
            })
        ).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    // --- GESTIÓN DE PROVEEDORES ---
    @GetMapping("/suppliers")
    public ResponseEntity<List<Supplier>> getSuppliers() {
        return ResponseEntity.ok(supplierRepository.findAll());
    }

    @PostMapping("/suppliers")
    @Transactional
    public ResponseEntity<Supplier> createSupplier(@RequestBody Supplier supplier) {
        return ResponseEntity.ok(supplierRepository.save(supplier));
    }

    @PutMapping("/suppliers/{id}")
    @Transactional
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier details) {
        return supplierRepository.findById(id).map(s -> {
            s.setName(details.getName());
            s.setContactName(details.getContactName());
            s.setPhone(details.getPhone());
            s.setEmail(details.getEmail());
            s.setAddress(details.getAddress());
            s.setIsActive(details.getIsActive());
            return ResponseEntity.ok(supplierRepository.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/suppliers/{id}")
    @Transactional
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        return supplierRepository.findById(id).map(s -> {
            s.setIsActive(false);
            supplierRepository.save(s);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
