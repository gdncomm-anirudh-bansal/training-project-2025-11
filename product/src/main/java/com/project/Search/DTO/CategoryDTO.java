package com.project.Search.DTO;

import lombok.Data;

@Data
public class CategoryDTO {
    private String categoryId;
    private String name;
    private String description;
    private String parentId;
    private Integer level;
    private String path;
    private String image;
    private Integer order;
    private Boolean isActive;
}
