package com.example.ecommerce.events;

public class OrderEvent {
	
	public static class OrderPlacedEvent{
		public Long orderId;
		public String email;
		public OrderPlacedEvent(Long orderId, String email) {
			//super();
			this.orderId = orderId;
			this.email = email;
		}
		public Long getOrderId() {
			return orderId;
		}
		public void setOrderId(Long orderId) {
			this.orderId = orderId;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		
		
	}
	
	public static class OrderShippedEvent{
		public Long orderId;
		public String email;
		public OrderShippedEvent(Long orderId, String email) {
			//super();
			this.orderId = orderId;
			this.email = email;
		}
		public Long getOrderId() {
			return orderId;
		}
		public void setOrderId(Long orderId) {
			this.orderId = orderId;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		
	}
	
	
}
