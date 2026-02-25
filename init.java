package com.example.config;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;

@Configuration
@RequiredArgsConstructor
public class NettyWarmupConfiguration {

    private final WebClient webClient; // Inject your real WebClient bean

    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> nettyServerWarmup() {
        return factory -> factory.addServerCustomizers(HttpServer::warmup);
    }

    
    @Bean
    public HttpClient httpClient() {
        return HttpClient.create()
                         .warmup();
    }

    
    @Bean
    public WebClient webClient(HttpClient httpClient) {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    
    @EventListener(ApplicationReadyEvent.class)
    public void warmup() {

        Mono.when(
                webClient.get()
                         .uri("http://localhost:8080/actuator/health")
                         .retrieve()
                         .bodyToMono(String.class)
        )
        .timeout(Duration.ofSeconds(5))
        .block();  //??
    }
}
