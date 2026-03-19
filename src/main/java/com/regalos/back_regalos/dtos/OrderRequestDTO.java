package com.regalos.back_regalos.dtos;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDTO {
    private Long customerId; // Opcional si ya existe
    private String documentType;
    private String documentNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    
    private Long deliveryCityId;
    private String deliveryAddress;
    private String deliveryNeighborhood;
    private String deliveryInstructions;
    private String deliveryDate; // String para validación manual o LocalDate
    private String deliveryTimeRange;
    
    private String giftSenderName;
    private String giftReceiverName;
    private String giftMessage;
    
    private List<CartItemDTO> items;
}
