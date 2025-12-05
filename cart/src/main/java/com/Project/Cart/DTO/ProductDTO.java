package com.Project.Cart.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDTO {
    private String sku;
    private String name;
    

    @JsonDeserialize(using = PriceDeserializer.class)
    @JsonProperty("price")
    private Long price;
    
    private String productImage;
    private Map<String, String> attributes;
}

