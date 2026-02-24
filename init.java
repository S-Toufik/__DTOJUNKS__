@Component
public class AppWarmup {
    private final MyService service;
    private final WebClient webClient;

    public AppWarmup(MyService service, WebClient webClient) {
        this.service = service;
        this.webClient = webClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmup() {
        // warmup reactive pipeline
        service.getData().subscribe();

        // warmup webclient
        webClient.get().uri("/actuator/health")
                 .retrieve()
                 .bodyToMono(String.class)
                 .subscribe();
    }
}
