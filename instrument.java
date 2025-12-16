@GetMapping("/cacheEquityData")
public Mono<ResponseEntity<Object>> cacheEquityData(
        @RequestParam(required = false) String requestUUID,
        @RequestParam Set<String> cacheRegion,
        ServerHttpRequest request) {

    HttpHeaders h = request.getHeaders();

    log.error(
        """
        >>> CONTROLLER HIT <<<
        METHOD   : {}
        PATH     : {}
        REMOTE IP: {}
        XFF      : {}
        AGENT    : {}
        """,
        request.getMethod(),
        request.getURI().getPath(),
        request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "UNKNOWN",
        h.getFirst("X-Forwarded-For"),
        h.getFirst(HttpHeaders.USER_AGENT)
    );

    // your existing code continues...
}
