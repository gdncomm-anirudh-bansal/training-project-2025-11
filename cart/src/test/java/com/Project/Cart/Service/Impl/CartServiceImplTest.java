package com.Project.Cart.Service.Impl;

import com.Project.Cart.Client.ProductServiceClient;
import com.Project.Cart.DTO.CartAddRequestDTO;
import com.Project.Cart.DTO.CartUpdateRequestDTO;
import com.Project.Cart.DTO.ErrorDTO;
import com.Project.Cart.DTO.ProductDTO;
import com.Project.Cart.DTO.Response;
import com.Project.Cart.Entity.Cart;
import com.Project.Cart.Entity.CartItem;
import com.Project.Cart.Entity.CartSummary;
import com.Project.Cart.Repository.CartRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private CartAddRequestDTO cartAddRequest;
    private ProductDTO productDTO;
    private Cart existingCart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {

        cartAddRequest = new CartAddRequestDTO();
        cartAddRequest.setProductSku("SKU123");
        cartAddRequest.setQuantity(2);


        productDTO = new ProductDTO();
        productDTO.setSku("SKU123");
        productDTO.setName("Test Product");
        productDTO.setPrice(1000L);
        productDTO.setProductImage("image.jpg");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("color", "red");
        attributes.put("size", "M");
        productDTO.setAttributes(attributes);


        existingCart = new Cart();
        existingCart.setMemberId(1L);
        existingCart.setStatus("ACTIVE");

        CartSummary summary = new CartSummary();
        summary.setItemCount(0);
        summary.setTotalQuantity(0);
        summary.setSubtotal(0);
        summary.setTax(0);
        summary.setShipping(0);
        summary.setDiscount(0);
        summary.setTotal(0);
        existingCart.setSummary(summary);
        existingCart.setItems(new ArrayList<>());


        cartItem = new CartItem();
        cartItem.setSku("SKU123");
        cartItem.setName("Test Product");
        cartItem.setPrice(1000L);
        cartItem.setQuantity(1);
        cartItem.setProductImage("image.jpg");
        cartItem.setStatus("active");
        cartItem.setAttributes(attributes);
    }



    @Test
    void testAddItemToCart_HappyFlow_NewCart() {
        Long memberId = 1L;
        Optional<Cart> emptyCart = Optional.empty();

        when(cartRepository.findById(memberId)).thenReturn(emptyCart);
        when(productServiceClient.getProductBySku("SKU123")).thenReturn(productDTO);
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Response<Object> response = cartService.addItemToCart(memberId, cartAddRequest);


        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertEquals("Item added to cart successfully", response.getMessage());
        assertNull(response.getError());
        assertNotNull(response.getData());

        verify(productServiceClient, times(1)).getProductBySku("SKU123");
        verify(cartRepository, times(1)).findById(memberId);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testAddItemToCart_HappyFlow_ExistingCart_NewItem() {

        Long memberId = 1L;
        Optional<Cart> cartOptional = Optional.of(existingCart);

        when(cartRepository.findById(memberId)).thenReturn(cartOptional);
        when(productServiceClient.getProductBySku("SKU123")).thenReturn(productDTO);
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Response<Object> response = cartService.addItemToCart(memberId, cartAddRequest);


        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertEquals("Item added to cart successfully", response.getMessage());
        assertNull(response.getError());

        Cart savedCart = (Cart) response.getData();
        assertNotNull(savedCart);
        assertEquals(1, savedCart.getItems().size());
        assertEquals("SKU123", savedCart.getItems().get(0).getSku());
        assertEquals(2, savedCart.getItems().get(0).getQuantity());

        verify(productServiceClient, times(1)).getProductBySku("SKU123");
        verify(cartRepository, times(1)).findById(memberId);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testAddItemToCart_HappyFlow_ExistingCart_UpdateQuantity() {

        Long memberId = 1L;
        existingCart.getItems().add(cartItem);
        Optional<Cart> cartOptional = Optional.of(existingCart);

        when(cartRepository.findById(memberId)).thenReturn(cartOptional);
        when(productServiceClient.getProductBySku("SKU123")).thenReturn(productDTO);
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Response<Object> response = cartService.addItemToCart(memberId, cartAddRequest);


        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());

        Cart savedCart = (Cart) response.getData();
        assertNotNull(savedCart);
        assertEquals(1, savedCart.getItems().size());
        assertEquals(2, savedCart.getItems().get(0).getQuantity()); // Updated quantity

        verify(productServiceClient, times(1)).getProductBySku("SKU123");
        verify(cartRepository, times(1)).findById(memberId);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testGetCart_HappyFlow_ExistingCart() {

        Long memberId = 1L;
        existingCart.getItems().add(cartItem);
        Optional<Cart> cartOptional = Optional.of(existingCart);


        ProductDTO updatedProductDTO = new ProductDTO();
        updatedProductDTO.setSku("SKU123");
        updatedProductDTO.setName("Test Product");
        updatedProductDTO.setPrice(1500L); // Different price to trigger update
        updatedProductDTO.setProductImage("image.jpg");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("color", "red");
        attributes.put("size", "M");
        updatedProductDTO.setAttributes(attributes);

        when(cartRepository.findById(memberId)).thenReturn(cartOptional);
        when(productServiceClient.getProductBySku("SKU123")).thenReturn(updatedProductDTO);
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Response<Object> response = cartService.getCart(memberId);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertEquals("Cart retrieved successfully", response.getMessage());
        assertNull(response.getError());
        assertNotNull(response.getData());

        verify(cartRepository, times(1)).findById(memberId);
        verify(productServiceClient, times(1)).getProductBySku("SKU123");
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testGetCart_HappyFlow_EmptyCart() {

        Long memberId = 1L;
        Optional<Cart> emptyCart = Optional.empty();

        when(cartRepository.findById(memberId)).thenReturn(emptyCart);

        Response<Object> response = cartService.getCart(memberId);


        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertEquals("Cart retrieved successfully", response.getMessage());
        assertNull(response.getError());
        assertNotNull(response.getData());

        Cart cart = (Cart) response.getData();
        assertEquals(memberId, cart.getMemberId());
        assertTrue(cart.getItems().isEmpty());

        verify(cartRepository, times(1)).findById(memberId);
        verify(productServiceClient, never()).getProductBySku(anyString());
    }

    @Test
    void testUpdateCartItem_HappyFlow() {

        Long memberId = 1L;
        String sku = "SKU123";
        CartUpdateRequestDTO updateRequest = new CartUpdateRequestDTO();
        updateRequest.setQuantity(5);

        existingCart.getItems().add(cartItem);
        Optional<Cart> cartOptional = Optional.of(existingCart);

        when(cartRepository.findById(memberId)).thenReturn(cartOptional);
        when(productServiceClient.getProductBySku(sku)).thenReturn(productDTO);
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Response<Object> response = cartService.updateCartItem(memberId, sku, updateRequest);


        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertEquals("Item updated successfully", response.getMessage());
        assertNull(response.getError());

        Cart savedCart = (Cart) response.getData();
        assertNotNull(savedCart);
        assertEquals(1, savedCart.getItems().size());
        assertEquals(5, savedCart.getItems().get(0).getQuantity());

        verify(cartRepository, times(1)).findById(memberId);
        verify(productServiceClient, times(1)).getProductBySku(sku);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testDeleteCartItem_HappyFlow() {

        Long memberId = 1L;
        String sku = "SKU123";

        existingCart.getItems().add(cartItem);
        Optional<Cart> cartOptional = Optional.of(existingCart);

        when(cartRepository.findById(memberId)).thenReturn(cartOptional);
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Response<Object> response = cartService.deleteCartItem(memberId, sku);


        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertEquals("Item removed from cart", response.getMessage());
        assertNull(response.getError());

        Cart savedCart = (Cart) response.getData();
        assertNotNull(savedCart);
        assertTrue(savedCart.getItems().isEmpty());

        verify(cartRepository, times(1)).findById(memberId);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }


    @Test
    void testAddItemToCart_NegativeFlow_ProductNotFound() {

        Long memberId = 1L;

        FeignException.NotFound notFoundException = mock(FeignException.NotFound.class);
        when(productServiceClient.getProductBySku("SKU123")).thenThrow(notFoundException);


        Response<Object> response = cartService.addItemToCart(memberId, cartAddRequest);


        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(404, response.getCode());
        assertNotNull(response.getError());
        assertEquals("PRODUCT_NOT_FOUND", response.getError().getCode());
        assertEquals("Product not found", response.getError().getMessage());

        verify(productServiceClient, times(1)).getProductBySku("SKU123");
        // When product not found, method returns early, so repository is never called
        verify(cartRepository, never()).findById(anyLong());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testAddItemToCart_NegativeFlow_ProductServiceError() {

        Long memberId = 1L;

        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(500);
        when(feignException.contentUTF8()).thenReturn("Internal Server Error");

        when(productServiceClient.getProductBySku("SKU123")).thenThrow(feignException);


        Response<Object> response = cartService.addItemToCart(memberId, cartAddRequest);


        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(500, response.getCode());
        assertNotNull(response.getError());
        assertEquals("PRODUCT_SERVICE_ERROR", response.getError().getCode());
        assertEquals("Error fetching product details", response.getError().getMessage());

        verify(productServiceClient, times(1)).getProductBySku("SKU123");
        verify(cartRepository, never()).findById(anyLong());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testUpdateCartItem_NegativeFlow_CartNotFound() {

        Long memberId = 1L;
        String sku = "SKU123";
        CartUpdateRequestDTO updateRequest = new CartUpdateRequestDTO();
        updateRequest.setQuantity(5);

        Optional<Cart> emptyCart = Optional.empty();

        when(cartRepository.findById(memberId)).thenReturn(emptyCart);


        Response<Object> response = cartService.updateCartItem(memberId, sku, updateRequest);


        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(404, response.getCode());
        assertNotNull(response.getError());
        assertEquals("CART_NOT_FOUND", response.getError().getCode());
        assertEquals("Cart not found", response.getError().getMessage());

        verify(cartRepository, times(1)).findById(memberId);
        verify(productServiceClient, never()).getProductBySku(anyString());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testUpdateCartItem_NegativeFlow_ItemNotFound() {

        Long memberId = 1L;
        String sku = "SKU999"; // Non-existent SKU
        CartUpdateRequestDTO updateRequest = new CartUpdateRequestDTO();
        updateRequest.setQuantity(5);

        existingCart.getItems().add(cartItem); // Cart has SKU123, not SKU999
        Optional<Cart> cartOptional = Optional.of(existingCart);

        when(cartRepository.findById(memberId)).thenReturn(cartOptional);


        Response<Object> response = cartService.updateCartItem(memberId, sku, updateRequest);


        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(404, response.getCode());
        assertNotNull(response.getError());
        assertEquals("ITEM_NOT_FOUND", response.getError().getCode());
        assertEquals("Item not found in cart", response.getError().getMessage());

        verify(cartRepository, times(1)).findById(memberId);
        verify(productServiceClient, never()).getProductBySku(anyString());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testUpdateCartItem_NegativeFlow_ProductNotFound() {

        Long memberId = 1L;
        String sku = "SKU123";
        CartUpdateRequestDTO updateRequest = new CartUpdateRequestDTO();
        updateRequest.setQuantity(5);

        existingCart.getItems().add(cartItem);
        Optional<Cart> cartOptional = Optional.of(existingCart);

        FeignException.NotFound notFoundException = mock(FeignException.NotFound.class);

        when(cartRepository.findById(memberId)).thenReturn(cartOptional);
        when(productServiceClient.getProductBySku(sku)).thenThrow(notFoundException);


        Response<Object> response = cartService.updateCartItem(memberId, sku, updateRequest);


        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(404, response.getCode());
        assertNotNull(response.getError());
        assertEquals("PRODUCT_NOT_FOUND", response.getError().getCode());
        assertEquals("Product not found", response.getError().getMessage());

        verify(cartRepository, times(1)).findById(memberId);
        verify(productServiceClient, times(1)).getProductBySku(sku);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testDeleteCartItem_NegativeFlow_CartNotFound() {

        Long memberId = 1L;
        String sku = "SKU123";

        Optional<Cart> emptyCart = Optional.empty();

        when(cartRepository.findById(memberId)).thenReturn(emptyCart);

        Response<Object> response = cartService.deleteCartItem(memberId, sku);


        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(404, response.getCode());
        assertNotNull(response.getError());
        assertEquals("CART_NOT_FOUND", response.getError().getCode());
        assertEquals("Cart not found", response.getError().getMessage());

        verify(cartRepository, times(1)).findById(memberId);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testDeleteCartItem_NegativeFlow_ItemNotFound() {

        Long memberId = 1L;
        String sku = "SKU999"; // Non-existent SKU

        existingCart.getItems().add(cartItem); // Cart has SKU123, not SKU999
        Optional<Cart> cartOptional = Optional.of(existingCart);

        when(cartRepository.findById(memberId)).thenReturn(cartOptional);


        Response<Object> response = cartService.deleteCartItem(memberId, sku);


        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(404, response.getCode());
        assertNotNull(response.getError());
        assertEquals("ITEM_NOT_FOUND", response.getError().getCode());
        assertEquals("Item not found in cart", response.getError().getMessage());

        verify(cartRepository, times(1)).findById(memberId);
        verify(cartRepository, never()).save(any(Cart.class));
    }
}

