package com.example.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_shouldSaveProductAndCreateInventory() {
        Category category = new Category();
        category.setId(7L);
        category.setName("Electronics");

        ProductRequest request = new ProductRequest(
                "Laptop",
                "Gaming Laptop",
                999.99,
                10,
                "https://example.com/laptop.jpg",
                7L);

        when(categoryRepository.findById(7L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(11L);
            return product;
        });
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.createProduct(request);

        assertEquals(11L, response.id());
        assertEquals("Laptop", response.name());
        assertEquals("Electronics", response.categoryName());
        assertEquals(999.99, response.price());

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());

        Inventory savedInventory = inventoryCaptor.getValue();
        assertNotNull(savedInventory.getProduct());
        assertEquals(10, savedInventory.getAvailableStock());
        assertEquals(0, savedInventory.getReservedStock());
    }

    @Test
    void deleteProduct_shouldDeleteInventoryBeforeRemovingProduct() {
        Product product = new Product();
        product.setId(33L);
        product.setName("Wireless Mouse");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableStock(5);
        inventory.setReservedStock(1);

        when(productRepository.findById(33L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(33L)).thenReturn(Optional.of(inventory));

        productService.deleteProduct(33L);

        verify(inventoryRepository).delete(inventory);
        verify(productRepository).delete(product);
    }
}
