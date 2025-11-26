package com.shop.controller;

import com.shop.dto.UserDto;
import com.shop.entity.User;
import com.shop.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        logger.info("GET /api/users - Retrieving all users");
        List<UserDto> users = userService.findAll();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getActiveUsers() {
        logger.info("GET /api/users/active - Retrieving active users");
        List<UserDto> users = userService.findActiveUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable User.Role role) {
        logger.info("GET /api/users/role/{} - Retrieving users by role", role);
        List<UserDto> users = userService.findByRole(role);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @userController.isOwner(#id, authentication))")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id, Authentication authentication) {
        logger.info("GET /api/users/{} - Retrieving user", id);
        UserDto user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        logger.info("GET /api/users/me - Retrieving current user");
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();
        
        UserDto user = userService.findByKeycloakId(keycloakId);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String keyword) {
        logger.info("GET /api/users/search - Searching users with keyword: {}", keyword);
        List<UserDto> users = userService.findByKeyword(keyword);
        return ResponseEntity.ok(users);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        logger.info("POST /api/users - Creating new user: {}", userDto.getUsername());
        UserDto savedUser = userService.save(userDto);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @userController.isOwner(#id, authentication))")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, 
                                             @Valid @RequestBody UserDto userDto,
                                             Authentication authentication) {
        logger.info("PUT /api/users/{} - Updating user", id);
        UserDto updatedUser = userService.update(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> deactivateUser(@PathVariable Long id) {
        logger.info("PATCH /api/users/{}/deactivate - Deactivating user", id);
        UserDto deactivatedUser = userService.deactivateUser(id);
        return ResponseEntity.ok(deactivatedUser);
    }
    
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> activateUser(@PathVariable Long id) {
        logger.info("PATCH /api/users/{}/activate - Activating user", id);
        UserDto activatedUser = userService.activateUser(id);
        return ResponseEntity.ok(activatedUser);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("DELETE /api/users/{} - Deleting user", id);
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/exists/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        logger.info("GET /api/users/exists/{} - Checking if user exists", id);
        boolean exists = userService.existsById(id);
        return ResponseEntity.ok(exists);
    }
    
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> existsByUsername(@PathVariable String username) {
        logger.info("GET /api/users/exists/username/{} - Checking if username exists", username);
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }
    
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        logger.info("GET /api/users/exists/email/{} - Checking if email exists", email);
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
    
    // Helper method for authorization
    public boolean isOwner(Long userId, Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String keycloakId = jwt.getSubject();
            UserDto currentUser = userService.findByKeycloakId(keycloakId);
            return currentUser.getId().equals(userId);
        } catch (Exception e) {
            logger.error("Error checking user ownership: ", e);
            return false;
        }
    }
}