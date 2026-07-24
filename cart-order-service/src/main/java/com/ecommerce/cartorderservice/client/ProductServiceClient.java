package com.ecommerce.cartorderservice.client;

import com.ecommerce.cartorderservice.dto.client.ProductClientResponse;
import com.ecommerce.cartorderservice.dto.client.StockAdjustmentRequest;
import com.ecommerce.cartorderservice.exception.InsufficientStockException;
import com.ecommerce.cartorderservice.exception.ResourceNotFoundException;
import com.ecommerce.cartorderservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private static final String INTERNAL_SERVICE_EMAIL = "internal-service@ecommerce.local";
    private static final String INTERNAL_SERVICE_ID = "00000000-0000-0000-0000-000000000000";
    private static final String PRODUCT_SERVICE_URL = "http://product-service";

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    public ProductClientResponse getProduct(UUID productId){
        try{
            return restTemplate.getForObject(PRODUCT_SERVICE_URL + "/api/products/" + productId, ProductClientResponse.class);
        } catch (HttpClientErrorException.NotFound e){
            throw new ResourceNotFoundException("Product not found: " + productId);
        } catch (RestClientException e){
            log.error("Failed to fetch product {} from product-service: {}", productId, e.getMessage());
            throw new RuntimeException("Product service unavailable");
        }
    }

    public void reserveStock(StockAdjustmentRequest request){
        try{
            restTemplate.postForEntity(
                    PRODUCT_SERVICE_URL + "/api/products/internal/reserve-stock",
                    new HttpEntity<>(request, serviceAuthHeaders()), Void.class);
        } catch (HttpClientErrorException e){
            if (e.getStatusCode() == HttpStatus.CONFLICT){
                throw new InsufficientStockException("Insufficient stock for one or more items");
            }
            log.error("Stock reservation failed: {}", e.getMessage());
            throw new RuntimeException("Stock reservation failed");
        }
    }

    public void releaseStock(StockAdjustmentRequest request){
        try {
            restTemplate.postForEntity(
                    PRODUCT_SERVICE_URL + "/api/products/internal/release-stock",
                    new HttpEntity<>(request, serviceAuthHeaders()),
                    Void.class);
            log.info("Stock release request sent for {} item(s)", request.getItems().size());
        } catch (RestClientException e){
            log.error("Stock release failed: {}", e.getMessage());
        }
    }

    private HttpHeaders serviceAuthHeaders(){
        String serviceToken = jwtService.generateAccessToken(INTERNAL_SERVICE_EMAIL, "SERVICE", INTERNAL_SERVICE_ID);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken);
        return headers;
    }
}
