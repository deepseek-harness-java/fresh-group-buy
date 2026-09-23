package cn.xiaofuge.groupbuy.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Order(
        String orderNo,
        String customerId,
        String pickupPointId,
        String pickupPointName,
        List<OrderItem> items,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createdAt,
        LocalDateTime pickupDate,
        String pickupCode
) {
}
