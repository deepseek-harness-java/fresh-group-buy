# Fresh Group Buy 交付验收

## 交付物

| 项目 | 状态 | 说明 |
|---|---|---|
| 业务应用 | ✅ | Spring Boot 3.3.2 + 原生前端 |
| DSH 插件 | ✅ | `fresh-group-buy-assistant`，5 个工具 |
| README | ✅ | 使用说明、快捷体验流程、架构、体验流程、简历、面试要点 |
| 设计文档 | ✅ | `docs/DESIGN.md` |
| 运行截图 | ✅ | `docs/images/home.png`、`docs/images/assistant-chat.png` |

## 自动化验证

### 构建

```bash
mvn clean package -DskipTests
```

结果：`BUILD SUCCESS`。

### 服务探活

```bash
curl --noproxy '*' -o /dev/null -w '%{http_code}' http://127.0.0.1:8090
curl --noproxy '*' -o /dev/null -w '%{http_code}' http://127.0.0.1:18082
```

结果：两个地址均返回 `200`。

### 插件状态

```bash
curl --noproxy '*' http://127.0.0.1:8090/api/harness/plugins
```

结果：

```text
fresh-group-buy-assistant ACTIVE
```

## 工具实测

| 工具 | 测试问题 | 结果 |
|---|---|---|
| `search_products` | 推荐一款适合火锅的食材，并说明原因。 | 返回草原羔羊肉卷，价格/库存/成团人数来自商城 API |
| `product_detail` | 查一下 g003 的完整详情。 | 返回价格、规格、库存、供应商、保鲜建议 |
| `group_status` | 查一下 g003 的拼团进度。 | 返回 100% 成团进度、截止时间、供应商 |
| `pickup_points` | 最近的取货点在哪？ | 返回 3 个自提点，按距离排序 |
| `order_query` | 查一下我的团购订单。 | 返回订单号、金额、自提点、取货码 |

## API 链路

- 商品搜索：`GET /api/groupbuy/products?keyword=草莓`，成功返回商品。
- 自提点：`GET /api/groupbuy/pickup-points`，返回 3 个站点。
- 下单：`POST /api/groupbuy/orders`，生成订单号 `GB202609231001` 和取货码 `C54F67`。
- 订单查询：`GET /api/groupbuy/orders?customerId=customer-1`，返回订单列表。

## 已知限制

1. 数据保存在内存中，重启应用后会还原种子状态，已下单数据不保留。
2. 拼团人数为演示数据，不接真实支付和履约系统。
3. 自提点为虚拟站点，不接真实地图和冷鲜物流。
4. 服务进程可能被环境回收；README 中已给出重启命令。
