package cn.xiaofuge.groupbuy.assistant;

import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolDefinition;
import cn.xiaofuge.deepseek.harness.domain.spi.AbstractHarnessPlugin;
import cn.xiaofuge.deepseek.harness.domain.spi.PluginContext;
import java.util.List;

public class FreshGroupBuyAssistantPlugin extends AbstractHarnessPlugin {
    private static final String PLUGIN_ID = "fresh-group-buy-assistant";
    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:18082";
    private final GroupBuyApiClient client = new GroupBuyApiClient(DEFAULT_BASE_URL);

    public FreshGroupBuyAssistantPlugin() {
        super(PLUGIN_ID);
    }

    @Override
    public List<ToolDefinition> tools() {
        return List.of(
                new SearchProductsTool(client),
                new ProductDetailTool(client),
                new GroupStatusTool(client),
                new PickupPointsTool(client),
                new OrderQueryTool(client)
        );
    }

    @Override
    public void configure(PluginContext context) {
        super.configure(context);
        client.setBaseUrl(context.getConfig("groupbuy.base-url", DEFAULT_BASE_URL));
        context.registerSystemPrompt("groupbuy-capabilities", 10, """
                ## Fresh Group Buy assistant
                你是生鲜社区团购助手。

                ## 硬性规则
                1. 商品推荐、价格、库存、拼团问题必须先调用 `search_products` 或 `product_detail`。
                2. 成团进度、剩余名额、截止时间必须调用 `group_status`。
                3. 自提点问题必须调用 `pickup_points`。
                4. 订单问题必须调用 `order_query`，未提供 customerId 时使用 `customer-1`。
                5. 不使用工具之外的任何商品、价格、库存、拼团或订单数据，也不虚构外部商家。
                6. 不承诺真实配送时效、退款、食品安全或法律义务。
                7. 用简洁中文回答，优先使用工具返回的真实数据。
                """);

        context.registerHook("POST_TOOL_USE", (toolName, payloadJson) -> {
            if (toolName != null && toolName.contains(PLUGIN_ID)) {
                context.emit("fresh-group-buy-assistant.tool.used", java.util.Map.of(
                        "tool", toolName,
                        "payload", payloadJson == null ? "" : payloadJson
                ));
            }
            return null;
        });

        context.registerDisposer(() -> context.emit("fresh-group-buy-assistant.plugin.closed", "plugin shutdown"));
    }
}
