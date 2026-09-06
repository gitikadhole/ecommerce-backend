package com.example.ecommerce.controller;

import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    // POST /api/orders/checkout
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(Authentication authentication, @RequestBody CheckoutRequest req) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        OrderResponse response = orderService.checkout(user.getId(), req);
        return ResponseEntity.ok(response);
    }

    // GET /api/orders/my?page=0&size=10
    @GetMapping("/my")
    public Page<OrderResponse> myOrders(Authentication authentication, 
                                       @RequestParam(defaultValue = "0") int page, 
                                       @RequestParam(defaultValue = "10") int size) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return orderService.getUserOrders(user.getId(), page, size);
    }

    // GET /api/orders/{id}
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getDetail(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        return ResponseEntity.ok(orderService.getOrderDetail(id, user.getId(), isAdmin));
    }

    // GET /api/orders - ADMIN only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderResponse> allOrders(@RequestParam(defaultValue = "0") int page, 
                                         @RequestParam(defaultValue = "10") int size) {
        return orderService.getAllOrders(page, size);
    }

    // PUT /api/orders/{id}/status?status=CONFIRMED - ADMIN only
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }
}