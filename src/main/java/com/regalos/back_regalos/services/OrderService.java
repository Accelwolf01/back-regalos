package com.regalos.back_regalos.services;

import com.regalos.back_regalos.dtos.CartItemDTO;
import com.regalos.back_regalos.dtos.OrderRequestDTO;
import com.regalos.back_regalos.dtos.PaymentInitResponseDTO;
import com.regalos.back_regalos.models.*;
import com.regalos.back_regalos.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CustomerRepository customerRepository;
    private final DeliveryCityRepository cityRepository;
    private final OrderStatusRepository statusRepository;
    private final StoreSettingRepository settingRepository;
    private final BoldService boldService;

    public PaymentInitResponseDTO preparePayment(OrderRequestDTO request) {
        // Validación básica
        BigDecimal totalProducts = BigDecimal.ZERO;
        for (CartItemDTO item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductId()));
            
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("Stock insuficiente para: " + product.getName());
            }
            totalProducts = totalProducts.add(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }

        DeliveryCity city = cityRepository.findById(request.getDeliveryCityId())
                .orElseThrow(() -> new RuntimeException("Ciudad de entrega no válida"));
        
        BigDecimal totalOrder = totalProducts.add(city.getDeliveryCost());
        
        String orderRef = "ORD" + System.currentTimeMillis();
        String integritySignature = boldService.generateIntegritySignature(orderRef, totalOrder.longValue(), "COP");
        
        String apiKey = settingRepository.findByConfigKey("bold_api_key")
                .map(s -> s.getConfigValue())
                .orElse("");

        return PaymentInitResponseDTO.builder()
                .orderId(orderRef)
                .amount(totalOrder.longValue())
                .currency("COP")
                .integritySignature(integritySignature)
                .apiKey(apiKey)
                .build();
    }

    @Transactional
    public Order confirmAndCreateOrder(OrderRequestDTO request, String boldTransactionId, String orderRef) {
        System.out.println(">>> Procesando confirmación para ref: " + orderRef + " | Items: " + (request.getItems() != null ? request.getItems().size() : 0));
        
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("El pedido no tiene productos");
        }
        
        if (request.getDeliveryDate() == null || request.getDeliveryDate().isEmpty()) {
            throw new RuntimeException("La fecha de entrega es obligatoria");
        }

        // 1. Buscar o crear cliente
        Customer customer = customerRepository.findByDocumentNumber(request.getDocumentNumber())
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .documentType(request.getDocumentType())
                        .documentNumber(request.getDocumentNumber())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .build()));

        // 2. Calcular montos finales y costos
        BigDecimal productsAmount = BigDecimal.ZERO;
        BigDecimal productsCost = BigDecimal.ZERO;
        List<OrderItem> itemsToSave = new ArrayList<>();

        for (CartItemDTO item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
            productService.reduceStock(product.getId(), item.getQuantity());

            BigDecimal unitPrice = product.getPrice();
            BigDecimal unitCost = product.getCostPrice();
            BigDecimal subtotal = unitPrice.multiply(new BigDecimal(item.getQuantity()));
            BigDecimal subtotalCost = unitCost.multiply(new BigDecimal(item.getQuantity()));

            productsAmount = productsAmount.add(subtotal);
            productsCost = productsCost.add(subtotalCost);

            itemsToSave.add(OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .unitPrice(unitPrice)
                    .unitCostPrice(unitCost)
                    .quantity(item.getQuantity())
                    .subtotal(subtotal)
                    .subtotalCost(subtotalCost)
                    .build());
        }

        DeliveryCity city = cityRepository.findById(request.getDeliveryCityId())
                .orElseThrow(() -> new RuntimeException("Ciudad de entrega no encontrada con ID: " + request.getDeliveryCityId()));
        
        BigDecimal totalOrder = productsAmount.add(city.getDeliveryCost());
        
        List<OrderStatus> statuses = statusRepository.findAllByIsActiveOrderByDisplayOrderAsc(true);
        if (statuses.isEmpty()) {
            throw new RuntimeException("No se encontraron estados de pedido activos en la base de datos.");
        }
        OrderStatus initialStatus = statuses.get(0);

        // 3. Guardar Orden
        System.out.println(">>> Guardando orden para el cliente: " + customer.getEmail());
        Order order = Order.builder()
                .customer(customer)
                .subtotalAmount(productsAmount)
                .deliveryCost(city.getDeliveryCost())
                .totalAmount(totalOrder)
                .totalCostAmount(productsCost)
                .deliveryCity(city)
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryNeighborhood(request.getDeliveryNeighborhood())
                .deliveryInstructions(request.getDeliveryInstructions())
                .deliveryDate(LocalDate.parse(request.getDeliveryDate()))
                .deliveryTimeRange(request.getDeliveryTimeRange())
                .giftSenderName(request.getGiftSenderName())
                .giftReceiverName(request.getGiftReceiverName())
                .giftMessage(request.getGiftMessage())
                .trackingCode("TRK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .orderStatus(initialStatus)
                .paymentStatus("PAID")
                .boldTransactionId(boldTransactionId)
                .paymentMethod("BOLD")
                .build();

        Order savedOrder = orderRepository.save(order);

        // 4. Guardar Items vinculados
        for (OrderItem oi : itemsToSave) {
            oi.setOrder(savedOrder);
            orderItemRepository.save(oi);
        }

        return savedOrder;
    }

    public Order getByTrackingCode(String trackingCode) {
        return orderRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con el código: " + trackingCode));
    }
}
