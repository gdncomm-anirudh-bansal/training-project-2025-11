package com.Project.Cart.Client;

import com.Project.Cart.DTO.ProductDTO;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface ProductServiceClient {

    @RequestLine("GET /api/search/get/detailById?skuId={skuId}")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    ProductDTO getProductBySku(@Param("skuId") String skuId);
}

