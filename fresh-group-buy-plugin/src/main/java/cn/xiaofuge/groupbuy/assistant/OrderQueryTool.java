package cn.xiaofuge.groupbuy.assistant;

import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolDefinition;
import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolExecutionResult;
import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolRunContext;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class OrderQueryTool extends AbstractGroupBuyTool {
    OrderQueryTool(GroupBuyApiClient client) {
        super(client);
    }

    @Override
    public String name() {
        return "order_query";
    }

    @Override
    public String description() {
        return "查询指定用户的生鲜团购订单列表，包含状态、金额、自提点和取货码。";
    }

    @Override
    public Map<String, Object> parameters() {
        return objectSchema()
                .prop("customerId", stringSchema("用户 ID，默认 customer-1"))
                .build();
    }

    @Override
    protected CompletableFuture<ToolExecutionResult> run(Map<String, Object> args, ToolRunContext context) {
        String customerId = str(args, "customerId", "customer-1");
        return callApp("/api/groupbuy/orders?customerId=" + URLEncoder.encode(customerId, StandardCharsets.UTF_8));
    }
}
