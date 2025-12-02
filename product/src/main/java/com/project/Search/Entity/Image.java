package com.project.Search.Entity;

import lombok.Data;

@Data
public class Image {

    private String url;
    private String alt;
    private Boolean isPrimary;
    private Integer order;
}
