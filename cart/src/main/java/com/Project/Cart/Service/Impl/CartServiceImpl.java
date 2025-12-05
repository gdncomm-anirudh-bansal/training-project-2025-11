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
import com.Project.Cart.Service.CartService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    @CacheEvict(value = "cart", key = "#memberId")
    public Response<Object> addItemToCart(Long memberId, CartAddRequestDTO request) {
        try {
            // Fetch product details from Search Service
            ProductDTO product;
            try {
                product = productServiceClient.getProductBySku(request.getProductSku());
                if (product == null) {
                    ErrorDTO error = new ErrorDTO("PRODUCT_NOT_FOUND", "Product not found");
                    return new Response<>(null, null, 404, false, error);
                }
            } catch (FeignException.NotFound e) {
                // Product not found
                log.error("Product not found for SKU: {}", request.getProductSku());
                ErrorDTO error = new ErrorDTO("PRODUCT_NOT_FOUND", "Product not found");
                return new Response<>(null, null, 404, false, error);
            } catch (FeignException e) {
                log.error("Error calling product service: Status={}, Message={}", e.status(), e.getMessage());
                if (e.contentUTF8() != null) {
                    log.error("Response body: {}", e.contentUTF8());
                }
                ErrorDTO error = new ErrorDTO("PRODUCT_SERVICE_ERROR", "Error fetching product details");
                return new Response<>(null, null, 500, false, error);
            } catch (Exception e) {
                log.error("Unexpected error deserializing product response: {}", e.getMessage(), e);
                ErrorDTO error = new ErrorDTO("PRODUCT_SERVICE_ERROR", "Error processing product details");
                return new Response<>(null, null, 500, false, error);
            }


            Optional<Cart> cartOptional = cartRepository.findById(memberId);
            Cart cart = cartOptional.orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setMemberId(memberId);
                newCart.setItems(new ArrayList<>());
                newCart.setStatus("ACTIVE");
                CartSummary summary = new CartSummary();
                summary.setItemCount(0);
                summary.setTotalQuantity(0);
                summary.setSubtotal(0);
                summary.setTax(0);
                summary.setShipping(0);
                summary.setDiscount(0);
                summary.setTotal(0);
                newCart.setSummary(summary);
                return newCart;
            });

            List<CartItem> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();
            Optional<CartItem> existingItem = items.stream()
                    .filter(item -> item.getSku().equals(request.getProductSku()))
                    .findFirst();

            if (existingItem.isPresent()) {

                CartItem item = existingItem.get();
                item.setQuantity(request.getQuantity());
            } else {

                CartItem newItem = new CartItem();
                newItem.setSku(product.getSku());
                newItem.setName(product.getName());
                newItem.setPrice(product.getPrice());
                newItem.setQuantity(request.getQuantity());
                newItem.setProductImage(product.getProductImage());
                newItem.setAttributes(product.getAttributes());
                newItem.setStatus("active");
                items.add(newItem);
            }

            cart.setItems(items);


            CartSummary summary = cart.getSummary() != null ? cart.getSummary() : new CartSummary();
            summary.setItemCount(items.size());
            summary.setTotalQuantity(items.stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum());
            summary.setSubtotal(items.stream()
                    .mapToLong(item -> item.getPrice() * item.getQuantity())
                    .sum());
            summary.setTotal(summary.getSubtotal() + summary.getTax() + summary.getShipping() - summary.getDiscount());
            cart.setSummary(summary);


            cartRepository.save(cart);

            return new Response<>(cart, "Item added to cart successfully", 200, true, null);

        } catch (Exception e) {
            log.error("Error adding item to cart: {}", e.getMessage(), e);
            ErrorDTO error = new ErrorDTO("INTERNAL_ERROR", "An error occurred while adding item to cart");
            return new Response<>(null, null, 500, false, error);
        }
    }

    @Override
    public Response<Object> getCart(Long memberId) {
        try {
            Optional<Cart> cartOptional = cartRepository.findById(memberId);
            
            if (cartOptional.isEmpty()) {

                Cart emptyCart = createEmptyCart(memberId);
                return new Response<>(emptyCart, "Cart retrieved successfully", 200, true, null);
            }

            Cart cart = cartOptional.get();

            if (cart.getItems() == null) {
                cart.setItems(new ArrayList<>());
            }


            List<CartItem> items = cart.getItems();
            boolean cartUpdated = false;
            List<CartItem> itemsToRemove = new ArrayList<>();
            
            for (CartItem item : items) {
                try {

                    ProductDTO product = productServiceClient.getProductBySku(item.getSku());
                    
                    if (product != null) {

                        item.setStatus("active");
                        

                        boolean itemUpdated = false;
                        
                        if (!isEqual(item.getName(), product.getName())) {
                            item.setName(product.getName());
                            itemUpdated = true;
                        }
                        
                        if (item.getPrice() != product.getPrice()) {
                            item.setPrice(product.getPrice());
                            itemUpdated = true;
                        }
                        
                        if (!isEqual(item.getProductImage(), product.getProductImage())) {
                            item.setProductImage(product.getProductImage());
                            itemUpdated = true;
                        }
                        
                        if (!areAttributesEqual(item.getAttributes(), product.getAttributes())) {
                            item.setAttributes(product.getAttributes());
                            itemUpdated = true;
                        }
                        
                        if (itemUpdated) {
                            cartUpdated = true;
                            log.debug("Updated product details for SKU: {}", item.getSku());
                        }
                    } else {

                        log.warn("Product not found for SKU: {}, removing from cart", item.getSku());
                        itemsToRemove.add(item);
                        cartUpdated = true;
                    }
                } catch (FeignException.NotFound e) {

                    log.warn("Product not found (404) for SKU: {}, removing from cart", item.getSku());
                    itemsToRemove.add(item);
                    cartUpdated = true;
                } catch (Exception e) {

                    log.error("Error fetching product details for SKU: {} - {}", item.getSku(), e.getMessage());
                    if (item.getStatus() == null || item.getStatus().isEmpty()) {
                        item.setStatus("inactive");
                        cartUpdated = true;
                    }
                }
            }
            

            if (!itemsToRemove.isEmpty()) {
                items.removeAll(itemsToRemove);
                cart.setItems(items);
                log.info("Removed {} item(s) from cart due to product not found", itemsToRemove.size());
            }


            updateCartSummary(cart);
            

            if (cartUpdated) {
                cartRepository.save(cart);
                log.debug("Cart updated for memberId: {}", memberId);
            }

            return new Response<>(cart, "Cart retrieved successfully", 200, true, null);

        } catch (Exception e) {
            log.error("Error retrieving cart: {}", e.getMessage(), e);
            ErrorDTO error = new ErrorDTO("INTERNAL_ERROR", "An error occurred while retrieving cart");
            return new Response<>(null, null, 500, false, error);
        }
    }
    

    private boolean isEqual(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }
    

    private boolean areAttributesEqual(Map<String, String> map1, Map<String, String> map2) {
        if (map1 == null && map2 == null) {
            return true;
        }
        if (map1 == null || map2 == null) {
            return false;
        }
        if (map1.size() != map2.size()) {
            return false;
        }
        return map1.entrySet().stream()
                .allMatch(entry -> isEqual(entry.getValue(), map2.get(entry.getKey())));
    }

    @Override
    @CacheEvict(value = "cart", key = "#memberId")
    public Response<Object> updateCartItem(Long memberId, String sku, CartUpdateRequestDTO request) {
        try {

            Optional<Cart> cartOptional = cartRepository.findById(memberId);
            if (cartOptional.isEmpty()) {
                ErrorDTO error = new ErrorDTO("CART_NOT_FOUND", "Cart not found");
                return new Response<>(null, null, 404, false, error);
            }

            Cart cart = cartOptional.get();
            List<CartItem> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();


            Optional<CartItem> itemOptional = items.stream()
                    .filter(item -> item.getSku().equals(sku))
                    .findFirst();

            if (itemOptional.isEmpty()) {
                ErrorDTO error = new ErrorDTO("ITEM_NOT_FOUND", "Item not found in cart");
                return new Response<>(null, null, 404, false, error);
            }


            ProductDTO product;
            try {
                product = productServiceClient.getProductBySku(sku);
                if (product == null) {
                    ErrorDTO error = new ErrorDTO("PRODUCT_NOT_FOUND", "Product not found");
                    return new Response<>(null, null, 404, false, error);
                }
            } catch (FeignException.NotFound e) {
                // Product not found (404) - return error
                log.error("Product not found (404) for SKU: {} during update", sku);
                ErrorDTO error = new ErrorDTO("PRODUCT_NOT_FOUND", "Product not found");
                return new Response<>(null, null, 404, false, error);
            } catch (Exception e) {
                log.error("Error verifying product for SKU: {} - {}", sku, e.getMessage());
                ErrorDTO error = new ErrorDTO("PRODUCT_SERVICE_ERROR", "Error verifying product details");
                return new Response<>(null, null, 500, false, error);
            }
            

            CartItem item = itemOptional.get();
            item.setQuantity(request.getQuantity());
            item.setStatus("active");
            

            if (!isEqual(item.getName(), product.getName())) {
                item.setName(product.getName());
            }
            if (item.getPrice() != product.getPrice()) {
                item.setPrice(product.getPrice());
            }
            if (!isEqual(item.getProductImage(), product.getProductImage())) {
                item.setProductImage(product.getProductImage());
            }
            if (!areAttributesEqual(item.getAttributes(), product.getAttributes())) {
                item.setAttributes(product.getAttributes());
            }


            updateCartSummary(cart);


            cartRepository.save(cart);

            return new Response<>(cart, "Item updated successfully", 200, true, null);

        } catch (Exception e) {
            log.error("Error updating cart item: {}", e.getMessage(), e);
            ErrorDTO error = new ErrorDTO("INTERNAL_ERROR", "An error occurred while updating cart item");
            return new Response<>(null, null, 500, false, error);
        }
    }

    @Override
    @CacheEvict(value = "cart", key = "#memberId")
    public Response<Object> deleteCartItem(Long memberId, String sku) {
        try {
            // Get cart
            Optional<Cart> cartOptional = cartRepository.findById(memberId);
            if (cartOptional.isEmpty()) {
                ErrorDTO error = new ErrorDTO("CART_NOT_FOUND", "Cart not found");
                return new Response<>(null, null, 404, false, error);
            }

            Cart cart = cartOptional.get();
            List<CartItem> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();

            // Remove the item
            boolean removed = items.removeIf(item -> item.getSku().equals(sku));

            if (!removed) {
                ErrorDTO error = new ErrorDTO("ITEM_NOT_FOUND", "Item not found in cart");
                return new Response<>(null, null, 404, false, error);
            }

            cart.setItems(items);


            updateCartSummary(cart);

            // Save cart
            cartRepository.save(cart);

            return new Response<>(cart, "Item removed from cart", 200, true, null);

        } catch (Exception e) {
            log.error("Error deleting cart item: {}", e.getMessage(), e);
            ErrorDTO error = new ErrorDTO("INTERNAL_ERROR", "An error occurred while deleting cart item");
            return new Response<>(null, null, 500, false, error);
        }
    }


    private Cart createEmptyCart(Long memberId) {
        Cart newCart = new Cart();
        newCart.setMemberId(memberId);
        newCart.setItems(new ArrayList<>());
        newCart.setStatus("ACTIVE");
        CartSummary summary = new CartSummary();
        summary.setItemCount(0);
        summary.setTotalQuantity(0);
        summary.setSubtotal(0);
        summary.setTax(0);
        summary.setShipping(0);
        summary.setDiscount(0);
        summary.setTotal(0);
        newCart.setSummary(summary);
        return newCart;
    }


    private void updateCartSummary(Cart cart) {
        List<CartItem> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();
        CartSummary summary = cart.getSummary() != null ? cart.getSummary() : new CartSummary();
        
        summary.setItemCount(items.size());
        summary.setTotalQuantity(items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum());
        summary.setSubtotal(items.stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum());
        summary.setTotal(summary.getSubtotal() + summary.getTax() + summary.getShipping() - summary.getDiscount());
        
        cart.setSummary(summary);
    }
}
