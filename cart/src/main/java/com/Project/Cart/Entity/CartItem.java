package com.Project.Cart.Entity;


import lombok.Data;

import java.util.Map;

@Data
public class CartItem {

    private String sku;
    private String name;
    private long price;
    private Integer quantity;
    private String productImage;
    private Map<String, String> attributes;
    private String status; // "active" or "inactive"

}
