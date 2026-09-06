package com.example.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;

public interface CategoryRepository extends JpaRepository<Category, Long>{

}
