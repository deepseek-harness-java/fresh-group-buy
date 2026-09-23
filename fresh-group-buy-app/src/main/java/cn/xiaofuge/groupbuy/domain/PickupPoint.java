package cn.xiaofuge.groupbuy.domain;

import java.time.LocalTime;
import java.util.List;

public record PickupPoint(
        String id,
        String name,
        String address,
        String contact,
        double distanceKm,
        LocalTime openTime,
        LocalTime closeTime,
        int capacity,
        int currentOrders,
        List<String> tags
) {
}
