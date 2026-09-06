package com.example.ecommerce.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ecommerce.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class OrderResponse {
	private Long id;
	private Long userId;
	private List<OrderItemResponse> items;
	private Double totalAmount;
	private OrderStatus status;
	private LocalDateTime createdAt;
	private AddressResponse shippingAddress;
}
