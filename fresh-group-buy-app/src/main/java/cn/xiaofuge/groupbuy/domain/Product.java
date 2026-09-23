package cn.xiaofuge.groupbuy.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Product(
        String id,
        String name,
        String category,
        String emoji,
        String tagline,
        String description,
        BigDecimal price,
        BigDecimal originalPrice,
        String unit,
        int stock,
        int sold,
        int minGroupSize,
        int currentParticipants,
        LocalDateTime groupDeadline,
        String supplier,
        String freshTip,
        List<String> tags
) {
}
