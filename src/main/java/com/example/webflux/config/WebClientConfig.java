package com.example.webflux.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean(name = "server1")
    public WebClient webClientForServer1() {
        return WebClient.create("http://localhost:8081/server");
    }

    @Bean(name = "server2")
    public WebClient webClientForServer2() {
        return WebClient.create("http://localhost:8082/server");
    }

    @Bean
    public javax.validation.Validator localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }
}
