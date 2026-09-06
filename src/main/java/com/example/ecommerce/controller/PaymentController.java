package com.example.ecommerce.controller;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.events.OrderEvent;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired private PaymentRepository paymentRepo;
    @Autowired private OrderService orderService;
    @Autowired private ApplicationEventPublisher publisher;

    // Step 1: Create intent after checkout
    @PostMapping("/create-intent")
    public Map<String, Object> createIntent(@RequestParam Long orderId) {
        Order order = orderService.getOrderById(orderId);
        String razorpayOrderId = "order_mock_" + System.currentTimeMillis();

        Payment p = new Payment();
        p.setOrderId(orderId);
        p.setPaymentIntentId(razorpayOrderId);
        p.setAmount(order.getTotalAmount());
        p.setStatus(PaymentStatus.CREATED);
        paymentRepo.save(p);

        Map<String, Object> res = new HashMap<>();
        res.put("razorpayOrderId", razorpayOrderId);
        res.put("amount", order.getTotalAmount());
        res.put("key", "rzp_test_mock_key"); // put real key here if you have
        return res;
    }

    // Step 2: Webhook - Razorpay calls this
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody Map<String, Object> payload) {
        String razorpayOrderId = (String) payload.get("razorpay_order_id");
        String status = (String) payload.get("status"); // captured / failed

        Payment payment = paymentRepo.findByPaymentIntentId(razorpayOrderId);
        if (payment == null) return ResponseEntity.badRequest().body("Payment not found");

        if ("captured".equals(status)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            orderService.updateStatus(payment.getOrderId(), OrderStatus.CONFIRMED);
            Order order = orderService.getOrderById(payment.getOrderId());
            // Publish event -> email
            publisher.publishEvent(new OrderEvent.OrderPlacedEvent(order.getId(), order.getUser().getEmail()));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            orderService.updateStatus(payment.getOrderId(), OrderStatus.CANCELLED);
        }
        paymentRepo.save(payment);
        return ResponseEntity.ok("Webhook processed");
    }
}