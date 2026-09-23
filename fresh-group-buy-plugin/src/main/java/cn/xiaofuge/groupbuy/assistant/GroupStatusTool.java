package cn.xiaofuge.groupbuy.assistant;

import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolDefinition;
import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolExecutionResult;
import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolRunContext;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class GroupStatusTool extends AbstractGroupBuyTool {
    GroupStatusTool(GroupBuyApiClient client) {
        super(client);
    }

    @Override
    public String name() {
        return "group_status";
    }

    @Override
    public String description() {
        return "查询指定商品的拼团进度、剩余名额、截止时间和供应商。";
    }

    @Override
    public Map<String, Object> parameters() {
        return objectSchema()
                .prop("productId", stringSchema("商品 ID，例如 g001"))
                .required("productId")
                .build();
    }

    @Override
    protected CompletableFuture<ToolExecutionResult> run(Map<String, Object> args, ToolRunContext context) {
        return callApp("/api/groupbuy/products/" + str(args, "productId", "").trim() + "/group");
    }
}
