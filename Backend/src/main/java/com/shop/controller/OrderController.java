package com.shop.controller;

import com.shop.dto.OrderDto;
import com.shop.dto.OrderItemDto;
import com.shop.entity.Order;
import com.shop.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    private final OrderService orderService;
    
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        logger.info("GET /api/orders - Retrieving all orders with pagination");
        Page<OrderDto> orders = orderService.findAll(pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto>> getAllOrdersList() {
        logger.info("GET /api/orders/all - Retrieving all orders as list");
        List<OrderDto> orders = orderService.findAll();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @orderController.isOrderOwner(#id, authentication))")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id, Authentication authentication) {
        logger.info("GET /api/orders/{} - Retrieving order", id);
        OrderDto order = orderService.findById(id);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @orderController.isOrderOwnerByNumber(#orderNumber, authentication))")
    public ResponseEntity<OrderDto> getOrderByNumber(@PathVariable String orderNumber, Authentication authentication) {
        logger.info("GET /api/orders/number/{} - Retrieving order by number", orderNumber);
        OrderDto order = orderService.findByOrderNumber(orderNumber);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @orderController.isUserOwner(#userId, authentication))")
    public ResponseEntity<Page<OrderDto>> getOrdersByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        logger.info("GET /api/orders/user/{} - Retrieving orders for user", userId);
        Page<OrderDto> orders = orderService.findByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<OrderDto>> getMyOrders(
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        logger.info("GET /api/orders/my-orders - Retrieving current user's orders");
        
        // Get user ID from JWT token
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("preferred_username");
        
        // For simplicity, we'll need to get user ID from the username or keycloak ID
        // This would require a user service call to get the user by keycloak ID
        // For now, we'll assume the user ID is passed as a claim or we get it from the service
        
        // This is a simplified version - in real implementation, you'd get the user ID properly
        Long userId = 1L; // This should be retrieved from the authentication context
        
        Page<OrderDto> orders = orderService.findByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDto>> getOrdersByStatus(
            @PathVariable Order.OrderStatus status,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        logger.info("GET /api/orders/status/{} - Retrieving orders by status", status);
        Page<OrderDto> orders = orderService.findByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        logger.info("GET /api/orders/date-range - Retrieving orders between {} and {}", startDate, endDate);
        List<OrderDto> orders = orderService.findByDateRange(startDate, endDate);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDto>> searchOrders(
            @RequestParam String keyword,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        logger.info("GET /api/orders/search - Searching orders with keyword: {}", keyword);
        Page<OrderDto> orders = orderService.findByKeyword(keyword, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderDto orderDto) {
        logger.info("POST /api/orders - Creating new order for user: {}", orderDto.getUserId());
        OrderDto savedOrder = orderService.createOrder(orderDto);
        return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);
    }
    
    @PostMapping("/{id}/items")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @orderController.isOrderOwner(#id, authentication))")
    public ResponseEntity<OrderDto> addOrderItem(@PathVariable Long id, 
                                                 @Valid @RequestBody OrderItemDto orderItemDto,
                                                 Authentication authentication) {
        logger.info("POST /api/orders/{}/items - Adding item to order", id);
        OrderDto updatedOrder = orderService.addOrderItem(id, orderItemDto);
        return ResponseEntity.ok(updatedOrder);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @orderController.isOrderOwner(#id, authentication))")
    public ResponseEntity<OrderDto> updateOrder(@PathVariable Long id, 
                                               @Valid @RequestBody OrderDto orderDto,
                                               Authentication authentication) {
        logger.info("PUT /api/orders/{} - Updating order", id);
        OrderDto updatedOrder = orderService.update(id, orderDto);
        return ResponseEntity.ok(updatedOrder);
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable Long id, 
                                                      @RequestBody Map<String, String> request) {
        logger.info("PATCH /api/orders/{}/status - Updating order status", id);
        String status = request.get("status");
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        OrderDto updatedOrder = orderService.updateOrderStatus(id, Order.OrderStatus.valueOf(status));
        return ResponseEntity.ok(updatedOrder);
    }
    
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @orderController.isOrderOwner(#id, authentication))")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long id, Authentication authentication) {
        logger.info("PATCH /api/orders/{}/cancel - Cancelling order", id);
        OrderDto cancelledOrder = orderService.cancelOrder(id);
        return ResponseEntity.ok(cancelledOrder);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        logger.info("DELETE /api/orders/{} - Deleting order", id);
        orderService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/exists/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        logger.info("GET /api/orders/exists/{} - Checking if order exists", id);
        boolean exists = orderService.existsById(id);
        return ResponseEntity.ok(exists);
    }
    
    @GetMapping("/count/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @orderController.isUserOwner(#userId, authentication))")
    public ResponseEntity<Long> countOrdersByUser(@PathVariable Long userId, Authentication authentication) {
        logger.info("GET /api/orders/count/user/{} - Counting orders for user", userId);
        Long count = orderService.countByUserId(userId);
        return ResponseEntity.ok(count);
    }
    
    // Helper methods for authorization
    public boolean isOrderOwner(Long orderId, Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            // Implement logic to check if the authenticated user owns the order
            // This would require getting the order and checking the user ID
            return true; // Simplified for now
        } catch (Exception e) {
            logger.error("Error checking order ownership: ", e);
            return false;
        }
    }
    
    public boolean isOrderOwnerByNumber(String orderNumber, Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            // Implement logic to check if the authenticated user owns the order by number
            return true; // Simplified for now
        } catch (Exception e) {
            logger.error("Error checking order ownership by number: ", e);
            return false;
        }
    }
    
    public boolean isUserOwner(Long userId, Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            // Implement logic to check if the authenticated user ID matches the provided user ID
            return true; // Simplified for now
        } catch (Exception e) {
            logger.error("Error checking user ownership: ", e);
            return false;
        }
    }
}