package com.project.Search.Controller;

import com.project.Search.DTO.CategoryDTO;
import com.project.Search.DTO.ProductDetailDTO;
import com.project.Search.DTO.Response;
import com.project.Search.DTO.SearchRequestDTO;
import com.project.Search.DTO.SearchResultDTO;
import com.project.Search.Service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    SearchService searchService;


    @PostMapping("/product")
    public ResponseEntity<Response<SearchResultDTO>> searchProduct(@RequestBody SearchRequestDTO searchRequestDTO) {
        if (searchRequestDTO.getQuery() == null || searchRequestDTO.getQuery().trim().isEmpty()) {
            Response<SearchResultDTO> errorResponse = new Response<>();
            errorResponse.setMessage("Query is mandatory and cannot be empty");
            errorResponse.setCode(400);
            errorResponse.setSuccess(false);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        try {
            Response<SearchResultDTO> response = searchService.getProductSearch(searchRequestDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Response<SearchResultDTO> errorResponse = new Response<>();
            errorResponse.setMessage("An error occurred while searching products: " + e.getMessage());
            errorResponse.setCode(500);
            errorResponse.setSuccess(false);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("get/detailById")
    public ResponseEntity<?> getProductDetail(@RequestParam String skuId)
    {

        if(skuId==null || skuId.isEmpty()) {
            Response<ProductDetailDTO> errorResponse = new Response<>();
            errorResponse.setMessage("SKU ID is required and cannot be empty");
            errorResponse.setCode(400);
            errorResponse.setSuccess(false);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        ProductDetailDTO productDetailDTO = searchService.getProductDetail(skuId);

        if(productDetailDTO == null) {
            Response<ProductDetailDTO> errorResponse = new Response<>();
            errorResponse.setMessage("Product not found for SKU: " + skuId);
            errorResponse.setCode(404);
            errorResponse.setSuccess(false);
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(productDetailDTO, HttpStatus.OK);
    }

    @GetMapping("get/CategoryId")
    public ResponseEntity<CategoryDTO> getCategoryDetail(@RequestParam String categoryId)
    {

        if(categoryId==null || categoryId.isEmpty())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        CategoryDTO categoryDTO =searchService.getCategoryDetail(categoryId);

        return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
    }

    @GetMapping("/products")
    public ResponseEntity<Response<SearchResultDTO>> listAllProducts(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "asc") String order) {

        try {
            Response<SearchResultDTO> response = searchService.listAllProducts(page, size, sort, order);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Response<SearchResultDTO> errorResponse = new Response<>();
            errorResponse.setMessage("An error occurred while listing products: " + e.getMessage());
            errorResponse.setCode(500);
            errorResponse.setSuccess(false);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
