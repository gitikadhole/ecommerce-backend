package com.example.ecommerce.dto;

public record ProductResponse(Long id, String name, String description, Double price, Integer stockQuantity, String imageUrl, String categoryName,Long categoryId,Boolean active) {

}
