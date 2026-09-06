package com.example.ecommerce.service;

import com.example.ecommerce.dto.*;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.events.OrderEvent;
import com.example.ecommerce.repository.*;
import jakarta.transaction.Transactional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private ApplicationEventPublisher eventPublisher;

    

    public OrderService(CartRepository cartRepository, OrderRepository orderRepository,
			InventoryRepository inventoryRepository, UserRepository userRepository,
			ApplicationEventPublisher eventPublisher) {
		//super();
		this.cartRepository = cartRepository;
		this.orderRepository = orderRepository;
		this.inventoryRepository = inventoryRepository;
		this.userRepository = userRepository;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Cart is empty"));
        if (cart.getItems().isEmpty()) throw new RuntimeException("Cannot checkout empty cart");

        Address address = new Address();
        address.setFullName(req.getFullName());
        address.setPhone(req.getPhone());
        address.setStreet(req.getStreet());
        address.setCity(req.getCity());
        address.setState(req.getState());
        address.setZipcode(req.getZipCode());
        address.setCountry(req.getCountry());
        address.setUser(user);

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(address);

        double total = 0;
        for (CartItem cartItem : cart.getItems()) {
            Inventory inv = inventoryRepository.findByProductIdForUpdate(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Inventory not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setProductName(cartItem.getProduct().getName());
            orderItem.setPriceAtPurchase(cartItem.getProduct().getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(cartItem.getProduct().getPrice() * cartItem.getQuantity());
            order.getItems().add(orderItem);
            total += orderItem.getSubtotal();

            inv.setReservedStock(inv.getReservedStock() - cartItem.getQuantity());
            inventoryRepository.save(inv);
        }
        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        
        eventPublisher.publishEvent(new OrderEvent.OrderPlacedEvent(saved.getId(), saved.getUser().getEmail()));
        
        cart.getItems().clear();
        cartRepository.save(cart);
        return mapToResponse(saved);
    }

    public Page<OrderResponse> getUserOrders(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByUserId(userId, pageable).map(this::mapToResponse);
    }
    public Page<OrderResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findAll(pageable).map(this::mapToResponse);
    }
    public OrderResponse getOrderDetail(Long orderId, Long userId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if (!isAdmin && !order.getUser().getId().equals(userId)) throw new RuntimeException("Access denied");
        return mapToResponse(order);
    }
    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.changeStatus(newStatus);
        return mapToResponse(orderRepository.save(order));
    }
    private OrderResponse mapToResponse(Order order) {
        OrderResponse res = new OrderResponse();
        res.setId(order.getId());
        res.setUserId(order.getUser().getId());
        res.setTotalAmount(order.getTotalAmount());
        res.setStatus(order.getStatus());
        res.setCreatedAt(order.getCreatedAt());
        List<OrderItemResponse> items = new ArrayList<>();
        for(OrderItem i : order.getItems()) {
            OrderItemResponse ir = new OrderItemResponse();
            ir.setProductId(i.getProduct().getId());
            ir.setProductName(i.getProductName());
            ir.setPriceAtPurchase(i.getPriceAtPurchase());
            ir.setQuantity(i.getQuantity());
            ir.setSubtotal(i.getSubtotal());
            items.add(ir);
        }
        res.setItems(items);
        if(order.getShippingAddress()!=null) {
            AddressResponse ar = new AddressResponse();
            ar.setFullName(order.getShippingAddress().getFullName());
            ar.setPhone(order.getShippingAddress().getPhone());
            ar.setStreet(order.getShippingAddress().getStreet());
            ar.setCity(order.getShippingAddress().getCity());
            ar.setState(order.getShippingAddress().getState());
            ar.setZipCode(order.getShippingAddress().getZipcode());
            ar.setCountry(order.getShippingAddress().getCountry());
            res.setShippingAddress(ar);
        }
        return res;
    }
    
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

  
}