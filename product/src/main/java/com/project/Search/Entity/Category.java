package com.project.Search.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = Category.COLLECTION_NAME)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Category {

    public static final String COLLECTION_NAME="categories";


    @Id
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
