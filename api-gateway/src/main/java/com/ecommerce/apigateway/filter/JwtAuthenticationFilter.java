package com.ecommerce.apigateway.filter;

import com.ecommerce.apigateway.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtProperties jwtProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private record PublicRoute(HttpMethod method, String pattern) {}

    private static final List<PublicRoute> PUBLIC_ROUTES = List.of(
        new PublicRoute(HttpMethod.POST, "/api/auth/register"),
        new PublicRoute(HttpMethod.POST, "/api/auth/login"),
        new PublicRoute(HttpMethod.POST,"/api/auth/refresh"),
        new PublicRoute(HttpMethod.POST,"/api/auth/logout"),
        new PublicRoute(HttpMethod.GET,"/api/products/**"),
        new PublicRoute(HttpMethod.GET,"/api/categories/**"),
        new PublicRoute(HttpMethod.POST,"/api/payments/webhook"),
        new PublicRoute(null, "/actuator/health"),
        new PublicRoute(null, "/swagger-ui/**"),
        new PublicRoute(null, "/api-docs/**"),
        new PublicRoute(null, "/v3/api-docs/**")
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (isPublicRoute(request)){
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            return reject(exchange, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(7);

        try{
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException e){
            log.warn("Gateway rejected request to {}: {}", request.getURI().getPath(), e.getMessage());
            return reject(exchange, "Invalid or expired token");
        }

        return chain.filter(exchange);
    }

    private boolean isPublicRoute(ServerHttpRequest request){
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        return PUBLIC_ROUTES.stream().anyMatch(route ->
                        (route.method() == null || route.method().equals(method))
                                && pathMatcher.match(route.pattern(), path));
    }

    private Mono<Void> reject(ServerWebExchange exchange, String message){
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"status\":401,\"message\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private SecretKey getSigningKey(){
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
