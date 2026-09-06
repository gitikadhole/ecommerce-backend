package com.example.ecommerce.entity;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
	PENDING,CONFIRMED,SHIPPED,DELIVERED,CANCELLED;
	
	private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
			PENDING, Set.of(CONFIRMED,CANCELLED),
			CONFIRMED, Set.of(SHIPPED,CANCELLED),
			SHIPPED, Set.of(DELIVERED),
			DELIVERED, Set.of(),
			CANCELLED, Set.of()
			
			);
	
	public boolean canTransitionTo(OrderStatus newStatus) {
		return VALID_TRANSITIONS.getOrDefault(this, Set.of()).contains(newStatus);
	}
}
