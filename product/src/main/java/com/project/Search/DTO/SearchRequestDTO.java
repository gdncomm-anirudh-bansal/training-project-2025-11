package com.project.Search.DTO;

import lombok.Data;

@Data
public class SearchRequestDTO {

    private String query;
    private int page;
    private int size;
    private String sort;
    private String order;
    private String categoryId;
    private Long minPrice;
    private Long maxPrice;
    private String brand;



}
