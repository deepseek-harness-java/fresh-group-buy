package cn.xiaofuge.groupbuy.assistant;

import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolDefinition;
import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolExecutionResult;
import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolRunContext;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class ProductDetailTool extends AbstractGroupBuyTool {
    ProductDetailTool(GroupBuyApiClient client) {
        super(client);
    }

    @Override
    public String name() {
        return "product_detail";
    }

    @Override
    public String description() {
        return "查询单个生鲜商品的完整详情，包括价格、规格、库存、供应商和保鲜建议。";
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
        return callApp("/api/groupbuy/products/" + str(args, "productId", "").trim());
    }
}
