package com.project.Search.Service.Impl;

import com.project.Search.DTO.*;
import com.project.Search.Entity.Category;
import com.project.Search.Entity.Image;
import com.project.Search.Entity.Price;
import com.project.Search.Entity.Product;
import com.project.Search.Repository.CategoryRepository;
import com.project.Search.Repository.ProductRepository;
import com.project.Search.Service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@DisplayName("SearchService Integration Tests")
class SearchServiceIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    private SearchService searchService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.cache.type", () -> "simple");
    }

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        setupTestData();
    }

    private void setupTestData() {
        // Create test category
        Category category = new Category();
        category.setCategoryId("CAT001");
        category.setName("Electronics");
        category.setDescription("Electronic products");
        category.setIsActive(true);
        category.setLevel(1);
        categoryRepository.save(category);

        // Create test products
        Product product1 = createProduct("SKU001", "Laptop", "High performance laptop", "Dell", 50000L, 45000L);
        Product product2 = createProduct("SKU002", "Smartphone", "Latest smartphone", "Samsung", 30000L, 28000L);
        Product product3 = createProduct("SKU003", "Tablet", "Portable tablet", "Apple", 40000L, 38000L);

        productRepository.saveAll(List.of(product1, product2, product3));
    }

    private Product createProduct(String sku, String name, String description, String brand, Long amount, Long discountedAmount) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setDescription(description);
        product.setBrand(brand);
        product.setStatus("ACTIVE");

        Price price = new Price();
        price.setAmount(amount);
        price.setDiscountedAmount(discountedAmount);
        price.setCurrency("INR");
        product.setPrice(price);

        Image image = new Image();
        image.setUrl("https://example.com/" + sku.toLowerCase() + ".jpg");
        image.setIsPrimary(true);
        image.setAlt(name + " image");
        List<Image> images = new ArrayList<>();
        images.add(image);
        product.setImages(images);

        return product;
    }

    @Test
    @DisplayName("Happy Flow: Get Product Detail - Should return product detail from database")
    void testGetProductDetail_HappyFlow() {
        ProductDetailDTO result = searchService.getProductDetail("SKU001");

        assertNotNull(result);
        assertEquals("SKU001", result.getSku());
        assertEquals("Laptop", result.getName());
        assertEquals("High performance laptop", result.getDescription());
        assertEquals("Dell", result.getBrand());
        assertNotNull(result.getPrice());
        assertEquals(50000L, result.getPrice().getAmount());
        assertEquals(45000L, result.getPrice().getDiscountedAmount());
    }

    @Test
    @DisplayName("Happy Flow: Get Category Detail - Should return category from database")
    void testGetCategoryDetail_HappyFlow() {
        CategoryDTO result = searchService.getCategoryDetail("CAT001");

        assertNotNull(result);
        assertEquals("CAT001", result.getCategoryId());
        assertEquals("Electronics", result.getName());
        assertEquals("Electronic products", result.getDescription());
        assertTrue(result.getIsActive());
    }

    @Test
    @DisplayName("Happy Flow: Get Product Search - Should return search results from database")
    void testGetProductSearch_HappyFlow() {
        SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
        searchRequestDTO.setQuery("laptop");
        searchRequestDTO.setPage(0);
        searchRequestDTO.setSize(10);

        Response<SearchResultDTO> result = searchService.getProductSearch(searchRequestDTO);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertEquals("Search completed successfully", result.getMessage());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getContent());
        assertTrue(result.getData().getContent().size() > 0);
        assertEquals("SKU001", result.getData().getContent().get(0).getSku());
        assertEquals("Laptop", result.getData().getContent().get(0).getName());
        assertNotNull(result.getData().getPaginationDTO());
        assertEquals(0, result.getData().getPaginationDTO().getPage());
        assertEquals(10, result.getData().getPaginationDTO().getSize());
    }

    @Test
    @DisplayName("Happy Flow: Get Product Search with brand filter - Should return filtered results")
    void testGetProductSearch_WithBrandFilter() {
        SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
        searchRequestDTO.setQuery("laptop");
        searchRequestDTO.setBrand("Dell");
        searchRequestDTO.setPage(0);
        searchRequestDTO.setSize(10);

        Response<SearchResultDTO> result = searchService.getProductSearch(searchRequestDTO);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getContent());
        if (result.getData().getContent().size() > 0) {
            assertEquals("Dell", result.getData().getContent().get(0).getBrand());
        }
    }

    @Test
    @DisplayName("Happy Flow: List All Products - Should return all products from database")
    void testListAllProducts_HappyFlow() {
        Response<SearchResultDTO> result = searchService.listAllProducts(0, 10, "name", "asc");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertEquals("Products listed successfully", result.getMessage());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getContent());
        assertEquals(3, result.getData().getContent().size());
        assertNotNull(result.getData().getPaginationDTO());
        assertEquals(0, result.getData().getPaginationDTO().getPage());
        assertEquals(10, result.getData().getPaginationDTO().getSize());
        assertEquals(3, result.getData().getPaginationDTO().getTotalElements());
    }

    @Test
    @DisplayName("Happy Flow: List All Products with pagination - Should return paginated results")
    void testListAllProducts_WithPagination() {
        Response<SearchResultDTO> result = searchService.listAllProducts(0, 2, "name", "asc");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getContent());
        assertEquals(2, result.getData().getContent().size());
        assertEquals(2, result.getData().getPaginationDTO().getSize());
        assertEquals(2, result.getData().getPaginationDTO().getTotalPages());
    }

    @Test
    @DisplayName("Happy Flow: List All Products with sorting - Should return sorted results")
    void testListAllProducts_WithSorting() {
        Response<SearchResultDTO> result = searchService.listAllProducts(0, 10, "name", "desc");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getContent());
        assertEquals(3, result.getData().getContent().size());
        // Verify sorting (descending order)
        String firstProductName = result.getData().getContent().get(0).getName();
        String lastProductName = result.getData().getContent().get(2).getName();
        assertTrue(firstProductName.compareTo(lastProductName) > 0);
    }

    @Test
    @DisplayName("Happy Flow: Get Product Search with multiple results - Should return all matching products")
    void testGetProductSearch_MultipleResults() {
        SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
        searchRequestDTO.setQuery("smartphone");
        searchRequestDTO.setPage(0);
        searchRequestDTO.setSize(10);

        Response<SearchResultDTO> result = searchService.getProductSearch(searchRequestDTO);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getContent());
        assertTrue(result.getData().getContent().size() > 0);
        assertEquals("SKU002", result.getData().getContent().get(0).getSku());
        assertEquals("Smartphone", result.getData().getContent().get(0).getName());
    }
}

