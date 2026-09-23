package cn.xiaofuge.groupbuy.assistant;

import cn.xiaofuge.deepseek.harness.domain.model.entity.AbstractTool;
import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolExecutionResult;
import java.util.concurrent.CompletableFuture;

abstract class AbstractGroupBuyTool extends AbstractTool {
    final GroupBuyApiClient client;

    AbstractGroupBuyTool(GroupBuyApiClient client) {
        this.client = client;
    }

    @Override
    public boolean isConcurrencySafe(Object args) {
        return true;
    }

    protected CompletableFuture<ToolExecutionResult> callApp(String path) {
        try {
            return ok(client.get(path));
        } catch (Exception exception) {
            return fail(exception.getMessage(), "GROUP_BUY_API_ERROR");
        }
    }
}
