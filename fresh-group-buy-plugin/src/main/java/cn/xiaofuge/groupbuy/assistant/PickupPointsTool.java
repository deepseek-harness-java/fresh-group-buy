package cn.xiaofuge.groupbuy.assistant;

import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolDefinition;
import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolExecutionResult;
import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolRunContext;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class PickupPointsTool extends AbstractGroupBuyTool {
    PickupPointsTool(GroupBuyApiClient client) {
        super(client);
    }

    @Override
    public String name() {
        return "pickup_points";
    }

    @Override
    public String description() {
        return "查询社区自提点列表，包含地址、营业时间、距离、容量和特色服务。";
    }

    @Override
    public Map<String, Object> parameters() {
        return objectSchema()
                .prop("keyword", stringSchema("自提点名称、地址或服务关键词，可为空"))
                .build();
    }

    @Override
    protected CompletableFuture<ToolExecutionResult> run(Map<String, Object> args, ToolRunContext context) {
        String keyword = str(args, "keyword", "");
        return callApp("/api/groupbuy/pickup-points?keyword=" + URLEncoder.encode(keyword == null ? "" : keyword, StandardCharsets.UTF_8));
    }
}
