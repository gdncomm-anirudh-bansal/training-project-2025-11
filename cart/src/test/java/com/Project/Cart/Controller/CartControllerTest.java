package com.Project.Cart.Controller;

import com.Project.Cart.Client.MemberServiceClient;
import com.Project.Cart.DTO.CartAddRequestDTO;
import com.Project.Cart.DTO.ErrorDTO;
import com.Project.Cart.DTO.MemberStatusDTO;
import com.Project.Cart.DTO.Response;
import com.Project.Cart.Service.CartService;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private MemberServiceClient memberServiceClient;

    @InjectMocks
    private CartController cartController;

    private MemberStatusDTO activeMemberStatus;
    private CartAddRequestDTO cartAddRequest;
    private Response<Object> successResponse;
    private Response<Object> errorResponse;

    @BeforeEach
    void setUp() {
        // Setup active member status
        activeMemberStatus = new MemberStatusDTO();
        activeMemberStatus.setStatus("active");
        activeMemberStatus.setMemberId(1L);

        // Setup cart add request
        cartAddRequest = new CartAddRequestDTO();
        cartAddRequest.setProductSku("SKU123");
        cartAddRequest.setQuantity(2);

        // Setup success response
        successResponse = new Response<>();
        successResponse.setData("Cart data");
        successResponse.setMessage("Item added to cart successfully");
        successResponse.setCode(200);
        successResponse.setSuccess(true);
        successResponse.setError(null);

        // Setup error response
        errorResponse = new Response<>();
        errorResponse.setData(null);
        errorResponse.setMessage(null);
        errorResponse.setCode(404);
        errorResponse.setSuccess(false);
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode("PRODUCT_NOT_FOUND");
        errorDTO.setMessage("Product not found");
        errorResponse.setError(errorDTO);
    }


    @Test
    void testAddItemToCart_HappyFlow() {

        String userId = "1";
        Boolean isAuthNeeded = true;

        when(memberServiceClient.getMemberStatus(1L)).thenReturn(activeMemberStatus);
        when(cartService.addItemToCart(anyLong(), any(CartAddRequestDTO.class)))
                .thenReturn(successResponse);


        ResponseEntity<Response<Object>> response = cartController.addItemToCart(
                userId, isAuthNeeded, cartAddRequest);


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(200, response.getBody().getCode());
        assertEquals("Item added to cart successfully", response.getBody().getMessage());

        verify(memberServiceClient, times(1)).getMemberStatus(1L);
        verify(cartService, times(1)).addItemToCart(1L, cartAddRequest);
    }

    @Test
    void testGetCart_HappyFlow() {

        String userId = "1";
        Boolean isAuthNeeded = true;

        when(memberServiceClient.getMemberStatus(1L)).thenReturn(activeMemberStatus);
        when(cartService.getCart(anyLong())).thenReturn(successResponse);

        ResponseEntity<Response<Object>> response = cartController.getCart(userId, isAuthNeeded);


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        verify(memberServiceClient, times(1)).getMemberStatus(1L);
        verify(cartService, times(1)).getCart(1L);
    }


    @Test
    void testAddItemToCart_NegativeFlow_InvalidUserId() {

        String userId = "invalid";
        Boolean isAuthNeeded = true;


        ResponseEntity<Response<Object>> response = cartController.addItemToCart(
                userId, isAuthNeeded, cartAddRequest);


        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(400, response.getBody().getCode());
        assertNotNull(response.getBody().getError());
        assertEquals("BAD_REQUEST", response.getBody().getError().getCode());
        assertEquals("Invalid user ID format", response.getBody().getError().getMessage());

        verify(memberServiceClient, never()).getMemberStatus(anyLong());
        verify(cartService, never()).addItemToCart(anyLong(), any());
    }

    @Test
    void testAddItemToCart_NegativeFlow_MissingUserId() {

        String userId = null;
        Boolean isAuthNeeded = true;


        ResponseEntity<Response<Object>> response = cartController.addItemToCart(
                userId, isAuthNeeded, cartAddRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(400, response.getBody().getCode());
        assertNotNull(response.getBody().getError());
        assertEquals("BAD_REQUEST", response.getBody().getError().getCode());
        assertEquals("User ID is required", response.getBody().getError().getMessage());

        verify(memberServiceClient, never()).getMemberStatus(anyLong());
        verify(cartService, never()).addItemToCart(anyLong(), any());
    }

    @Test
    void testAddItemToCart_NegativeFlow_MemberNotActive() {

        String userId = "1";
        Boolean isAuthNeeded = true;

        MemberStatusDTO inactiveMemberStatus = new MemberStatusDTO();
        inactiveMemberStatus.setStatus("inactive");
        inactiveMemberStatus.setMemberId(1L);

        when(memberServiceClient.getMemberStatus(1L)).thenReturn(inactiveMemberStatus);


        ResponseEntity<Response<Object>> response = cartController.addItemToCart(
                userId, isAuthNeeded, cartAddRequest);


        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(401, response.getBody().getCode());
        assertNotNull(response.getBody().getError());
        assertEquals("UNAUTHORIZED", response.getBody().getError().getCode());
        assertEquals("Member status is not active", response.getBody().getError().getMessage());

        verify(memberServiceClient, times(1)).getMemberStatus(1L);
        verify(cartService, never()).addItemToCart(anyLong(), any());
    }

    @Test
    void testAddItemToCart_NegativeFlow_MemberNotFound() {

        String userId = "1";
        Boolean isAuthNeeded = true;

        FeignException.NotFound notFoundException = mock(FeignException.NotFound.class);
        when(memberServiceClient.getMemberStatus(1L)).thenThrow(notFoundException);


        ResponseEntity<Response<Object>> response = cartController.addItemToCart(
                userId, isAuthNeeded, cartAddRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(401, response.getBody().getCode());
        assertNotNull(response.getBody().getError());
        assertEquals("UNAUTHORIZED", response.getBody().getError().getCode());
        assertEquals("Member not found", response.getBody().getError().getMessage());

        verify(memberServiceClient, times(1)).getMemberStatus(1L);
        verify(cartService, never()).addItemToCart(anyLong(), any());
    }

    @Test
    void testAddItemToCart_NegativeFlow_ProductNotFound() {

        String userId = "1";
        Boolean isAuthNeeded = true;

        when(memberServiceClient.getMemberStatus(1L)).thenReturn(activeMemberStatus);
        when(cartService.addItemToCart(anyLong(), any(CartAddRequestDTO.class)))
                .thenReturn(errorResponse);

        ResponseEntity<Response<Object>> response = cartController.addItemToCart(
                userId, isAuthNeeded, cartAddRequest);


        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(404, response.getBody().getCode());
        assertNotNull(response.getBody().getError());
        assertEquals("PRODUCT_NOT_FOUND", response.getBody().getError().getCode());

        verify(memberServiceClient, times(1)).getMemberStatus(1L);
        verify(cartService, times(1)).addItemToCart(1L, cartAddRequest);
    }

    @Test
    void testGetCart_NegativeFlow_EmptyUserId() {

        String userId = "";
        Boolean isAuthNeeded = true;

        ResponseEntity<Response<Object>> response = cartController.getCart(userId, isAuthNeeded);


        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(400, response.getBody().getCode());
        assertNotNull(response.getBody().getError());
        assertEquals("BAD_REQUEST", response.getBody().getError().getCode());
        assertEquals("User ID is required", response.getBody().getError().getMessage());

        verify(memberServiceClient, never()).getMemberStatus(anyLong());
        verify(cartService, never()).getCart(anyLong());
    }
}

