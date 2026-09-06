//package com.example.ecommerce.controller;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import java.util.List;
//import java.util.Optional;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.userdetails.User;
//
//import com.example.ecommerce.dto.CartItemResponse;
//import com.example.ecommerce.dto.CartResponse;
//import com.example.ecommerce.entity.User as EntityUser;
//import com.example.ecommerce.repository.UserRepository;
//import com.example.ecommerce.service.CartService;
//
//@ExtendWith(MockitoExtension.class)
//class CartControllerTest {
//
//    @Mock
//    private CartService cartService;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private CartController cartController;
//
//    @Test
//    void addItem_shouldReturnUpdatedCart() {
//        User principal = new User("alice@example.com", "pass", List.of());
//        EntityUser user = new EntityUser();
//        user.setId(7L);
//        user.setEmail("alice@example.com");
//
//        CartResponse expected = new CartResponse(1L, 7L, List.of(new CartItemResponse(1L, 10L, "Mouse", 25.0, 2, "img.png")), 50.0);
//
//        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
//        when(cartService.addItem(7L, 10L, 2)).thenReturn(expected);
//
//        ResponseEntity<CartResponse> response = cartController.addItem(principal, 10L, 2);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertEquals(50.0, response.getBody().totalPrice());
//        verify(cartService).addItem(7L, 10L, 2);
//    }
//
//    @Test
//    void updateItemQuantity_shouldReturnUpdatedCart() {
//        User principal = new User("alice@example.com", "pass", List.of());
//        EntityUser user = new EntityUser();
//        user.setId(7L);
//        user.setEmail("alice@example.com");
//
//        CartResponse expected = new CartResponse(1L, 7L, List.of(), 0.0);
//
//        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
//        when(cartService.updateItemQuantity(7L, 5L, 3)).thenReturn(expected);
//
//        ResponseEntity<CartResponse> response = cartController.updateItemQuantity(principal, 5L, 3);
//
//        assertEquals(200, response.getStatusCode().value());
//        verify(cartService).updateItemQuantity(7L, 5L, 3);
//    }
//
//    @Test
//    void removeItem_shouldReturnNoContent() {
//        User principal = new User("alice@example.com", "pass", List.of());
//        EntityUser user = new EntityUser();
//        user.setId(7L);
//        user.setEmail("alice@example.com");
//
//        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
//
//        ResponseEntity<Void> response = cartController.removeItem(principal, 5L);
//
//        assertEquals(204, response.getStatusCode().value());
//        verify(cartService).removeItem(7L, 5L);
//    }
//
//    @Test
//    void getCart_shouldReturnUserCart() {
//        User principal = new User("alice@example.com", "pass", List.of());
//        EntityUser user = new EntityUser();
//        user.setId(7L);
//        user.setEmail("alice@example.com");
//
//        CartResponse expected = new CartResponse(1L, 7L, List.of(), 0.0);
//
//        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
//        when(cartService.getCart(7L)).thenReturn(expected);
//
//        ResponseEntity<CartResponse> response = cartController.getCart(principal);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertEquals(7L, response.getBody().userId());
//        verify(cartService).getCart(7L);
//    }
//
//    @Test
//    void clearCart_shouldReturnNoContent() {
//        User principal = new User("alice@example.com", "pass", List.of());
//        EntityUser user = new EntityUser();
//        user.setId(7L);
//        user.setEmail("alice@example.com");
//
//        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
//
//        ResponseEntity<Void> response = cartController.clearCart(principal);
//
//        assertEquals(204, response.getStatusCode().value());
//        verify(cartService).clearCart(7L);
//    }
//}
