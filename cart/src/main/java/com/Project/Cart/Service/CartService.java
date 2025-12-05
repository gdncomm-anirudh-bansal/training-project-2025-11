package com.Project.Cart.Service;

import com.Project.Cart.DTO.CartAddRequestDTO;
import com.Project.Cart.DTO.CartUpdateRequestDTO;
import com.Project.Cart.DTO.Response;

public interface CartService {
    Response<Object> addItemToCart(Long memberId, CartAddRequestDTO request);
    Response<Object> getCart(Long memberId);
    Response<Object> updateCartItem(Long memberId, String sku, CartUpdateRequestDTO request);
    Response<Object> deleteCartItem(Long memberId, String sku);
}
