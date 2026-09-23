package cn.xiaofuge.groupbuy.service;

import cn.xiaofuge.groupbuy.domain.Order;
import cn.xiaofuge.groupbuy.domain.OrderItem;
import cn.xiaofuge.groupbuy.domain.PickupPoint;
import cn.xiaofuge.groupbuy.domain.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GroupBuyService {
    private final List<Product> products = GroupBuyData.products();
    private final List<PickupPoint> pickupPoints = GroupBuyData.pickupPoints();
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong orderSequence = new AtomicLong(1000);

    public List<Product> searchProducts(String keyword, String category) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedCategory = category == null || category.isBlank() ? "全部" : category.trim();
        return products.stream()
                .filter(item -> "全部".equals(normalizedCategory) || item.category().equals(normalizedCategory))
                .filter(item -> matchesKeyword(item, normalizedKeyword))
                .sorted(Comparator.comparing(Product::currentParticipants).reversed())
                .toList();
    }

    public Product getProduct(String productId) {
        return products.stream().filter(item -> item.id().equals(productId)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在"));
    }

    public Map<String, Object> getGroup(String productId) {
        Product product = getProduct(productId);
        int remaining = Math.max(product.minGroupSize() - product.currentParticipants(), 0);
        int percent = Math.min(100, product.currentParticipants() * 100 / product.minGroupSize());
        return Map.of(
                "productId", product.id(),
                "name", product.name(),
                "price", product.price(),
                "minGroupSize", product.minGroupSize(),
                "currentParticipants", product.currentParticipants(),
                "remaining", remaining,
                "progressPercent", percent,
                "groupDeadline", product.groupDeadline(),
                "supplier", product.supplier()
        );
    }

    public List<PickupPoint> listPickupPoints(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return pickupPoints.stream()
                .filter(point -> normalizedKeyword.isEmpty()
                        || point.name().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || point.address().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || point.tags().stream().anyMatch(tag -> tag.toLowerCase(Locale.ROOT).contains(normalizedKeyword)))
                .sorted(Comparator.comparing(PickupPoint::distanceKm))
                .toList();
    }

    public PickupPoint getPickupPoint(String pickupPointId) {
        return pickupPoints.stream().filter(item -> item.id().equals(pickupPointId)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "自提点不存在"));
    }

    public synchronized Order placeOrder(String customerId, String productId, int quantity, String pickupPointId) {
        if (customerId == null || customerId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId 不能为空");
        }
        Product product = getProduct(productId);
        PickupPoint pickupPoint = getPickupPoint(pickupPointId);
        if (quantity < 1 || quantity > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "数量需在 1-10 之间");
        }
        if (product.stock() < quantity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "库存不足");
        }
        BigDecimal subtotal = product.price().multiply(BigDecimal.valueOf(quantity));
        Order order = new Order(
                generateOrderNo(), customerId, pickupPoint.id(), pickupPoint.name(),
                List.of(new OrderItem(product.id(), product.name(), product.emoji(), product.price(), quantity, subtotal)),
                subtotal, "待成团", LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                pickupCode()
        );
        orders.put(order.orderNo(), order);
        return order;
    }

    public List<Order> listOrders(String customerId) {
        return orders.values().stream()
                .filter(order -> order.customerId().equals(customerId))
                .sorted(Comparator.comparing(Order::createdAt).reversed())
                .toList();
    }

    public Order getOrder(String customerId, String orderNo) {
        Order order = orders.get(orderNo);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        if (!order.customerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权查看该订单");
        }
        return order;
    }

    private boolean matchesKeyword(Product product, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        return product.name().toLowerCase(Locale.ROOT).contains(keyword)
                || product.category().contains(keyword)
                || product.tagline().contains(keyword)
                || product.description().contains(keyword)
                || product.supplier().toLowerCase(Locale.ROOT).contains(keyword)
                || product.tags().stream().anyMatch(tag -> tag.toLowerCase(Locale.ROOT).contains(keyword));
    }

    private String generateOrderNo() {
        return "GB" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%04d", orderSequence.incrementAndGet());
    }

    private String pickupCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
    }
}
