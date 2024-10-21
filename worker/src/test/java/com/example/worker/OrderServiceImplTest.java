package com.example.worker;

import com.example.worker.model.Customer;
import com.example.worker.model.Order;
import com.example.worker.model.Product;
import com.example.worker.repository.OrderRepository;
import com.example.worker.service.EnrichmentService;
import com.example.worker.service.RetryService;
import com.example.worker.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private EnrichmentService enrichmentService;

    @Mock
    private RetryService retryService;

    @Mock
    private OrderRepository orderRepository;

    private Order order;
    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup mock data
        order = new Order();
        order.setOrderId("order-001");
        order.setCustomerId("customer-001");

        customer = new Customer();
        customer.setCustomerId("customer-001");
        customer.setStatus("active");

        product = new Product();
        product.setProductId("product-001");
        product.setName("Laptop");
    }

    @Test
    void testProcessOrderSuccessfully() {
        // Mocking lock acquisition, enrichment services, and order repository
        when(retryService.acquireLock(anyString())).thenReturn(Mono.just(true));
        when(enrichmentService.enrichCustomer(anyString())).thenReturn(Mono.just(customer));
        when(enrichmentService.enrichProduct(anyString())).thenReturn(Mono.just(product));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.empty());
        when(retryService.releaseLock(anyString())).thenReturn(Mono.empty());

        // Call the method under test
        orderService.processOrder(order);  // No need to capture return value

        // Verify that the interactions happened as expected
        verify(retryService).acquireLock(order.getOrderId());
        verify(enrichmentService).enrichCustomer(order.getCustomerId());
        verify(enrichmentService).enrichProduct(anyString());
        verify(orderRepository).save(order);
        verify(retryService).releaseLock(order.getOrderId());
    }

    @Test
    void testProcessOrderWithInactiveCustomer() {
        // Mocking customer as inactive
        customer.setStatus("inactive");

        when(retryService.acquireLock(anyString())).thenReturn(Mono.just(true));
        when(enrichmentService.enrichCustomer(anyString())).thenReturn(Mono.just(customer));

        // Call the method under test
        orderService.processOrder(order);

        // Verify that an error was thrown and no order was saved
        verify(enrichmentService).enrichCustomer(order.getCustomerId());
        verifyNoInteractions(orderRepository);  // Should not save the order
        verify(retryService).releaseLock(order.getOrderId());
    }

    @Test
    void testProcessOrderWithLockFailure() {
        // Simulate lock failure
        when(retryService.acquireLock(anyString())).thenReturn(Mono.just(false));

        // Call the method under test
        orderService.processOrder(order);

        // Verify that the process did not proceed due to lock failure
        verify(retryService).acquireLock(order.getOrderId());
        verifyNoInteractions(enrichmentService);
        verifyNoInteractions(orderRepository);
    }
}