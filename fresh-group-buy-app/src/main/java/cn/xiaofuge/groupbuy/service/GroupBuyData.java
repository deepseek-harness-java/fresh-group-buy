package cn.xiaofuge.groupbuy.service;

import cn.xiaofuge.groupbuy.domain.PickupPoint;
import cn.xiaofuge.groupbuy.domain.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

final class GroupBuyData {
    private GroupBuyData() {
    }

    static List<Product> products() {
        var deadline = LocalDateTime.now().plusHours(18);
        return List.of(
                product("g001", "丹东草莓 2 盒装", "水果", "🍓", "酸甜多汁",
                        "当天现摘现发，单果 25g+，酸甜比稳定，适合下午茶与儿童加餐。", "39.9", "59.9", "2盒/份", 120, 86, 30, 62,
                        deadline, "丹东草莓合作社", "收到后先冷藏 30 分钟再吃，口感更紧实。", List.of("今日特价", "水果", "送礼")),
                product("g002", "山东大葱 2 斤", "蔬菜", "🧅", "爆锅更香",
                        "当季沙土地大葱，葱白长、辛香足，适合爆锅与蘸酱。", "9.9", "15.9", "2斤/份", 200, 143, 50, 121,
                        deadline, "章丘大葱基地", "根部用报纸包好，冷藏可放 5 天。", List.of("家常菜", "葱香", "量大")),
                product("g003", "草原羔羊肉卷 1 斤", "肉禽", "🐑", "涮锅不散",
                        "内蒙古羔羊后腿肉卷，肥瘦 3:7，涮煮不易散。", "49.9", "69.9", "500g/份", 80, 47, 25, 39,
                        deadline, "草原直供牧场", "解冻后一次吃完，不建议反复冷冻。", List.of("火锅", "高蛋白", "量贩")),
                product("g004", "有机菠菜 300g", "蔬菜", "🥬", "嫩叶少渣",
                        "水培有机菠菜，叶柄短、叶片厚，适合清炒与汤面。", "6.9", "9.9", "300g/份", 150, 96, 40, 88,
                        deadline, "近郊有机农场", "焯水 10 秒可去草酸，颜色更亮。", List.of("低卡", "有机", "快手菜")),
                product("g005", "挪威三文鱼刺身 200g", "海鲜", "🐟", "入口即化",
                        "冰鲜中段刺身，纹理清晰，附柠檬角与芥末包。", "59.9", "89.9", "200g/份", 60, 34, 20, 31,
                        deadline, "挪威冰鲜直供", "收到后 2 小时内食用最佳，冷藏不宜过夜。", List.of("刺身", "高蛋白", "即食")),
                product("g006", "现磨豆浆原料包", "豆制品", "🥛", "早餐省心",
                        "黄豆、黑豆、燕麦按 6:3:1 配比，一包可打 1L 豆浆。", "12.9", "16.9", "300g/份", 180, 112, 60, 104,
                        deadline, "五谷工坊", "提前泡 6 小时，豆香更浓。", List.of("早餐", "植物蛋白", "低糖")),
                product("g007", "台州小黄鱼 500g", "海鲜", "🐠", "家常煎炸",
                        "东海小黄鱼，去鳞去鳃，肉质细嫩，适合红烧与椒盐。", "35.9", "49.9", "500g/份", 90, 58, 30, 52,
                        deadline, "台州渔港", "红烧前用厨房纸吸干水分，煎皮不破。", List.of("海鲜", "家常菜", "高蛋白")),
                product("g008", "烟台苹果 5 斤装", "水果", "🍎", "脆甜多汁",
                        "烟台红富士，脆甜带蜜，果径 80mm+，耐储存。", "29.9", "39.9", "5斤/份", 160, 105, 45, 93,
                        deadline, "烟台果园", "常温阴凉可放 10 天，冷藏口感更脆。", List.of("水果", "耐放", "家庭装"))
        );
    }

    static List<PickupPoint> pickupPoints() {
        return List.of(
                new PickupPoint("pp001", "梧桐里社区服务站", "徐汇区梧桐路 18 号一层", "王站长 13800000001", 0.4,
                        LocalTime.of(8, 0), LocalTime.of(21, 0), 180, 132, List.of("步行可达", "有冰柜", "夜间取货")),
                new PickupPoint("pp002", "滨江生活广场团点", "浦东新区滨江大道 268 号 B1", "李站长 13800000002", 1.8,
                        LocalTime.of(9, 0), LocalTime.of(20, 30), 220, 168, List.of("停车方便", "大件友好", "地铁旁")),
                new PickupPoint("pp003", "科技园区前沿驿站", "闵行区科技园路 9 号 1F", "陈站长 13800000003", 3.2,
                        LocalTime.of(7, 30), LocalTime.of(22, 0), 260, 191, List.of("加班友好", "冷鲜柜", "延时服务"))
        );
    }

    private static Product product(
            String id, String name, String category, String emoji, String tagline,
            String description, String price, String originalPrice, String unit, int stock, int sold,
            int minGroupSize, int currentParticipants, LocalDateTime groupDeadline, String supplier,
            String freshTip, java.util.List<String> tags
    ) {
        return new Product(id, name, category, emoji, tagline, description,
                new BigDecimal(price), new BigDecimal(originalPrice), unit, stock, sold,
                minGroupSize, currentParticipants, groupDeadline, supplier, freshTip, tags);
    }
}
