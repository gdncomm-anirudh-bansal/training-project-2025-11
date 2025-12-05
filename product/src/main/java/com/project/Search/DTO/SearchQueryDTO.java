package com.project.Search.DTO;

import lombok.Data;

@Data
public class SearchQueryDTO {

    private String sku;
    private String name;
    private String description;
    private String brand;

    private PriceDetail priceDetail;
    private String primaryImage;
    private String status;

    @Data
    public static class PriceDetail
    {
        private Long amount;
        private Long discountedAmount;
    }

}
