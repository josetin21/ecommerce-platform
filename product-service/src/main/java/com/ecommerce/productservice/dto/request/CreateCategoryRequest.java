package com.ecommerce.productservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateCategoryRequest {

    @NotNull(message = "Category name is required")
    @Size(min = 2, max = 100)
    private String name;

    private String description;

    private UUID parentId;
}
