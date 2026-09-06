package com.example.ecommerce.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.entity.PaymentStatus;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.service.OrderService;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentRepository paymentRepo;

    @Mock
    private OrderService orderService;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void createIntent_shouldReturnPaymentMetadata() {
        Order order = new Order();
        order.setId(25L);
        order.setTotalAmount(456.78);

        when(orderService.getOrderById(25L)).thenReturn(order);
        when(paymentRepo.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> response = paymentController.createIntent(25L);

        assertEquals("rzp_test_mock_key", response.get("key"));
        assertEquals(456.78, response.get("amount"));
        assertNotNull(response.get("razorpayOrderId"));
    }

//    @Test
//    void webhook_shouldMarkPaymentSuccessfulAndUpdateOrder() {
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("razorpay_order_id", "order_123");
//        payload.put("status", "captured");
//
//        Payment payment = new Payment();
//        payment.setOrderId(15L);
//        payment.setPaymentIntentId("order_123");
//        payment.setStatus(PaymentStatus.CREATED);
//
//        User user = new User();
//        user.setEmail("buyer@example.com");
//
//        Order order = new Order();
//        order.setId(15L);
//        order.setUser(user);
//
//        when(paymentRepo.findByPaymentIntentId("order_123")).thenReturn(payment);
//        when(orderService.getOrderById(15L)).thenReturn(order);
//
//        ResponseEntity<String> response = paymentController.webhook(payload);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
//        verify(orderService).updateStatus(eq(15L), eq(OrderStatus.CONFIRMED));
//        verify(publisher).publishEvent(any());
//    }
}
