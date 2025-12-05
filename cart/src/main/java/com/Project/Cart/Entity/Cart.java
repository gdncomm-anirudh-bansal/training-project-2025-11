package com.Project.Cart.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "carts")
public class Cart {

    @Id
    private Long memberId;
    private List<CartItem> items;
    private CartSummary summary;
    private String status;



}
