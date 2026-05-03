package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.request.CreateProductRequest;
import com.ecommerce.productservice.dto.request.UpdateProductRequest;
import com.ecommerce.productservice.dto.response.PageResponse;
import com.ecommerce.productservice.dto.response.ProductResponse;
import com.ecommerce.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog endpoints")
public class ProductController {

    private final ProductService productService;

    @GetMapping()
    @Operation(summary = "Get all products (paginated)")
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy){

        return ResponseEntity.ok(productService.getAllProducts(page, size, sortBy));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name or description")
    public ResponseEntity<PageResponse<ProductResponse>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity.ok(productService.searchProducts(q, page, size));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get products by id")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create products (admin only)")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update products (admin only)")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request){
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Soft delete products (admin only)")
    public ResponseEntity<Void> deleteProducts(@PathVariable UUID id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
