@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpRequestLoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ServerHttpRequest req = exchange.getRequest();
        HttpHeaders h = req.getHeaders();

        return chain.filter(exchange)
                .contextWrite(ctx -> ctx) // preserve context
                .doOnEach(signal -> {
                    String uuid = signal.getContextView().getOrDefault("requestId", "NO_UUID");

                    log.info(
                        "HTTP {} {} | ip={} | xff={} | agent={} | uuid={}",
                        req.getMethod(),
                        req.getURI().getPath(),
                        req.getRemoteAddress() != null
                            ? req.getRemoteAddress().getAddress().getHostAddress()
                            : "UNKNOWN",
                        h.getFirst("X-Forwarded-For"),
                        h.getFirst(HttpHeaders.USER_AGENT),
                        uuid
                    );
                });
    }
}


.contextWrite(ctx -> ctx.put("requestId", uuid));
