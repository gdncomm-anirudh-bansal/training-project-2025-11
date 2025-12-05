package com.Project.Api_Gateway.Config;

import com.Project.Api_Gateway.Filter.JwtAuthenticationFilter;
import com.Project.Api_Gateway.Filter.LoginJwtFilter;
import com.Project.Api_Gateway.Service.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Map;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private LoginJwtFilter loginJwtFilter;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Value("${member.service.url}")
    private String memberServiceUrl;

    @Value("${cart.service.url}")
    private String cartServiceUrl;

    @Value("${search.service.url}")
    private String searchServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Login endpoint - generates JWT token and adds to response
                .route("login-service", r -> r
                        .path("/api/member/login")
                        .filters(f -> f
                                .modifyResponseBody(String.class, String.class, 
                                        (exchange, responseBody) -> {
                                            try {
                                                System.out.println("ModifyResponseBody: Processing response: " + responseBody);
                                                ObjectMapper mapper = new ObjectMapper();
                                                @SuppressWarnings("unchecked")
                                                Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);
                                                
                                                Object successObj = responseMap.get("success");
                                                boolean isSuccess = successObj instanceof Boolean && (Boolean) successObj;
                                                Object userIdObj = responseMap.get("user_id");
                                                if (userIdObj == null) {
                                                    userIdObj = responseMap.get("userId");
                                                }
                                                
                                                if (isSuccess && userIdObj != null) {
                                                    String userId = String.valueOf(userIdObj);
                                                    String token = loginJwtFilter.getJwtService().generateToken(userId);
                                                    responseMap.put("token", token);
                                                    exchange.getResponse().getHeaders().add("Set-Cookie", 
                                                            "token=" + token + "; HttpOnly; Secure; SameSite=Strict; Path=/");
                                                    return Mono.just(mapper.writeValueAsString(responseMap));
                                                }
                                                return Mono.just(responseBody);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                return Mono.just(responseBody);
                                            }
                                        })
                        )
                        .uri(memberServiceUrl))
                

                .route("logout-service", r -> r
                        .path("/api/member/logout")
                        .filters(f -> f

                                .filter((exchange, chain) -> {
                                    var request = exchange.getRequest();
                                    var jwtService = loginJwtFilter.getJwtService();
                                    

                                    String token = null;
                                    var authHeaders = request.getHeaders().get("Authorization");
                                    if (authHeaders != null && !authHeaders.isEmpty()) {
                                        String authHeader = authHeaders.get(0);
                                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                                            token = authHeader.substring(7);
                                        }
                                    }
                                    
                                    if (token == null) {
                                        String cookies = request.getHeaders().getFirst("Cookie");
                                        if (cookies != null) {
                                            String[] cookieArray = cookies.split(";");
                                            for (String cookie : cookieArray) {
                                                cookie = cookie.trim();
                                                if (cookie.startsWith("token=")) {
                                                    token = cookie.substring(6);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    

                                    if (token != null && jwtService.validateToken(token)) {
                                        String userId = jwtService.extractUserClaim(token);
                                        if (userId != null && !userId.isEmpty()) {
                                            // Get token expiration time and add to blacklist
                                            Long expirationTime = jwtService.getTokenExpirationTime(token);
                                            if (expirationTime != null) {
                                                tokenBlacklistService.blacklistToken(token, expirationTime);
                                            }
                                            
                                            var modifiedRequest = request.mutate()
                                                    .header("X-User-Id", userId)
                                                    .headers(headers -> headers.remove("Authorization"))
                                                    .build();
                                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                                        }
                                    }
                                    

                                    return chain.filter(exchange);
                                })

                                .modifyResponseBody(String.class, String.class, 
                                        (exchange, responseBody) -> {
                                            // Clear the cookie by setting Max-Age=0
                                            exchange.getResponse().getHeaders().add("Set-Cookie", 
                                                    "token=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0");
                                            

                                            try {
                                                ObjectMapper mapper = new ObjectMapper();
                                                @SuppressWarnings("unchecked")
                                                Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);
                                                responseMap.put("message", "Logout successful");
                                                responseMap.put("success", true);
                                                return Mono.just(mapper.writeValueAsString(responseMap));
                                            } catch (Exception e) {
                                                // If parsing fails, return simple success message
                                                return Mono.just("{\"message\":\"Logout successful\",\"success\":true}");
                                            }
                                        })
                        )
                        .uri(memberServiceUrl))
                

                .route("member-service", r -> r
                        .path("/api/member/**")
                        .filters(f -> f
                               
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri(memberServiceUrl))
                

                .route("cart-service", r -> r
                        .path("/api/cart/**")
                        .filters(f -> f

                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri(cartServiceUrl))
                

                .route("search-service", r -> r
                        .path("/api/search/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(searchServiceUrl))
                
                .build();
    }
}

