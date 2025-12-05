package com.project.Search.DTO;

import com.project.Search.Entity.*;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProductDetailDTO {



    private String sku;
    private String name;
    private String description;
    private String brand;

    private List<CategoryMapping> categories;
    private Price price;
    private List<Image> images;
    private Map<String, Object> attributes;
    private List<Specification> specifications;
    private List<String> tags;
    private Stock stock;
    private Rating ratings;
    private String status;
}
