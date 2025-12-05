package com.project.Search.Controller;

import com.project.Search.DTO.*;
import com.project.Search.Service.SearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchController Tests")
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    @Test
    @DisplayName("Happy Flow: Search Product - Should return 200 OK with search results")
    void testSearchProduct_HappyFlow() {

        SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
        searchRequestDTO.setQuery("laptop");
        searchRequestDTO.setPage(0);
        searchRequestDTO.setSize(10);

        SearchResultDTO searchResultDTO = new SearchResultDTO();
        List<SearchQueryDTO> content = new ArrayList<>();
        SearchQueryDTO searchQueryDTO = new SearchQueryDTO();
        searchQueryDTO.setSku("SKU001");
        searchQueryDTO.setName("Laptop");
        content.add(searchQueryDTO);
        searchResultDTO.setContent(content);

        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setPage(0);
        paginationDTO.setSize(10);
        paginationDTO.setTotalPages(1);
        paginationDTO.setTotalElements(1);
        searchResultDTO.setPaginationDTO(paginationDTO);

        Response<SearchResultDTO> mockResponse = new Response<>();
        mockResponse.setData(searchResultDTO);
        mockResponse.setMessage("Search completed successfully");
        mockResponse.setCode(200);
        mockResponse.setSuccess(true);

        when(searchService.getProductSearch(any(SearchRequestDTO.class))).thenReturn(mockResponse);


        ResponseEntity<Response<SearchResultDTO>> response = searchController.searchProduct(searchRequestDTO);


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(200, response.getBody().getCode());
        assertEquals("Search completed successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getContent().size());
        verify(searchService, times(1)).getProductSearch(any(SearchRequestDTO.class));
    }

    @Test
    @DisplayName("Negative Flow: Search Product - Empty query should return 400 Bad Request")
    void testSearchProduct_NegativeFlow_EmptyQuery() {

        SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
        searchRequestDTO.setQuery("");


        ResponseEntity<Response<SearchResultDTO>> response = searchController.searchProduct(searchRequestDTO);


        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(400, response.getBody().getCode());
        assertEquals("Query is mandatory and cannot be empty", response.getBody().getMessage());
        verify(searchService, never()).getProductSearch(any(SearchRequestDTO.class));
    }

    @Test
    @DisplayName("Negative Flow: Search Product - Null query should return 400 Bad Request")
    void testSearchProduct_NegativeFlow_NullQuery() {

        SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
        searchRequestDTO.setQuery(null);


        ResponseEntity<Response<SearchResultDTO>> response = searchController.searchProduct(searchRequestDTO);


        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(400, response.getBody().getCode());
        assertEquals("Query is mandatory and cannot be empty", response.getBody().getMessage());
        verify(searchService, never()).getProductSearch(any(SearchRequestDTO.class));
    }

    @Test
    @DisplayName("Negative Flow: Search Product - Service throws exception should return 500 Internal Server Error")
    void testSearchProduct_NegativeFlow_ServiceException() {

        SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
        searchRequestDTO.setQuery("laptop");

        when(searchService.getProductSearch(any(SearchRequestDTO.class)))
                .thenThrow(new RuntimeException("Database connection failed"));


        ResponseEntity<Response<SearchResultDTO>> response = searchController.searchProduct(searchRequestDTO);


        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(500, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("An error occurred while searching products"));
        verify(searchService, times(1)).getProductSearch(any(SearchRequestDTO.class));
    }

    @Test
    @DisplayName("Happy Flow: Get Product Detail - Should return 200 OK with product details")
    void testGetProductDetail_HappyFlow() {
        // Arrange
        String skuId = "SKU001";
        ProductDetailDTO productDetailDTO = new ProductDetailDTO();
        productDetailDTO.setSku("SKU001");
        productDetailDTO.setName("Laptop");
        productDetailDTO.setDescription("High performance laptop");
        productDetailDTO.setBrand("Dell");

        when(searchService.getProductDetail(skuId)).thenReturn(productDetailDTO);

        // Act
        ResponseEntity<?> response = searchController.getProductDetail(skuId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ProductDetailDTO);
        ProductDetailDTO responseBody = (ProductDetailDTO) response.getBody();
        assertEquals("SKU001", responseBody.getSku());
        assertEquals("Laptop", responseBody.getName());
        verify(searchService, times(1)).getProductDetail(skuId);
    }

    @Test
    @DisplayName("Negative Flow: Get Product Detail - Empty SKU ID should return 400 Bad Request")
    void testGetProductDetail_NegativeFlow_EmptySkuId() {
        // Arrange
        String skuId = "";

        // Act
        ResponseEntity<?> response = searchController.getProductDetail(skuId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Response);
        Response<?> errorResponse = (Response<?>) response.getBody();
        assertFalse(errorResponse.isSuccess());
        assertEquals(400, errorResponse.getCode());
        assertEquals("SKU ID is required and cannot be empty", errorResponse.getMessage());
        verify(searchService, never()).getProductDetail(anyString());
    }

    @Test
    @DisplayName("Negative Flow: Get Product Detail - Product not found should return 404 Not Found")
    void testGetProductDetail_NegativeFlow_ProductNotFound() {
        // Arrange
        String skuId = "INVALID_SKU";

        when(searchService.getProductDetail(skuId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = searchController.getProductDetail(skuId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Response);
        Response<?> errorResponse = (Response<?>) response.getBody();
        assertFalse(errorResponse.isSuccess());
        assertEquals(404, errorResponse.getCode());
        assertTrue(errorResponse.getMessage().contains("Product not found"));
        verify(searchService, times(1)).getProductDetail(skuId);
    }

    @Test
    @DisplayName("Happy Flow: List All Products - Should return 200 OK with paginated products")
    void testListAllProducts_HappyFlow() {
        // Arrange
        int page = 0;
        int size = 10;
        String sort = "name";
        String order = "asc";

        SearchResultDTO searchResultDTO = new SearchResultDTO();
        List<SearchQueryDTO> content = new ArrayList<>();
        SearchQueryDTO searchQueryDTO = new SearchQueryDTO();
        searchQueryDTO.setSku("SKU001");
        searchQueryDTO.setName("Product 1");
        content.add(searchQueryDTO);
        searchResultDTO.setContent(content);

        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setPage(0);
        paginationDTO.setSize(10);
        paginationDTO.setTotalPages(1);
        paginationDTO.setTotalElements(1);
        searchResultDTO.setPaginationDTO(paginationDTO);

        Response<SearchResultDTO> mockResponse = new Response<>();
        mockResponse.setData(searchResultDTO);
        mockResponse.setMessage("Products listed successfully");
        mockResponse.setCode(200);
        mockResponse.setSuccess(true);

        when(searchService.listAllProducts(page, size, sort, order)).thenReturn(mockResponse);

        // Act
        ResponseEntity<Response<SearchResultDTO>> response = searchController.listAllProducts(page, size, sort, order);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(200, response.getBody().getCode());
        assertEquals("Products listed successfully", response.getBody().getMessage());
        verify(searchService, times(1)).listAllProducts(page, size, sort, order);
    }

    @Test
    @DisplayName("Negative Flow: List All Products - Service throws exception should return 500 Internal Server Error")
    void testListAllProducts_NegativeFlow_ServiceException() {
        // Arrange
        int page = 0;
        int size = 10;
        String sort = "name";
        String order = "asc";

        when(searchService.listAllProducts(page, size, sort, order))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<Response<SearchResultDTO>> response = searchController.listAllProducts(page, size, sort, order);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(500, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("An error occurred while listing products"));
        verify(searchService, times(1)).listAllProducts(page, size, sort, order);
    }

    @Test
    @DisplayName("Happy Flow: Get Category Detail - Should return 200 OK with category details")
    void testGetCategoryDetail_HappyFlow() {
        // Arrange
        String categoryId = "CAT001";
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryId("CAT001");
        categoryDTO.setName("Electronics");

        when(searchService.getCategoryDetail(categoryId)).thenReturn(categoryDTO);

        // Act
        ResponseEntity<CategoryDTO> response = searchController.getCategoryDetail(categoryId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CAT001", response.getBody().getCategoryId());
        assertEquals("Electronics", response.getBody().getName());
        verify(searchService, times(1)).getCategoryDetail(categoryId);
    }

    @Test
    @DisplayName("Negative Flow: Get Category Detail - Empty category ID should return 400 Bad Request")
    void testGetCategoryDetail_NegativeFlow_EmptyCategoryId() {
        // Arrange
        String categoryId = "";

        // Act
        ResponseEntity<CategoryDTO> response = searchController.getCategoryDetail(categoryId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(searchService, never()).getCategoryDetail(anyString());
    }
}

