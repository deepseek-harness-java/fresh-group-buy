package cn.xiaofuge.groupbuy.api;

import cn.xiaofuge.groupbuy.service.GroupBuyService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/groupbuy")
public class GroupBuyController {
    private final GroupBuyService service;

    public GroupBuyController(GroupBuyService service) {
        this.service = service;
    }

    @GetMapping("/products")
    public Map<String, Object> products(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "全部") String category
    ) {
        return Map.of("code", 0, "message", "ok", "data", service.searchProducts(keyword, category));
    }

    @GetMapping("/products/{productId}")
    public Map<String, Object> product(@PathVariable String productId) {
        return Map.of("code", 0, "message", "ok", "data", service.getProduct(productId));
    }

    @GetMapping("/products/{productId}/group")
    public Map<String, Object> group(@PathVariable String productId) {
        return Map.of("code", 0, "message", "ok", "data", service.getGroup(productId));
    }

    @GetMapping("/pickup-points")
    public Map<String, Object> pickupPoints(@RequestParam(required = false) String keyword) {
        return Map.of("code", 0, "message", "ok", "data", service.listPickupPoints(keyword));
    }

    @GetMapping("/pickup-points/{pickupPointId}")
    public Map<String, Object> pickupPoint(@PathVariable String pickupPointId) {
        return Map.of("code", 0, "message", "ok", "data", service.getPickupPoint(pickupPointId));
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> placeOrder(@RequestBody PlaceOrderRequest request) {
        return Map.of("code", 0, "message", "下单成功", "data",
                service.placeOrder(request.customerId(), request.productId(), request.quantity(), request.pickupPointId()));
    }

    @GetMapping("/orders")
    public Map<String, Object> orders(@RequestParam String customerId) {
        return Map.of("code", 0, "message", "ok", "data", service.listOrders(customerId));
    }

    @GetMapping("/orders/{orderNo}")
    public Map<String, Object> order(@RequestParam String customerId, @PathVariable String orderNo) {
        return Map.of("code", 0, "message", "ok", "data", service.getOrder(customerId, orderNo));
    }

    public record PlaceOrderRequest(
            @NotBlank String customerId,
            @NotBlank String productId,
            @Min(1) @Max(10) int quantity,
            @NotBlank String pickupPointId
    ) {
    }
}
