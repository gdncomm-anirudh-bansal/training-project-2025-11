package com.Project.Cart.Entity;

import lombok.Data;

@Data
public class CartSummary {

    private int itemCount;
    private int totalQuantity;
    private long subtotal;
    private long tax;
    private long shipping;
    private long discount;
    private long total;

}
