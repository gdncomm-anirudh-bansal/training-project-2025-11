package com.Project.Cart.Controller;

import com.Project.Cart.Client.MemberServiceClient;
import com.Project.Cart.DTO.CartAddRequestDTO;
import com.Project.Cart.DTO.ErrorDTO;
import com.Project.Cart.DTO.MemberStatusDTO;
import com.Project.Cart.DTO.Response;
import com.Project.Cart.Service.CartService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final MemberServiceClient memberServiceClient;

    @PostMapping("/items")
    public ResponseEntity<Response<Object>> addItemToCart(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value ="X-Auth-Needed",defaultValue = "true") Boolean isAuthNeeded,
            @RequestBody CartAddRequestDTO request) {

        try {

            if (userId == null || userId.isEmpty()) {
                Response<Object> errorResponse = new Response<>(
                        null,
                        null,
                        400,
                        false,
                        new ErrorDTO("BAD_REQUEST", "User ID is required")
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }


            Long memberId;
            try {
                memberId = Long.parseLong(userId);
            } catch (NumberFormatException e) {
                Response<Object> errorResponse = new Response<>(
                        null,
                        null,
                        400,
                        false,
                        new ErrorDTO("BAD_REQUEST", "Invalid user ID format")
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }


            ResponseEntity<Response<Object>> memberStatusResponse = validateMemberStatus(memberId);
            if (memberStatusResponse != null) {
                return memberStatusResponse;
            }


            Response<Object> response = cartService.addItemToCart(memberId, request);


            if (!response.isSuccess()) {
                if (response.getError() != null && "PRODUCT_NOT_FOUND".equals(response.getError().getCode())) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            log.error("Error in addItemToCart endpoint: {}", e.getMessage(), e);
            Response<Object> errorResponse = new Response<>(
                    null,
                    null,
                    500,
                    false,
                    new ErrorDTO("INTERNAL_ERROR", "An error occurred processing the request")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<Response<Object>> getCart(
            @RequestHeader(value = "X-User-Id", required = true) String userId,@RequestHeader(value ="X-Auth-Needed",defaultValue = "true") Boolean isAuthNeeded) {

        try {

            if (userId == null || userId.isEmpty()) {
                Response<Object> errorResponse = new Response<>(
                        null,
                        null,
                        400,
                        false,
                        new ErrorDTO("BAD_REQUEST", "User ID is required")
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }


            Long memberId;
            try {
                memberId = Long.parseLong(userId);
            } catch (NumberFormatException e) {
                Response<Object> errorResponse = new Response<>(
                        null,
                        null,
                        400,
                        false,
                        new ErrorDTO("BAD_REQUEST", "Invalid user ID format")
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }


            ResponseEntity<Response<Object>> memberStatusResponse = validateMemberStatus(memberId);
            if (memberStatusResponse != null) {
                return memberStatusResponse;
            }


            Response<Object> response = cartService.getCart(memberId);

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            log.error("Error in getCart endpoint: {}", e.getMessage(), e);
            Response<Object> errorResponse = new Response<>(
                    null,
                    null,
                    500,
                    false,
                    new ErrorDTO("INTERNAL_ERROR", "An error occurred processing the request")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/items/{sku}")
    public ResponseEntity<Response<Object>> updateCartItem(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value ="X-Auth-Needed",defaultValue = "true") Boolean isAuthNeeded,
            @PathVariable("sku") String sku,
            @RequestBody com.Project.Cart.DTO.CartUpdateRequestDTO request) {

        try {

            if (userId == null || userId.isEmpty()) {
                Response<Object> errorResponse = new Response<>(
                        null,
                        null,
                        400,
                        false,
                        new ErrorDTO("BAD_REQUEST", "User ID is required")
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }


            Long memberId;
            try {
                memberId = Long.parseLong(userId);
            } catch (NumberFormatException e) {
                Response<Object> errorResponse = new Response<>(
                        null,
                        null,
                        400,
                        false,
                        new ErrorDTO("BAD_REQUEST", "Invalid user ID format")
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }


            ResponseEntity<Response<Object>> memberStatusResponse = validateMemberStatus(memberId);
            if (memberStatusResponse != null) {
                return memberStatusResponse;
            }


            if (request.getQuantity() <= 0) {
                Response<Object> errorResponse = new Response<>(
                        null,
                        null,
                        400,
                        false,
                        new ErrorDTO("BAD_REQUEST", "Quantity must be greater than 0")
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }


            Response<Object> response = cartService.updateCartItem(memberId, sku, request);


            if (!response.isSuccess()) {
                if (response.getError() != null && 
                    ("CART_NOT_FOUND".equals(response.getError().getCode()) || 
                     "ITEM_NOT_FOUND".equals(response.getError().getCode()))) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            log.error("Error in updateCartItem endpoint: {}", e.getMessage(), e);
            Response<Object> errorResponse = new Response<>(
                    null,
                    null,
                    500,
                    false,
                    new ErrorDTO("INTERNAL_ERROR", "An error occurred processing the request")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/items/{sku}")
    public ResponseEntity<Response<Object>> deleteCartItem(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value ="X-Auth-Needed",defaultValue = "true") Boolean isAuthNeeded,
            @PathVariable("sku") String sku) {

        try {

            if (userId == null || userId.isEmpty()) {
                Response<Object> errorResponse = new Response<>(
                        null,
                        null,
                        400,
                        false,
                        new ErrorDTO("BAD_REQUEST", "User ID is required")
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }


            Long memberId;
            try {
                memberId = Long.parseLong(userId);
            } catch (NumberFormatException e) {
                Response<Object> errorResponse = new Response<>(
                        null,
                        null,
                        400,
                        false,
                        new ErrorDTO("BAD_REQUEST", "Invalid user ID format")
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }


            ResponseEntity<Response<Object>> memberStatusResponse = validateMemberStatus(memberId);
            if (memberStatusResponse != null) {
                return memberStatusResponse;
            }


            Response<Object> response = cartService.deleteCartItem(memberId, sku);


            if (!response.isSuccess()) {
                if (response.getError() != null && 
                    ("CART_NOT_FOUND".equals(response.getError().getCode()) || 
                     "ITEM_NOT_FOUND".equals(response.getError().getCode()))) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            log.error("Error in deleteCartItem endpoint: {}", e.getMessage(), e);
            Response<Object> errorResponse = new Response<>(
                    null,
                    null,
                    500,
                    false,
                    new ErrorDTO("INTERNAL_ERROR", "An error occurred processing the request")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    private ResponseEntity<Response<Object>> validateMemberStatus(Long memberId) {
        try {
            MemberStatusDTO memberStatus = memberServiceClient.getMemberStatus(memberId);
            
            if (memberStatus == null || memberStatus.getStatus() == null || 
                !"active".equalsIgnoreCase(memberStatus.getStatus())) {
                Response<Object> errorResponse = new Response<>(
                        null,
                        null,
                        401,
                        false,
                        new ErrorDTO("UNAUTHORIZED", "Member status is not active")
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            return null;
        } catch (FeignException.NotFound e) {
            log.error("Member not found: {}", memberId);
            Response<Object> errorResponse = new Response<>(
                    null,
                    null,
                    401,
                    false,
                    new ErrorDTO("UNAUTHORIZED", "Member not found")
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (FeignException e) {
            log.error("Error calling member service: {}", e.getMessage());
            Response<Object> errorResponse = new Response<>(
                    null,
                    null,
                    401,
                    false,
                    new ErrorDTO("UNAUTHORIZED", "Unable to validate member status")
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error validating member status: {}", e.getMessage(), e);
            Response<Object> errorResponse = new Response<>(
                    null,
                    null,
                    401,
                    false,
                    new ErrorDTO("UNAUTHORIZED", "Error validating member status")
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}
