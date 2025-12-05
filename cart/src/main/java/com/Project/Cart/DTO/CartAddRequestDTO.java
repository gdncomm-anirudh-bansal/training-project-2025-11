package com.Project.Cart.DTO;

import lombok.Data;

@Data
public class CartAddRequestDTO {

    private String productSku;
    private int quantity;


}
