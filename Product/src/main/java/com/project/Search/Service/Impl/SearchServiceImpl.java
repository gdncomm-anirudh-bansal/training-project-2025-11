package com.project.Search.Service.Impl;

import com.project.Search.DTO.*;
import com.project.Search.Entity.Category;
import com.project.Search.Entity.Image;
import com.project.Search.Entity.Product;
import com.project.Search.Repository.CategoryRepository;
import com.project.Search.Repository.ProductRepository;
import com.project.Search.Service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchServiceImpl  implements SearchService {



    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Override
    @Cacheable(value = "productDetail", key = "#skuId", unless = "#result == null")
    public ProductDetailDTO getProductDetail(String skuId) {

        log.info("Fetching the product Detail from database for SKU: {}", skuId);

        Product product = productRepository.findById(skuId)
                .orElse(null);

        if (product == null) {
            log.warn("Product not found for SKU: {}", skuId);
            return null;
        }

        return convertToDTOResponse(product);

    }

    @Override
    public CategoryDTO getCategoryDetail(String categoryID) {

        log.info("Fetching the Category Detail");

        Category category = categoryRepository.findById(categoryID)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryID));

        return convertToDTOResponse(category);
    }

    @Override
    public Response<SearchResultDTO> getProductSearch(SearchRequestDTO searchRequestDTO) {
        log.info("Searching products with query: {}", searchRequestDTO.getQuery());





        int page = searchRequestDTO.getPage() > 0 ? searchRequestDTO.getPage() : 0;
        int size = searchRequestDTO.getSize() > 0 ? searchRequestDTO.getSize() : 10;


        Sort sort = buildSort(searchRequestDTO.getSort(), searchRequestDTO.getOrder());
        Pageable pageable = PageRequest.of(page, size, sort);


        Page<Product> productPage = productRepository.searchProducts(searchRequestDTO, pageable);


        List<SearchQueryDTO> searchQueryDTOList = productPage.getContent().stream()
                .map(this::convertToSearchQueryDTO)
                .collect(Collectors.toList());


        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setPage(productPage.getNumber());
        paginationDTO.setSize(productPage.getSize());
        paginationDTO.setTotalPages(productPage.getTotalPages());
        paginationDTO.setTotalElements((int) productPage.getTotalElements());


        SearchResultDTO searchResultDTO = new SearchResultDTO();
        searchResultDTO.setContent(searchQueryDTOList);
        searchResultDTO.setPaginationDTO(paginationDTO);


        Response<SearchResultDTO> response = new Response<>();
        response.setData(searchResultDTO);
        response.setMessage("Search completed successfully");
        response.setCode(200);
        response.setSuccess(true);

        return response;
    }

    @Override
    public Response<SearchResultDTO> listAllProducts(int page, int size, String sort, String order) {
        log.info("Listing all products with pagination - page: {}, size: {}, sort: {}, order: {}", page, size, sort, order);

        // Validate and set defaults
        int pageNumber = page > 0 ? page : 0;
        int pageSize = size > 0 ? size : 10;

        // Build sort
        Sort sortObj = buildSort(sort, order);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortObj);

        // Fetch all products with pagination
        org.springframework.data.domain.Page<Product> productPage = productRepository.findAll(pageable);

        // Convert to DTOs
        List<SearchQueryDTO> searchQueryDTOList = productPage.getContent().stream()
                .map(this::convertToSearchQueryDTO)
                .collect(Collectors.toList());

        // Build pagination DTO
        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setPage(productPage.getNumber());
        paginationDTO.setSize(productPage.getSize());
        paginationDTO.setTotalPages(productPage.getTotalPages());
        paginationDTO.setTotalElements((int) productPage.getTotalElements());

        // Build response
        SearchResultDTO searchResultDTO = new SearchResultDTO();
        searchResultDTO.setContent(searchQueryDTOList);
        searchResultDTO.setPaginationDTO(paginationDTO);

        Response<SearchResultDTO> response = new Response<>();
        response.setData(searchResultDTO);
        response.setMessage("Products listed successfully");
        response.setCode(200);
        response.setSuccess(true);

        return response;
    }

    private Sort buildSort(String sortField, String order) {
        if (sortField == null || sortField.trim().isEmpty()) {
            return Sort.unsorted();
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(order) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;

        return Sort.by(direction, sortField);
    }

    private SearchQueryDTO convertToSearchQueryDTO(Product product) {
        SearchQueryDTO searchQueryDTO = new SearchQueryDTO();
        searchQueryDTO.setSku(product.getSku());
        searchQueryDTO.setName(product.getName());
        searchQueryDTO.setDescription(product.getDescription());
        searchQueryDTO.setBrand(product.getBrand());
        searchQueryDTO.setStatus(product.getStatus());


        if (product.getPrice() != null) {
            SearchQueryDTO.PriceDetail priceDetail = new SearchQueryDTO.PriceDetail();
            priceDetail.setAmount(product.getPrice().getAmount());
            priceDetail.setDiscountedAmount(product.getPrice().getDiscountedAmount());
            searchQueryDTO.setPriceDetail(priceDetail);
        }


        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Optional<Image> primaryImage = product.getImages().stream()
                    .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
                    .findFirst();
            
            if (primaryImage.isPresent()) {
                searchQueryDTO.setPrimaryImage(primaryImage.get().getUrl());
            } else {

                searchQueryDTO.setPrimaryImage(product.getImages().get(0).getUrl());
            }
        }

        return searchQueryDTO;
    }


    private static ProductDetailDTO convertToDTOResponse(Product product) {
        ProductDetailDTO productDetailDTO = new ProductDetailDTO();
        BeanUtils.copyProperties(product,productDetailDTO);
        return productDetailDTO;
    }

    private static CategoryDTO convertToDTOResponse(Category category) {
        CategoryDTO categoryDTO = new CategoryDTO();
        BeanUtils.copyProperties(category,categoryDTO);
        return categoryDTO;
    }

}
