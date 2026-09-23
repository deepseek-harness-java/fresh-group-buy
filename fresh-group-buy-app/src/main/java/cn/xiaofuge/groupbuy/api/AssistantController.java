package cn.xiaofuge.groupbuy.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private final ObjectMapper objectMapper;
    private final String harnessBaseUrl;
    private final String agentId;
    private final HttpClient httpClient;

    public AssistantController(
            ObjectMapper objectMapper,
            @Value("${groupbuy.assistant.harness-base-url:http://127.0.0.1:8090}") String harnessBaseUrl,
            @Value("${groupbuy.assistant.agent-id:fresh-group-buy-demo}") String agentId
    ) {
        this.objectMapper = objectMapper;
        this.harnessBaseUrl = harnessBaseUrl.endsWith("/") ? harnessBaseUrl.substring(0, harnessBaseUrl.length() - 1) : harnessBaseUrl;
        this.agentId = agentId;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void stream(@RequestBody AssistantMessageRequest request, HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(harnessBaseUrl + "/api/agent/stream"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .timeout(Duration.ofMinutes(5))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(payload(request.message()))))
                    .build();
            HttpResponse<InputStream> upstream = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
            if (upstream.statusCode() / 100 != 2) {
                response.getWriter().println("event: error");
                response.getWriter().println("data: {\"content\":\"助手暂时不可用，请稍后再试。\"}");
                return;
            }
            try (InputStream input = upstream.body(); OutputStream output = response.getOutputStream()) {
                input.transferTo(output);
                output.flush();
            }
        } catch (Exception exception) {
            response.getWriter().println("event: error");
            response.getWriter().println("data: {\"content\":\"无法连接智能助手服务，请确认 Harness 已启动。\"}");
        }
    }

    private Map<String, Object> payload(String message) {
        String contextualMessage = """
                [业务上下文] 当前演示用户ID：customer-1。
                这是 Fresh Group Buy 生鲜团购助手。商品、推荐、价格、库存、拼团进度必须调用 search_products / product_detail / group_status；
                自提点问题必须调用 pickup_points；订单问题必须调用 order_query / order_detail。
                禁止使用工具之外的任何商品、价格、库存、拼团或订单数据。
                """ + (message == null ? "" : message);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("agentId", agentId);
        payload.put("approvalMode", "FULL_OPEN");
        payload.put("message", contextualMessage);
        payload.put("sessionId", null);
        return payload;
    }

    public record AssistantMessageRequest(String message) {
    }
}
