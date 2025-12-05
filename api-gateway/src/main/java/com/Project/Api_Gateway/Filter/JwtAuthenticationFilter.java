package com.Project.Api_Gateway.Filter;

import com.Project.Api_Gateway.Service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtService jwtService;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            

            String authNeeded = request.getHeaders().getFirst("X-Auth-Needed");
            

            if (authNeeded == null || !authNeeded.equalsIgnoreCase("true")) {
                return chain.filter(exchange);
            }
            

            String token = extractToken(request);
            
            if (token == null || token.isEmpty()) {
                return onError(exchange, "Missing or invalid token", HttpStatus.UNAUTHORIZED);
            }


            if (!jwtService.validateToken(token)) {
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }


            String userId = jwtService.extractUserClaim(token);
            if (userId == null || userId.isEmpty()) {
                return onError(exchange, "Invalid token: missing user_id", HttpStatus.UNAUTHORIZED);
            }


            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
                    .headers(headers -> headers.remove("x-authorisationNeeded"))
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }


    private String extractToken(ServerHttpRequest request) {

        List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }


        String cookies = request.getHeaders().getFirst(HttpHeaders.COOKIE);
        if (cookies != null) {
            String[] cookieArray = cookies.split(";");
            for (String cookie : cookieArray) {
                cookie = cookie.trim();
                if (cookie.startsWith("token=")) {
                    return cookie.substring(6);
                }
            }
        }

        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(
                        ("{\"error\": \"" + message + "\"}").getBytes()
                ))
        );
    }

    public static class Config {

    }
}

