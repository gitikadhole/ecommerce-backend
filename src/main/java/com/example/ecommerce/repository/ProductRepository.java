package com.example.ecommerce.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    
	@Query(value = "SELECT p FROM Product p WHERE " +
	           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
	           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
	           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
	           "(:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))",
	       countQuery = "SELECT COUNT(p) FROM Product p WHERE " +
	           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
	           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
	           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
	           "(:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
	    Page<Product> searchAndFilter(@Param("categoryId") Long categoryId,
	                                  @Param("minPrice") Double minPrice,
	                                  @Param("maxPrice") Double maxPrice,
	                                  @Param("keyword") String keyword,
	                                  Pageable pageable);
}