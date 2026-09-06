package com.example.ecommerce.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderController orderController;

    @Test
    void checkout_shouldReturnOrderResponse() {
        User user = new User();
        user.setId(9L);
        user.setEmail("customer@example.com");

        CheckoutRequest request = new CheckoutRequest();
        request.setFullName("Jane Doe");
        request.setPhone("9999999999");
        request.setStreet("Main");
        request.setCity("Bengaluru");
        request.setState("KA");
        request.setZipCode("560001");
        request.setCountry("India");

        OrderResponse expected = new OrderResponse();
        expected.setId(11L);
        expected.setUserId(9L);
        expected.setTotalAmount(280.0);
        expected.setStatus(OrderStatus.PENDING);

        Authentication authentication = new TestingAuthenticationToken("customer@example.com", null, "ROLE_USER");

        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(orderService.checkout(9L, request)).thenReturn(expected);

        ResponseEntity<OrderResponse> response = orderController.checkout(authentication, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(280.0, response.getBody().getTotalAmount());
    }

    @Test
    void myOrders_shouldReturnUserOrdersPage() {
        User user = new User();
        user.setId(9L);
        user.setEmail("customer@example.com");

        OrderResponse expected = new OrderResponse();
        expected.setId(11L);
        expected.setUserId(9L);
        expected.setTotalAmount(280.0);
        expected.setStatus(OrderStatus.PENDING);

        Authentication authentication = new TestingAuthenticationToken("customer@example.com", null, "ROLE_USER");

        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(orderService.getUserOrders(9L, 0, 10)).thenReturn(new PageImpl<>(List.of(expected)));

        Page<OrderResponse> response = orderController.myOrders(authentication, 0, 10);

        assertEquals(1, response.getTotalElements());
        assertEquals(11L, response.getContent().get(0).getId());
    }

    @Test
    void getDetail_shouldReturnOrderDetailForUser() {
        User user = new User();
        user.setId(9L);
        user.setEmail("customer@example.com");

        OrderResponse expected = new OrderResponse();
        expected.setId(11L);
        expected.setUserId(9L);
        expected.setTotalAmount(280.0);
        expected.setStatus(OrderStatus.PENDING);

        Authentication authentication = new TestingAuthenticationToken("customer@example.com", null, "ROLE_USER");

        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(orderService.getOrderDetail(11L, 9L, false)).thenReturn(expected);

        ResponseEntity<OrderResponse> response = orderController.getDetail(11L, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(11L, response.getBody().getId());
    }

    @Test
    void allOrders_shouldReturnAllOrdersForAdmin() {
        OrderResponse expected = new OrderResponse();
        expected.setId(21L);
        expected.setUserId(99L);
        expected.setTotalAmount(500.0);
        expected.setStatus(OrderStatus.CONFIRMED);

        when(orderService.getAllOrders(0, 10)).thenReturn(new PageImpl<>(List.of(expected)));

        Page<OrderResponse> response = orderController.allOrders(0, 10);

        assertEquals(1, response.getTotalElements());
        assertEquals(21L, response.getContent().get(0).getId());
    }

    @Test
    void updateStatus_shouldReturnUpdatedOrder() {
        OrderResponse expected = new OrderResponse();
        expected.setId(11L);
        expected.setStatus(OrderStatus.CONFIRMED);

        when(orderService.updateStatus(11L, OrderStatus.CONFIRMED)).thenReturn(expected);

        ResponseEntity<OrderResponse> response = orderController.updateStatus(11L, OrderStatus.CONFIRMED);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(OrderStatus.CONFIRMED, response.getBody().getStatus());
    }
}
