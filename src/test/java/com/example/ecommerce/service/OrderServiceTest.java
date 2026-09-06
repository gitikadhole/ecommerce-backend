package com.example.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.events.OrderEvent;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Test
    void checkout_shouldCreateOrderAndPublishOrderPlacedEvent() {
        User user = new User();
        user.setId(9L);
        user.setEmail("customer@example.com");

        Product product = new Product();
        product.setId(5L);
        product.setName("Monitor");
        product.setPrice(150.0);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>(List.of(item)));

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableStock(20);
        inventory.setReservedStock(2);

        CheckoutRequest request = new CheckoutRequest();
        request.setFullName("Customer Name");
        request.setPhone("1234567890");
        request.setStreet("Main Street");
        request.setCity("New York");
        request.setState("NY");
        request.setZipCode("10001");
        request.setCountry("USA");

        when(userRepository.findById(9L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(9L)).thenReturn(Optional.of(cart));
        when(inventoryRepository.findByProductIdForUpdate(5L)).thenReturn(Optional.of(inventory));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(77L);
            return saved;
        });
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.checkout(9L, request);

        assertNotNull(response);
        assertEquals(300.0, response.getTotalAmount());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        verify(eventPublisher).publishEvent(any(OrderEvent.OrderPlacedEvent.class));
    }

    @Test
    void updateStatus_shouldTransitionOrderToConfirmed() {
        User user = new User();
        user.setId(5L);

        Order order = new Order();
        order.setId(12L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(new ArrayList<>());
        order.setTotalAmount(99.0);

        when(orderRepository.findById(12L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderResponse response = orderService.updateStatus(12L, OrderStatus.CONFIRMED);

        assertEquals(12L, response.getId());
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
    }
}
