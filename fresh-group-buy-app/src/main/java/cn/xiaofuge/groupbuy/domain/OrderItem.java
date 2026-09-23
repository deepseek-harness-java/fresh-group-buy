package cn.xiaofuge.groupbuy.domain;

import java.math.BigDecimal;

public record OrderItem(
        String productId,
        String name,
        String emoji,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal
) {
}
