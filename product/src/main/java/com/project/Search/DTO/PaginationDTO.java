package com.project.Search.DTO;

import lombok.Data;

@Data
public class PaginationDTO {

    private int size;
    private int page;
    private int totalPages;
    private int totalElements;
}
