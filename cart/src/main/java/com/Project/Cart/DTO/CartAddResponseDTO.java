package com.Project.Cart.DTO;

import lombok.Data;

import java.util.List;

@Data
public class CartAddResponseDTO {

    private Long memberId;
    private List<CartItemDTO> items;
    private CartSummaryDTO summary;

    @Data
    public static class CartItemDTO {
        private String sku;
        private String name;
        private long price;
        private Integer quantity;
        private long subtotal;
        private Boolean isActive;
    }

    @Data
    public static class CartSummaryDTO {
        private Integer itemCount;
        private Integer totalQuantity;
        private long subtotal;
        private long total;
    }
}
