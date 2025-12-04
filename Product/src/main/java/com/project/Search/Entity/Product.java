package com.project.Search.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = Product.COLLECTION_NAME)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {

    public static final String COLLECTION_NAME="product";


    @Id
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
