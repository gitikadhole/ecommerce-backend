package com.example.ecommerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;

@Entity
public class Inventory {
	
	@Id
	private Long productId;
	
	@OneToOne
	@MapsId
	@JoinColumn(name = "product_id")
	private Product product;
	
	private Integer availableStock;
	private Integer reservedStock;
	
	@Version
	private Long version;

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Integer getAvailableStock() {
		return availableStock;
	}

	public void setAvailableStock(Integer availableStock) {
		this.availableStock = availableStock;
	}

	public Integer getReservedStock() {
		return reservedStock;
	}

	public void setReservedStock(Integer reservedStock) {
		this.reservedStock = reservedStock;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
	
	
}
