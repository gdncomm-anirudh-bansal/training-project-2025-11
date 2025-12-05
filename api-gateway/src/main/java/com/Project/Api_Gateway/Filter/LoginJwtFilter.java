package com.Project.Api_Gateway.Filter;

import com.Project.Api_Gateway.Service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class LoginJwtFilter extends AbstractGatewayFilterFactory<LoginJwtFilter.Config> {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;
    
    public JwtService getJwtService() {
        return jwtService;
    }

    public LoginJwtFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            System.out.println("LoginJwtFilter: Filter applied for path: " + exchange.getRequest().getPath());
            ServerHttpResponse originalResponse = exchange.getResponse();
            

            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
                    System.out.println("LoginJwtFilter: writeWith called, body type: " + (body != null ? body.getClass().getName() : "null"));

                    Flux<DataBuffer> fluxBody;
                    if (body instanceof Flux) {
                        @SuppressWarnings("unchecked")
                        Flux<DataBuffer> flux = (Flux<DataBuffer>) body;
                        fluxBody = flux;
                    } else if (body instanceof Mono) {
                        @SuppressWarnings("unchecked")
                        Mono<DataBuffer> mono = (Mono<DataBuffer>) body;
                        fluxBody = mono.flux();
                    } else {
                        return super.writeWith(body);
                    }
                    
                    return DataBufferUtils.join(fluxBody)
                            .flatMap(dataBuffer -> {
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);
                                
                                try {

                                    String responseBody = new String(content, StandardCharsets.UTF_8);
                                    System.out.println("LoginJwtFilter: Response body: " + responseBody);
                                    

                                    if (responseBody == null || responseBody.trim().isEmpty()) {
                                        System.out.println("LoginJwtFilter: Empty response body");
                                        DataBuffer buffer = originalResponse.bufferFactory().wrap(content);
                                        return originalResponse.writeWith(Mono.just(buffer));
                                    }
                                    
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                                    System.out.println("LoginJwtFilter: Parsed response map: " + responseMap);


                                    Object successObj = responseMap.get("success");
                                    boolean isSuccess = false;
                                    if (successObj instanceof Boolean) {
                                        isSuccess = (Boolean) successObj;
                                    } else if (successObj != null) {
                                        isSuccess = Boolean.parseBoolean(successObj.toString());
                                    }
                                    

                                    Object userIdObj = responseMap.get("user_id");
                                    if (userIdObj == null) {
                                        userIdObj = responseMap.get("userId");
                                    }
                                    

                                        System.out.println("LoginJwtFilter: Checking conditions - success: " + isSuccess + 
                                                ", userId: " + userIdObj + ", status: " + getStatusCode());
                                        if (isSuccess && userIdObj != null && getStatusCode().is2xxSuccessful()) {

                                            String userId = String.valueOf(userIdObj);
                                            System.out.println("LoginJwtFilter: Generating token for userId: " + userId);
                                            

                                            String token = jwtService.generateToken(userId);
                                            System.out.println("LoginJwtFilter: Generated token: " + token.substring(0, Math.min(50, token.length())) + "...");
                                            

                                            responseMap.put("token", token);
                                            

                                            getHeaders().add("Set-Cookie", 
                                                    "token=" + token + "; HttpOnly; Secure; SameSite=Strict; Path=/");
                                            

                                            String modifiedBody = objectMapper.writeValueAsString(responseMap);
                                            byte[] modifiedContent = modifiedBody.getBytes(StandardCharsets.UTF_8);
                                            

                                            getHeaders().remove("Content-Length");
                                            
                                            DataBuffer buffer = originalResponse.bufferFactory().wrap(modifiedContent);
                                            getHeaders().setContentLength(modifiedContent.length);
                                            
                                            return originalResponse.writeWith(Mono.just(buffer));
                                        } else {

                                            System.out.println("Token not added - success: " + isSuccess + 
                                                    ", userId: " + userIdObj + ", status: " + getStatusCode());
                                            DataBuffer buffer = originalResponse.bufferFactory().wrap(content);
                                            return originalResponse.writeWith(Mono.just(buffer));
                                        }
                                } catch (Exception e) {

                                    e.printStackTrace();
                                    DataBuffer buffer = originalResponse.bufferFactory().wrap(content);
                                    return originalResponse.writeWith(Mono.just(buffer));
                                }
                            });
                }
            };
            
            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}

