package com.project.Search.Entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Price {

    private Long amount;
    private String currency;
    private Discount discount;
    private Long discountedAmount;

}
