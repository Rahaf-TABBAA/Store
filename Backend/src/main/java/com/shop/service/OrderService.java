package com.shop.service;

import com.shop.dto.OrderDto;
import com.shop.dto.OrderItemDto;
import com.shop.entity.Order;
import com.shop.entity.OrderItem;
import com.shop.entity.Product;
import com.shop.entity.User;
import com.shop.exception.ResourceNotFoundException;
import com.shop.mapper.OrderMapper;
import com.shop.repository.OrderRepository;
import com.shop.repository.ProductRepository;
import com.shop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    
    @Autowired
    public OrderService(OrderRepository orderRepository,
                       UserRepository userRepository,
                       ProductRepository productRepository,
                       OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
    }
    
    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        logger.debug("Finding all orders");
        List<Order> orders = orderRepository.findAll();
        return orderMapper.toDtoList(orders);
    }
    
    @Transactional(readOnly = true)
    public Page<OrderDto> findAll(Pageable pageable) {
        logger.debug("Finding all orders with pagination");
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(orderMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        logger.debug("Finding order by id: {}", id);
        Order order = orderRepository.findByIdWithUserAndOrderItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return orderMapper.toDto(order);
    }
    
    @Transactional(readOnly = true)
    public OrderDto findByOrderNumber(String orderNumber) {
        logger.debug("Finding order by order number: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        return orderMapper.toDto(order);
    }
    
    @Transactional(readOnly = true)
    public List<OrderDto> findByUserId(Long userId) {
        logger.debug("Finding orders by user id: {}", userId);
        List<Order> orders = orderRepository.findByUserId(userId);
        return orderMapper.toDtoList(orders);
    }
    
    @Transactional(readOnly = true)
    public Page<OrderDto> findByUserId(Long userId, Pageable pageable) {
        logger.debug("Finding orders by user id: {} with pagination", userId);
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return orders.map(orderMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public List<OrderDto> findByStatus(Order.OrderStatus status) {
        logger.debug("Finding orders by status: {}", status);
        List<Order> orders = orderRepository.findByStatus(status);
        return orderMapper.toDtoList(orders);
    }
    
    @Transactional(readOnly = true)
    public Page<OrderDto> findByStatus(Order.OrderStatus status, Pageable pageable) {
        logger.debug("Finding orders by status: {} with pagination", status);
        Page<Order> orders = orderRepository.findByStatus(status, pageable);
        return orders.map(orderMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public List<OrderDto> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Finding orders by date range: {} to {}", startDate, endDate);
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        return orderMapper.toDtoList(orders);
    }
    
    @Transactional(readOnly = true)
    public Page<OrderDto> findByKeyword(String keyword, Pageable pageable) {
        logger.debug("Finding orders by keyword: {} with pagination", keyword);
        Page<Order> orders = orderRepository.findByKeyword(keyword, pageable);
        return orders.map(orderMapper::toDto);
    }
    
    public OrderDto createOrder(OrderDto orderDto) {
        logger.debug("Creating order for user id: {}", orderDto.getUserId());
        
        User user = userRepository.findById(orderDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + orderDto.getUserId()));
        
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        
        if (orderDto.getShippingAddress() != null) {
            order.setShippingAddress(orderDto.getShippingAddress());
        }
        if (orderDto.getBillingAddress() != null) {
            order.setBillingAddress(orderDto.getBillingAddress());
        }
        if (orderDto.getNotes() != null) {
            order.setNotes(orderDto.getNotes());
        }
        
        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully with id: {}", savedOrder.getId());
        
        return orderMapper.toDto(savedOrder);
    }
    
    public OrderDto addOrderItem(Long orderId, OrderItemDto orderItemDto) {
        logger.debug("Adding item to order id: {}", orderId);
        
        Order order = orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        Product product = productRepository.findById(orderItemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + orderItemDto.getProductId()));
        
        if (!product.isInStock()) {
            throw new IllegalArgumentException("Product '" + product.getName() + "' is out of stock");
        }
        
        if (product.getStockQuantity() < orderItemDto.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStockQuantity() + 
                                             ", Requested: " + orderItemDto.getQuantity());
        }
        
        OrderItem orderItem = new OrderItem(product, orderItemDto.getQuantity(), product.getPrice());
        order.addOrderItem(orderItem);
        
        // Decrease product stock
        product.decreaseStock(orderItemDto.getQuantity());
        productRepository.save(product);
        
        Order updatedOrder = orderRepository.save(order);
        logger.info("Order item added successfully to order id: {}", orderId);
        
        return orderMapper.toDto(updatedOrder);
    }
    
    public OrderDto updateOrderStatus(Long id, Order.OrderStatus status) {
        logger.debug("Updating order status for id: {} to: {}", id, status);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        logger.info("Order status updated successfully for id: {}", id);
        
        return orderMapper.toDto(updatedOrder);
    }
    
    public OrderDto update(Long id, OrderDto orderDto) {
        logger.debug("Updating order with id: {}", id);
        
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        
        orderMapper.updateEntityFromDto(orderDto, existingOrder);
        Order updatedOrder = orderRepository.save(existingOrder);
        logger.info("Order updated successfully with id: {}", updatedOrder.getId());
        
        return orderMapper.toDto(updatedOrder);
    }
    
    public OrderDto cancelOrder(Long id) {
        logger.debug("Cancelling order with id: {}", id);
        
        Order order = orderRepository.findByIdWithOrderItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        
        if (order.getStatus() == Order.OrderStatus.DELIVERED || 
            order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot cancel order with status: " + order.getStatus());
        }
        
        // Restore product stock
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.increaseStock(orderItem.getQuantity());
            productRepository.save(product);
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        logger.info("Order cancelled successfully with id: {}", id);
        
        return orderMapper.toDto(updatedOrder);
    }
    
    public void deleteById(Long id) {
        logger.debug("Deleting order with id: {}", id);
        
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }
        
        orderRepository.deleteById(id);
        logger.info("Order deleted successfully with id: {}", id);
    }
    
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return orderRepository.existsById(id);
    }
    
    @Transactional(readOnly = true)
    public boolean existsByOrderNumber(String orderNumber) {
        return orderRepository.existsByOrderNumber(orderNumber);
    }
    
    @Transactional(readOnly = true)
    public Long countByUserId(Long userId) {
        return orderRepository.countByUserId(userId);
    }
    
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}