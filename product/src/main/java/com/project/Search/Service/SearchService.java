package com.project.Search.Service;

import com.project.Search.DTO.CategoryDTO;
import com.project.Search.DTO.ProductDetailDTO;
import com.project.Search.DTO.Response;
import com.project.Search.DTO.SearchRequestDTO;
import com.project.Search.DTO.SearchResultDTO;

public interface SearchService {


    ProductDetailDTO getProductDetail(String skuId);
    CategoryDTO getCategoryDetail(String categoryID);
    Response<SearchResultDTO> getProductSearch(SearchRequestDTO searchRequestDTO);


}
