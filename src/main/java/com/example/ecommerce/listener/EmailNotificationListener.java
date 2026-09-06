package com.example.ecommerce.listener;
import com.example.ecommerce.entity.DeadLetter;
import com.example.ecommerce.events.OrderEvent;
import com.example.ecommerce.repository.DeadLetterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationListener {

    @Autowired private DeadLetterRepository deadLetterRepo;

    @Async
    @EventListener
    @Retryable(value = {RuntimeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void handleOrderPlaced(OrderEvent.OrderPlacedEvent event) {
        System.out.println("========================================");
        System.out.println("[MOCK EMAIL] Order Placed: Sending confirmation to " + event.email + " for Order ID " + event.orderId);
        System.out.println("========================================");
        // To test retry, throw exception randomly: if(true) throw new RuntimeException("SMTP fail");
    }

    @Recover
    public void recover(RuntimeException ex, OrderEvent.OrderPlacedEvent event) {
        System.out.println("[DLQ] Email failed after 3 retries for order " + event.orderId);
        deadLetterRepo.save(new DeadLetter(event.orderId, ex.getMessage()));
    }

    @EventListener
    public void handleShipped(OrderEvent.OrderShippedEvent event) {
        System.out.println("[MOCK EMAIL] Order Shipped: " + event.orderId + " to " + event.email);
    }
}