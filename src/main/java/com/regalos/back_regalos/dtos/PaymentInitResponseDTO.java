package com.regalos.back_regalos.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentInitResponseDTO {
    private String orderId;
    private Long amount;
    private String currency;
    private String integritySignature;
    private String apiKey;
}
