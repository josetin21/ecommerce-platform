package com.ecommerce.cartorderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart implements Serializable {

    private UUID userId;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    public BigDecimal getTotalAmount(){
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(CartItem newItem){
        items.stream()
                .filter(item -> item.getProductId().equals(newItem.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(
                                existing.getQuantity() + newItem.getQuantity()),
                        () -> items.add(newItem)
                );
    }

    public void removeItem(UUID productId){
        items.removeIf(item -> item.getProductId().equals(productId));
    }

    public void updateItemQuantity(UUID productId, int quantity){
        items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));
    }
}

