package com.example.ecommerce.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.OutOfStockException;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponse addItem(Long userId, Long productId, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Lock inventory row - prevents race
        Inventory inventory = inventoryRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product " + productId + " - create product again"));

        // 2. Check available stock
        if (inventory.getAvailableStock() < quantity) {
            throw new RuntimeException("Only " + inventory.getAvailableStock() + " items left for " + inventory.getProduct().getName());
        }

        // 3. Get or create cart - SAVE IMMEDIATELY if new
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart); // SAVE HERE so id is not null
        });

        // 4. Check if product already in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        // 5. Reserve stock
        inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
        inventory.setReservedStock(inventory.getReservedStock() + quantity);
        inventoryRepository.save(inventory);

        Cart savedCart = cartRepository.save(cart);
        return mapToResponse(savedCart);
    }

    @Transactional
    public CartResponse updateItemQuantity(Long userId, Long itemId, int newQuantity) {
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        // Lock inventory
        Inventory inventory = inventoryRepository.findByProductIdForUpdate(item.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        int diff = newQuantity - item.getQuantity(); // +ve means need more stock

        if (diff > 0) {
            if (inventory.getAvailableStock() < diff) {
                throw new OutOfStockException("Only " + inventory.getAvailableStock() + " more available");
            }
            inventory.setAvailableStock(inventory.getAvailableStock() - diff);
            inventory.setReservedStock(inventory.getReservedStock() + diff);
        } else if (diff < 0) {
            // Returning stock
            inventory.setAvailableStock(inventory.getAvailableStock() + (-diff));
            inventory.setReservedStock(inventory.getReservedStock() - (-diff));
        }

        item.setQuantity(newQuantity);
        inventoryRepository.save(inventory);
        cartItemRepository.save(item);

        return mapToResponse(cart);
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // Restore stock
        Inventory inventory = inventoryRepository.findByProductIdForUpdate(item.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inventory.setAvailableStock(inventory.getAvailableStock() + item.getQuantity());
        inventory.setReservedStock(inventory.getReservedStock() - item.getQuantity());
        inventoryRepository.save(inventory);

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(userRepository.findById(userId).orElse(null));
                    return newCart;
                });
        return mapToResponse(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) return;

        // Restore all stock
        for (CartItem item : cart.getItems()) {
            Inventory inv = inventoryRepository.findByProductIdForUpdate(item.getProduct().getId()).orElse(null);
            if (inv != null) {
                inv.setAvailableStock(inv.getAvailableStock() + item.getQuantity());
                inv.setReservedStock(inv.getReservedStock() - item.getQuantity());
                inventoryRepository.save(inv);
            }
        }
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private CartResponse mapToResponse(Cart cart) {
        // REMOVE the if (cart.getId() == null) check - that was causing empty response

        List<CartItemResponse> itemDtos = cart.getItems().stream().map(item -> 
            new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                item.getProduct().getImageUrl()
            )
        ).collect(Collectors.toList());

        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();

        Long userId = cart.getUser() != null ? cart.getUser().getId() : null;

        return new CartResponse(cart.getId(), userId, itemDtos, total);
    }
}