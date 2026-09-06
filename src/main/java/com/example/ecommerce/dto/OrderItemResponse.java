package com.example.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class OrderItemResponse {
	private Long productId;
	private String productName;
	private Double priceAtPurchase;
	private Integer quantity;
	private Double subtotal;
}
