package com.project.Search.Service.Impl;

import com.project.Search.DTO.*;
import com.project.Search.Entity.Category;
import com.project.Search.Entity.Image;
import com.project.Search.Entity.Price;
import com.project.Search.Entity.Product;
import com.project.Search.Repository.CategoryRepository;
import com.project.Search.Repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchServiceImpl Tests")
class SearchServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private SearchServiceImpl searchService;

    private Product mockProduct;
    private Category mockCategory;
    private SearchRequestDTO searchRequestDTO;

    @BeforeEach
    void setUp() {
        // Setup mock product
        mockProduct = new Product();
        mockProduct.setSku("SKU001");
        mockProduct.setName("Laptop");
        mockProduct.setDescription("High performance laptop");
        mockProduct.setBrand("Dell");
        mockProduct.setStatus("ACTIVE");

        Price price = new Price();
        price.setAmount(50000L);
        price.setDiscountedAmount(45000L);
        mockProduct.setPrice(price);

        Image image = new Image();
        image.setUrl("https://example.com/image.jpg");
        image.setIsPrimary(true);
        List<Image> images = new ArrayList<>();
        images.add(image);
        mockProduct.setImages(images);

        // Setup mock category
        mockCategory = new Category();
        mockCategory.setCategoryId("CAT001");
        mockCategory.setName("Electronics");

        // Setup search request DTO
        searchRequestDTO = new SearchRequestDTO();
        searchRequestDTO.setQuery("laptop");
        searchRequestDTO.setPage(0);
        searchRequestDTO.setSize(10);
    }

    @Test
    @DisplayName("Happy Flow: Get Product Detail - Should return product detail DTO")
    void testGetProductDetail_HappyFlow() {

        String skuId = "SKU001";
        when(productRepository.findById(skuId)).thenReturn(Optional.of(mockProduct));


        ProductDetailDTO result = searchService.getProductDetail(skuId);


        assertNotNull(result);
        assertEquals("SKU001", result.getSku());
        assertEquals("Laptop", result.getName());
        assertEquals("High performance laptop", result.getDescription());
        assertEquals("Dell", result.getBrand());
        verify(productRepository, times(1)).findById(skuId);
    }

    @Test
    @DisplayName("Negative Flow: Get Product Detail - Product not found should return null")
    void testGetProductDetail_NegativeFlow_ProductNotFound() {

        String skuId = "INVALID_SKU";
        when(productRepository.findById(skuId)).thenReturn(Optional.empty());


        ProductDetailDTO result = searchService.getProductDetail(skuId);


        assertNull(result);
        verify(productRepository, times(1)).findById(skuId);
    }

    @Test
    @DisplayName("Happy Flow: Get Category Detail - Should return category DTO")
    void testGetCategoryDetail_HappyFlow() {

        String categoryId = "CAT001";
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));


        CategoryDTO result = searchService.getCategoryDetail(categoryId);


        assertNotNull(result);
        assertEquals("CAT001", result.getCategoryId());
        assertEquals("Electronics", result.getName());
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    @DisplayName("Negative Flow: Get Category Detail - Category not found should throw RuntimeException")
    void testGetCategoryDetail_NegativeFlow_CategoryNotFound() {

        String categoryId = "INVALID_CAT";
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());


        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            searchService.getCategoryDetail(categoryId);
        });

        assertTrue(exception.getMessage().contains("Category not found"));
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    @DisplayName("Happy Flow: Get Product Search - Should return search results with pagination")
    void testGetProductSearch_HappyFlow() {

        List<Product> productList = new ArrayList<>();
        productList.add(mockProduct);

        Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);
        when(productRepository.searchProducts(any(SearchRequestDTO.class), any(Pageable.class)))
                .thenReturn(productPage);


        Response<SearchResultDTO> result = searchService.getProductSearch(searchRequestDTO);


        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertEquals("Search completed successfully", result.getMessage());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getContent());
        assertEquals(1, result.getData().getContent().size());
        assertEquals("SKU001", result.getData().getContent().get(0).getSku());
        assertEquals("Laptop", result.getData().getContent().get(0).getName());
        assertNotNull(result.getData().getPaginationDTO());
        assertEquals(0, result.getData().getPaginationDTO().getPage());
        assertEquals(10, result.getData().getPaginationDTO().getSize());
        assertEquals(1, result.getData().getPaginationDTO().getTotalElements());
        verify(productRepository, times(1)).searchProducts(any(SearchRequestDTO.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Negative Flow: Get Product Search - Empty search results should return empty content")
    void testGetProductSearch_NegativeFlow_NoResults() {

        List<Product> emptyList = new ArrayList<>();
        Page<Product> emptyPage = new PageImpl<>(emptyList, PageRequest.of(0, 10), 0);
        when(productRepository.searchProducts(any(SearchRequestDTO.class), any(Pageable.class)))
                .thenReturn(emptyPage);


        Response<SearchResultDTO> result = searchService.getProductSearch(searchRequestDTO);


        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getContent());
        assertEquals(0, result.getData().getContent().size());
        assertEquals(0, result.getData().getPaginationDTO().getTotalElements());
        verify(productRepository, times(1)).searchProducts(any(SearchRequestDTO.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Happy Flow: List All Products - Should return all products with pagination")
    void testListAllProducts_HappyFlow() {

        List<Product> productList = new ArrayList<>();
        productList.add(mockProduct);

        Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);


        Response<SearchResultDTO> result = searchService.listAllProducts(0, 10, "name", "asc");


        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertEquals("Products listed successfully", result.getMessage());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getContent());
        assertEquals(1, result.getData().getContent().size());
        assertNotNull(result.getData().getPaginationDTO());
        assertEquals(0, result.getData().getPaginationDTO().getPage());
        assertEquals(10, result.getData().getPaginationDTO().getSize());
        verify(productRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Negative Flow: List All Products - Empty repository should return empty results")
    void testListAllProducts_NegativeFlow_NoProducts() {

        List<Product> emptyList = new ArrayList<>();
        Page<Product> emptyPage = new PageImpl<>(emptyList, PageRequest.of(0, 10), 0);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);


        Response<SearchResultDTO> result = searchService.listAllProducts(0, 10, null, "asc");


        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getContent());
        assertEquals(0, result.getData().getContent().size());
        assertEquals(0, result.getData().getPaginationDTO().getTotalElements());
        verify(productRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Happy Flow: Get Product Search - Should handle default pagination values")
    void testGetProductSearch_WithDefaultPagination() {

        SearchRequestDTO requestDTO = new SearchRequestDTO();
        requestDTO.setQuery("laptop");
        requestDTO.setPage(0);
        requestDTO.setSize(0); // Should default to 10

        List<Product> productList = new ArrayList<>();
        productList.add(mockProduct);
        Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);
        when(productRepository.searchProducts(any(SearchRequestDTO.class), any(Pageable.class)))
                .thenReturn(productPage);


        Response<SearchResultDTO> result = searchService.getProductSearch(requestDTO);


        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(productRepository, times(1)).searchProducts(any(SearchRequestDTO.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Happy Flow: Get Product Search - Should handle product with primary image")
    void testGetProductSearch_WithPrimaryImage() {

        List<Product> productList = new ArrayList<>();
        Product productWithImage = new Product();
        productWithImage.setSku("SKU002");
        productWithImage.setName("Product with Image");
        
        Image primaryImage = new Image();
        primaryImage.setUrl("https://example.com/primary.jpg");
        primaryImage.setIsPrimary(true);
        
        Image secondaryImage = new Image();
        secondaryImage.setUrl("https://example.com/secondary.jpg");
        secondaryImage.setIsPrimary(false);
        
        List<Image> images = new ArrayList<>();
        images.add(secondaryImage);
        images.add(primaryImage); // Primary image added second
        productWithImage.setImages(images);
        
        productList.add(productWithImage);
        Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);
        when(productRepository.searchProducts(any(SearchRequestDTO.class), any(Pageable.class)))
                .thenReturn(productPage);


        Response<SearchResultDTO> result = searchService.getProductSearch(searchRequestDTO);


        assertNotNull(result);
        assertNotNull(result.getData().getContent());
        assertEquals(1, result.getData().getContent().size());
        assertEquals("https://example.com/primary.jpg", result.getData().getContent().get(0).getPrimaryImage());
    }
}

