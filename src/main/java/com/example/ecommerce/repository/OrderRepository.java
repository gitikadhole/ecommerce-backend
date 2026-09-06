package com.example.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.entity.Order;

public interface OrderRepository extends JpaRepository<Order,Long> {
	Page<Order> findByUserId(Long userId,Pageable pageable);
	Optional<Order> findById(Long id);
}
