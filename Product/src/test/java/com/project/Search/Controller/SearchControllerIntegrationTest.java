package com.project.Search.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("SearchController Integration Tests")
class SearchControllerIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
        categoryRepository.save(category);

        // Create test product
        Product product = new Product();
        product.setSku("SKU001");
        product.setName("Laptop");
        product.setDescription("High performance laptop");
        product.setBrand("Dell");
        product.setStatus("ACTIVE");

        Price price = new Price();
        price.setAmount(50000L);
        price.setDiscountedAmount(45000L);
        price.setCurrency("INR");
        product.setPrice(price);

        Image image = new Image();
        image.setUrl("https://example.com/laptop.jpg");
        image.setIsPrimary(true);
        image.setAlt("Laptop image");
        List<Image> images = new ArrayList<>();
        images.add(image);
        product.setImages(images);

        productRepository.save(product);
    }

    @Test
    @DisplayName("Happy Flow: Search Product - Should return 200 OK with search results")
    void testSearchProduct_HappyFlow() throws Exception {
        SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
        searchRequestDTO.setQuery("laptop");
        searchRequestDTO.setPage(0);
        searchRequestDTO.setSize(10);

        MvcResult result = mockMvc.perform(post("/api/search/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Search completed successfully"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].sku").value("SKU001"))
                .andExpect(jsonPath("$.data.content[0].name").value("Laptop"))
                .andExpect(jsonPath("$.data.paginationDTO.page").value(0))
                .andExpect(jsonPath("$.data.paginationDTO.size").value(10))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);
    }

    @Test
    @DisplayName("Happy Flow: Get Product Detail - Should return 200 OK with product details")
    void testGetProductDetail_HappyFlow() throws Exception {
        mockMvc.perform(get("/api/search/get/detailById")
                        .param("skuId", "SKU001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU001"))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.description").value("High performance laptop"))
                .andExpect(jsonPath("$.brand").value("Dell"))
                .andExpect(jsonPath("$.price.amount").value(50000))
                .andExpect(jsonPath("$.price.discountedAmount").value(45000));
    }

    @Test
    @DisplayName("Happy Flow: Get Category Detail - Should return 200 OK with category details")
    void testGetCategoryDetail_HappyFlow() throws Exception {
        mockMvc.perform(get("/api/search/get/CategoryId")
                        .param("categoryId", "CAT001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value("CAT001"))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.description").value("Electronic products"));
    }

    @Test
    @DisplayName("Happy Flow: List All Products - Should return 200 OK with paginated products")
    void testListAllProducts_HappyFlow() throws Exception {
        mockMvc.perform(get("/api/search/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name")
                        .param("order", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Products listed successfully"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].sku").value("SKU001"))
                .andExpect(jsonPath("$.data.paginationDTO.page").value(0))
                .andExpect(jsonPath("$.data.paginationDTO.size").value(10))
                .andExpect(jsonPath("$.data.paginationDTO.totalElements").value(1));
    }

    @Test
    @DisplayName("Happy Flow: List All Products with default parameters - Should return 200 OK")
    void testListAllProducts_WithDefaultParameters() throws Exception {
        mockMvc.perform(get("/api/search/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.paginationDTO.page").value(0))
                .andExpect(jsonPath("$.data.paginationDTO.size").value(10));
    }

    @Test
    @DisplayName("Happy Flow: Search Product with pagination - Should return paginated results")
    void testSearchProduct_WithPagination() throws Exception {

        for (int i = 2; i <= 5; i++) {
            Product product = new Product();
            product.setSku("SKU00" + i);
            product.setName("Product " + i);
            product.setDescription("Description " + i);
            product.setBrand("Brand " + i);
            product.setStatus("ACTIVE");
            productRepository.save(product);
        }

        SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
        searchRequestDTO.setQuery("Product");
        searchRequestDTO.setPage(0);
        searchRequestDTO.setSize(2);

        mockMvc.perform(post("/api/search/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.paginationDTO.size").value(2));
    }
}

