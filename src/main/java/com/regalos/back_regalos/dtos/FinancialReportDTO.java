package com.regalos.back_regalos.dtos;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class FinancialReportDTO {
    private BigDecimal totalSales;
    private BigDecimal totalCosts;
    private BigDecimal netProfit;
    private Long orderCount;
}
