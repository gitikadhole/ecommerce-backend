package com.example.ecommerce.dto;

public record ProductRequest(String name, String description,Double price, Integer stockQuantity, String imageUrl,Long categoryId) {

}
