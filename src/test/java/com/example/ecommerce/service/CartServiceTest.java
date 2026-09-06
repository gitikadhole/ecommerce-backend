package com.example.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addItem_shouldReserveInventoryAndReturnUpdatedCart() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");

        Product product = new Product();
        product.setId(11L);
        product.setName("Headphones");
        product.setPrice(200.0);
        product.setImageUrl("https://example.com/headphones.jpg");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableStock(5);
        inventory.setReservedStock(0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(inventoryRepository.findByProductIdForUpdate(11L)).thenReturn(Optional.of(inventory));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(productRepository.findById(11L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setId(22L);
            return cart;
        });
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartService.addItem(1L, 11L, 2);

        assertNotNull(response);
        assertEquals(22L, response.id());
        assertEquals(1L, response.userId());
        assertEquals(400.0, response.totalPrice());
        assertEquals(1, response.items().size());
        assertEquals(2, response.items().get(0).quantity());
        assertEquals(3, inventory.getAvailableStock());
        assertEquals(2, inventory.getReservedStock());
    }

    @Test
    void removeItem_shouldRestoreReservedStock() {
        User user = new User();
        user.setId(3L);

        Product product = new Product();
        product.setId(88L);
        product.setName("Keyboard");
        product.setPrice(99.99);
        product.setImageUrl("https://example.com/keyboard.jpg");

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        CartItem item = new CartItem();
        item.setId(50L);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(3);
        cart.getItems().add(item);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableStock(2);
        inventory.setReservedStock(3);

        when(cartRepository.findByUserId(3L)).thenReturn(Optional.of(cart));
        when(inventoryRepository.findByProductIdForUpdate(88L)).thenReturn(Optional.of(inventory));

        cartService.removeItem(3L, 50L);

        assertEquals(5, inventory.getAvailableStock());
        assertEquals(0, inventory.getReservedStock());
        verify(cartItemRepository).delete(item);
    }
}
