package com.example.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.entity.DeadLetter;

public interface DeadLetterRepository extends JpaRepository<DeadLetter,Long>{

}
