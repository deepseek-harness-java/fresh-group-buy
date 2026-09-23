package cn.xiaofuge.groupbuy.assistant;

import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolDefinition;
import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolExecutionResult;
import cn.xiaofuge.deepseek.harness.domain.model.entity.ToolRunContext;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class SearchProductsTool extends AbstractGroupBuyTool {
    SearchProductsTool(GroupBuyApiClient client) {
        super(client);
    }

    @Override
    public String name() {
        return "search_products";
    }

    @Override
    public String description() {
        return "按关键词或分类搜索今日生鲜团购商品，返回价格、库存、成团人数和卖点。";
    }

    @Override
    public Map<String, Object> parameters() {
        return objectSchema()
                .prop("keyword", stringSchema("商品名、用途、标签或分类关键词，可为空"))
                .prop("category", stringSchema("分类，如 水果、蔬菜、肉禽、海鲜"))
                .build();
    }

    @Override
    protected CompletableFuture<ToolExecutionResult> run(Map<String, Object> args, ToolRunContext context) {
        String keyword = str(args, "keyword", "");
        String category = str(args, "category", "全部");
        return callApp("/api/groupbuy/products?keyword=" + encode(keyword) + "&category=" + encode(category));
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
