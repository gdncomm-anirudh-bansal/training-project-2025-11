package com.Project.Cart.Config;

import com.Project.Cart.Client.MemberServiceClient;
import com.Project.Cart.Client.ProductServiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Value("${search.service.url}")
    private String searchServiceUrl;

    @Value("${member.service.url}")
    private String memberServiceUrl;

    @Bean
    public ProductServiceClient productServiceClient(ObjectMapper objectMapper) {
        return Feign.builder()
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .logger(new Slf4jLogger(ProductServiceClient.class))
                .logLevel(feign.Logger.Level.BASIC)
                .target(ProductServiceClient.class, searchServiceUrl);
    }

    @Bean
    public MemberServiceClient memberServiceClient(ObjectMapper objectMapper) {
        return Feign.builder()
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .logger(new Slf4jLogger(MemberServiceClient.class))
                .logLevel(feign.Logger.Level.BASIC)
                .target(MemberServiceClient.class, memberServiceUrl);
    }
}

