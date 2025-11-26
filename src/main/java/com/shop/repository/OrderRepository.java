package com.shop.repository;

import com.shop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserId(Long userId);
    
    Page<Order> findByUserId(Long userId, Pageable pageable);
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByStatus(Order.OrderStatus status);
    
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    Page<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Order.OrderStatus status);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<Order> findByIdWithOrderItems(@Param("id") Long id);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<Order> findByIdWithUserAndOrderItems(@Param("id") Long id);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    boolean existsByOrderNumber(String orderNumber);
    
    @Query("SELECT o FROM Order o WHERE o.orderNumber LIKE %:keyword% OR o.user.username LIKE %:keyword% OR o.user.email LIKE %:keyword%")
    Page<Order> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}