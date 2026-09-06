package com.example.ecommerce.dto;

public record CartItemResponse(Long id,Long productId,String productName,Double price,Integer quantity,String imageUrl) {

}
