package com.regalos.back_regalos.controllers;

import com.regalos.back_regalos.dtos.OrderRequestDTO;
import com.regalos.back_regalos.dtos.PaymentInitResponseDTO;
import com.regalos.back_regalos.models.Order;
import com.regalos.back_regalos.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos y Pagos", description = "Endpoints para el flujo de compra y Bold")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/init-payment")
    @Operation(summary = "Paso 1: Valida el carrito y devuelve las llaves y hashes para el botón de Bold")
    public ResponseEntity<PaymentInitResponseDTO> init(@RequestBody OrderRequestDTO request) {
        return ResponseEntity.ok(orderService.preparePayment(request));
    }

    @PostMapping("/confirm")
    @Operation(summary = "Paso 2: Confirma el pago con Bold y crea la orden definitivamente en la BD")
    public ResponseEntity<?> confirm(
            @RequestBody OrderRequestDTO request,
            @RequestParam String boldTransactionId,
            @RequestParam String orderRef) {
        
        System.out.println(">>> Confirmando pedido: " + orderRef + " con tx: " + boldTransactionId);
        try {
            Order order = orderService.confirmAndCreateOrder(request, boldTransactionId, orderRef);
            System.out.println(">>> Pedido creado exitosamente: " + order.getId());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            System.err.println(">>> ERROR CREANDO PEDIDO: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al crear el pedido: " + e.getMessage());
        }
    }

    @GetMapping("/track/{trackingCode}")
    @Operation(summary = "Público: Permite rastrear un pedido mediante su código único")
    public ResponseEntity<Order> track(@PathVariable String trackingCode) {
        return ResponseEntity.ok(orderService.getByTrackingCode(trackingCode));
    }
}
