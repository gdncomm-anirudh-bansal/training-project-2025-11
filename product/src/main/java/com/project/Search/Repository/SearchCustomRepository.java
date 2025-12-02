package com.project.Search.Repository;

import com.project.Search.DTO.SearchRequestDTO;
import com.project.Search.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface SearchCustomRepository {

    Page<Product> searchProducts(SearchRequestDTO searchRequestDTO, Pageable pageable);

}
