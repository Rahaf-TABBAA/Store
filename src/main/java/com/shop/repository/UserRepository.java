package com.shop.repository;

import com.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByKeycloakId(String keycloakId);
    
    List<User> findByRole(User.Role role);
    
    List<User> findByIsActiveTrue();
    
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
    
    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:keyword% OR u.lastName LIKE %:keyword% OR u.username LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> findByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.id = :id")
    Optional<User> findByIdWithOrders(@Param("id") Long id);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByKeycloakId(String keycloakId);
}