package com.example.ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductService {
	
	@Autowired ProductRepository productRepository;
	@Autowired CategoryRepository categoryRepository;
	@Autowired InventoryRepository inventoryRepository;
	
	private ProductResponse mapToResponse(Product p) {
		return new ProductResponse(
			p.getId(),
			p.getName(),
			p.getDescription(),
			p.getPrice(),
			p.getStockQuantity(),
			p.getImageUrl(),
			p.getCategory().getName(),
			p.getCategory().getId(),
			p.getActive()
		);
	}
	
	// FIXED KEY: includes all filters
	@Cacheable(value="products", key="#categoryId + '-' + #minPrice + '-' + #maxPrice + '-' + #keyword + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
	public Page<ProductResponse> getAllProducts(Pageable pageable, Long categoryId, Double minPrice, Double maxPrice, String keyword){
		return productRepository.searchAndFilter(categoryId, minPrice, maxPrice, keyword, pageable)
				.map(this::mapToResponse);
	}
	
	@Transactional
	@CacheEvict(value = "products", allEntries=true)
	public ProductResponse createProduct(ProductRequest req) {
		Category cat = categoryRepository.findById(req.categoryId())
				.orElseThrow(()-> new RuntimeException("Category not found"));
		
		Product p = new Product();
		p.setName(req.name());
		p.setDescription(req.description());
		p.setPrice(req.price());
		p.setStockQuantity(req.stockQuantity());
		p.setImageUrl(req.imageUrl());
		p.setCategory(cat);
		p.setActive(true);
		
		Product saved = productRepository.save(p);

		// CREATE INVENTORY FOR CART LOCKING
		Inventory inv = new Inventory();
		inv.setProduct(saved);
		// if your Inventory uses @MapsId, set productId also
		// inv.setProductId(saved.getId());
		inv.setAvailableStock(req.stockQuantity() != null ? req.stockQuantity() : 100);
		inv.setReservedStock(0);
		inventoryRepository.save(inv);
		
		return mapToResponse(saved);
	}
	
	@Transactional
	@CacheEvict(value = "products", allEntries=true)
	public ProductResponse updateProduct(Long id, ProductRequest req) {
		Product product = productRepository.findById(id)
				.orElseThrow(()-> new RuntimeException("Product not found with id "+id));
		
		if(req.categoryId()!=null) {
			Category cat = categoryRepository.findById(req.categoryId())
					.orElseThrow(()-> new RuntimeException("Category not found"));
			product.setCategory(cat);
		}
		
		// UPDATE INVENTORY STOCK IF ADMIN CHANGES STOCK
		if(req.stockQuantity() != null) {
			Inventory inv = inventoryRepository.findByProductId(product.getId())
					.orElse(null);
			if(inv != null) {
				int currentTotal = inv.getAvailableStock() + inv.getReservedStock();
				int diff = req.stockQuantity() - currentTotal;
				inv.setAvailableStock(inv.getAvailableStock() + diff);
				inventoryRepository.save(inv);
			}
		}
		
		product.setName(req.name());
		product.setDescription(req.description());
		product.setPrice(req.price());
		product.setStockQuantity(req.stockQuantity());
		product.setImageUrl(req.imageUrl());
		
		Product updated = productRepository.save(product);
		return mapToResponse(updated);
	}
	
	@Transactional
	@CacheEvict(value = "products", allEntries=true)
	public void deleteProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(()-> new RuntimeException("Product not found with id "+id));
		
		// delete inventory first due to FK
		inventoryRepository.findByProductId(product.getId()).ifPresent(inventoryRepository::delete);
		
		productRepository.delete(product);
	}
}