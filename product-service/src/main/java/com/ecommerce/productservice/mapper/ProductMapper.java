package com.ecommerce.productservice.mapper;

import com.ecommerce.productservice.dto.response.ProductImageResponse;
import com.ecommerce.productservice.dto.response.ProductResponse;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface ProductMapper {

    ProductResponse toProductResponse(Product product);

    ProductImageResponse toProductImageResponse(ProductImage productImage);
}
