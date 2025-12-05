package com.Project.Cart.Integration;

import com.Project.Cart.Client.MemberServiceClient;
import com.Project.Cart.Client.ProductServiceClient;
import com.Project.Cart.DTO.CartAddRequestDTO;
import com.Project.Cart.DTO.MemberStatusDTO;
import com.Project.Cart.DTO.ProductDTO;
import com.Project.Cart.Entity.Cart;
import com.Project.Cart.Repository.CartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberServiceClient memberServiceClient;

    @MockBean
    private ProductServiceClient productServiceClient;

    private MemberStatusDTO activeMemberStatus;
    private ProductDTO productDTO;
    private String userId;
    private Long memberId;

    @BeforeEach
    void setUp() {

       // cartRepository.deleteAll();


        userId = "1";
        memberId = 1L;


        activeMemberStatus = new MemberStatusDTO();
        activeMemberStatus.setStatus("active");
        activeMemberStatus.setMemberId(memberId);


        productDTO = new ProductDTO();
        productDTO.setSku("SKU123");
        productDTO.setName("Test Product");
        productDTO.setPrice(1000L);
        productDTO.setProductImage("https://example.com/image.jpg");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("color", "red");
        attributes.put("size", "M");
        productDTO.setAttributes(attributes);


        when(memberServiceClient.getMemberStatus(anyLong())).thenReturn(activeMemberStatus);

        // Mock product service client
        when(productServiceClient.getProductBySku(anyString())).thenReturn(productDTO);
    }



    @Test
    void testAddItemToCart_Integration_HappyFlow() throws Exception {

        CartAddRequestDTO request = new CartAddRequestDTO();
        request.setProductSku("SKU123");
        request.setQuantity(2);

        String requestBody = objectMapper.writeValueAsString(request);


        mockMvc.perform(post("/api/cart/items")
                        .header("X-User-Id", userId)
                        .header("X-Auth-Needed", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Item added to cart successfully"))
                .andExpect(jsonPath("$.data.memberId").value(memberId))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].sku").value("SKU123"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                .andExpect(jsonPath("$.data.items[0].price").value(1000))
                .andExpect(jsonPath("$.data.summary.itemCount").value(1))
                .andExpect(jsonPath("$.data.summary.totalQuantity").value(2))
                .andExpect(jsonPath("$.data.summary.subtotal").value(2000));


        Cart savedCart = cartRepository.findById(memberId).orElse(null);
        assertNotNull(savedCart);
        assertEquals(1, savedCart.getItems().size());
        assertEquals("SKU123", savedCart.getItems().get(0).getSku());
        assertEquals(2, savedCart.getItems().get(0).getQuantity());
        assertEquals(2000, savedCart.getSummary().getSubtotal());
    }

    @Test
    void testGetCart_Integration_HappyFlow_WithItems() throws Exception {

        CartAddRequestDTO addRequest = new CartAddRequestDTO();
        addRequest.setProductSku("SKU123");
        addRequest.setQuantity(3);

        String addRequestBody = objectMapper.writeValueAsString(addRequest);

        mockMvc.perform(post("/api/cart/items")
                        .header("X-User-Id", userId)
                        .header("X-Auth-Needed", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addRequestBody))
                .andExpect(status().isOk());


        MvcResult result = mockMvc.perform(get("/api/cart")
                        .header("X-User-Id", userId)
                        .header("X-Auth-Needed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Cart retrieved successfully"))
                .andExpect(jsonPath("$.data.memberId").value(memberId))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].sku").value("SKU123"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(3))
                .andExpect(jsonPath("$.data.summary.itemCount").value(1))
                .andExpect(jsonPath("$.data.summary.totalQuantity").value(3));


        Cart cart = cartRepository.findById(memberId).orElse(null);
        assertNotNull(cart);
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void testGetCart_Integration_HappyFlow_EmptyCart() throws Exception {

        mockMvc.perform(get("/api/cart")
                        .header("X-User-Id", userId)
                        .header("X-Auth-Needed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Cart retrieved successfully"))
                .andExpect(jsonPath("$.data.memberId").value(memberId))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.summary.itemCount").value(0))
                .andExpect(jsonPath("$.data.summary.totalQuantity").value(0));
    }

    @Test
    void testUpdateCartItem_Integration_HappyFlow() throws Exception {

        CartAddRequestDTO addRequest = new CartAddRequestDTO();
        addRequest.setProductSku("SKU123");
        addRequest.setQuantity(2);

        String addRequestBody = objectMapper.writeValueAsString(addRequest);

        mockMvc.perform(post("/api/cart/items")
                        .header("X-User-Id", userId)
                        .header("X-Auth-Needed", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addRequestBody))
                .andExpect(status().isOk());


        String updateRequestBody = "{\"quantity\": 5}";

        MvcResult result = (MvcResult) mockMvc.perform(put("/api/cart/items/SKU123")
                        .header("X-User-Id", userId)
                        .header("X-Auth-Needed", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Item updated successfully"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(5))
                .andExpect(jsonPath("$.data.summary.totalQuantity").value(5))
                .andExpect(jsonPath("$.data.summary.subtotal").value(5000));


        Cart updatedCart = cartRepository.findById(memberId).orElse(null);
        assertNotNull(updatedCart);
        assertEquals(1, updatedCart.getItems().size());
        assertEquals(5, updatedCart.getItems().get(0).getQuantity());
        assertEquals(5000, updatedCart.getSummary().getSubtotal());
    }

    @Test
    void testDeleteCartItem_Integration_HappyFlow() throws Exception {

        CartAddRequestDTO addRequest = new CartAddRequestDTO();
        addRequest.setProductSku("SKU123");
        addRequest.setQuantity(2);

        String addRequestBody = objectMapper.writeValueAsString(addRequest);

        mockMvc.perform(post("/api/cart/items")
                        .header("X-User-Id", userId)
                        .header("X-Auth-Needed", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addRequestBody))
                .andExpect(status().isOk());


        Cart cartBeforeDelete = cartRepository.findById(memberId).orElse(null);
        assertNotNull(cartBeforeDelete);
        assertEquals(1, cartBeforeDelete.getItems().size());


        MvcResult result = (MvcResult) mockMvc.perform(delete("/api/cart/items/SKU123")
                        .header("X-User-Id", userId)
                        .header("X-Auth-Needed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Item removed from cart"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.summary.itemCount").value(0))
                .andExpect(jsonPath("$.data.summary.totalQuantity").value(0));


        Cart cartAfterDelete = cartRepository.findById(memberId).orElse(null);
        assertNotNull(cartAfterDelete);
        assertTrue(cartAfterDelete.getItems().isEmpty());
        assertEquals(0, cartAfterDelete.getSummary().getItemCount());
    }

    @Test
    void testAddMultipleItems_Integration_HappyFlow() throws Exception {

        CartAddRequestDTO request1 = new CartAddRequestDTO();
        request1.setProductSku("SKU123");
        request1.setQuantity(2);

        String requestBody1 = objectMapper.writeValueAsString(request1);

        mockMvc.perform(post("/api/cart/items")
                        .header("X-User-Id", userId)
                        .header("X-Auth-Needed", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody1))
                .andExpect(status().isOk());


        ProductDTO productDTO2 = new ProductDTO();
        productDTO2.setSku("SKU456");
        productDTO2.setName("Second Product");
        productDTO2.setPrice(2000L);
        productDTO2.setProductImage("https://example.com/image2.jpg");
        Map<String, String> attributes2 = new HashMap<>();
        attributes2.put("color", "blue");
        productDTO2.setAttributes(attributes2);

        when(productServiceClient.getProductBySku("SKU456")).thenReturn(productDTO2);


        CartAddRequestDTO request2 = new CartAddRequestDTO();
        request2.setProductSku("SKU456");
        request2.setQuantity(1);

        String requestBody2 = objectMapper.writeValueAsString(request2);


        mockMvc.perform(post("/api/cart/items")
                        .header("X-User-Id", userId)
                        .header("X-Auth-Needed", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.summary.itemCount").value(2))
                .andExpect(jsonPath("$.data.summary.totalQuantity").value(3))
                .andExpect(jsonPath("$.data.summary.subtotal").value(4000)); // (2 * 1000) + (1 * 2000)


        Cart cart = cartRepository.findById(memberId).orElse(null);
        assertNotNull(cart);
        assertEquals(2, cart.getItems().size());
        assertEquals(3, cart.getSummary().getTotalQuantity());
        assertEquals(4000, cart.getSummary().getSubtotal());
    }

    @Test
    void testUpdateExistingItemQuantity_Integration_HappyFlow() throws Exception {

        CartAddRequestDTO addRequest = new CartAddRequestDTO();
        addRequest.setProductSku("SKU123");
        addRequest.setQuantity(2);

        String addRequestBody = objectMapper.writeValueAsString(addRequest);

        mockMvc.perform(post("/api/cart/items")
                        .header("X-User-Id", userId)
                        .header("X-Auth-Needed", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addRequestBody))
                .andExpect(status().isOk());


        CartAddRequestDTO updateRequest = new CartAddRequestDTO();
        updateRequest.setProductSku("SKU123");
        updateRequest.setQuantity(4);

        String updateRequestBody = objectMapper.writeValueAsString(updateRequest);


        mockMvc.perform(post("/api/cart/items")
                        .header("X-User-Id", userId)
                        .header("X-Auth-Needed", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].quantity").value(4))
                .andExpect(jsonPath("$.data.summary.itemCount").value(1))
                .andExpect(jsonPath("$.data.summary.totalQuantity").value(4))
                .andExpect(jsonPath("$.data.summary.subtotal").value(4000));


        Cart cart = cartRepository.findById(memberId).orElse(null);
        assertNotNull(cart);
        assertEquals(1, cart.getItems().size());
        assertEquals(4, cart.getItems().get(0).getQuantity());
    }
}

