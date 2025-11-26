package com.shop.service;

import com.shop.dto.UserDto;
import com.shop.entity.User;
import com.shop.exception.ResourceNotFoundException;
import com.shop.mapper.UserMapper;
import com.shop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }
    
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        logger.debug("Finding all users");
        List<User> users = userRepository.findAll();
        return userMapper.toDtoList(users);
    }
    
    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        logger.debug("Finding user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }
    
    @Transactional(readOnly = true)
    public UserDto findByUsername(String username) {
        logger.debug("Finding user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return userMapper.toDto(user);
    }
    
    @Transactional(readOnly = true)
    public UserDto findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toDto(user);
    }
    
    @Transactional(readOnly = true)
    public UserDto findByKeycloakId(String keycloakId) {
        logger.debug("Finding user by keycloak id: {}", keycloakId);
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with keycloak id: " + keycloakId));
        return userMapper.toDto(user);
    }
    
    @Transactional(readOnly = true)
    public List<UserDto> findByRole(User.Role role) {
        logger.debug("Finding users by role: {}", role);
        List<User> users = userRepository.findByRole(role);
        return userMapper.toDtoList(users);
    }
    
    @Transactional(readOnly = true)
    public List<UserDto> findActiveUsers() {
        logger.debug("Finding active users");
        List<User> users = userRepository.findByIsActiveTrue();
        return userMapper.toDtoList(users);
    }
    
    @Transactional(readOnly = true)
    public List<UserDto> findByKeyword(String keyword) {
        logger.debug("Finding users by keyword: {}", keyword);
        List<User> users = userRepository.findByKeyword(keyword);
        return userMapper.toDtoList(users);
    }
    
    public UserDto save(UserDto userDto) {
        logger.debug("Saving user: {}", userDto.getUsername());
        
        if (userDto.getId() == null) {
            if (userRepository.existsByUsername(userDto.getUsername())) {
                throw new IllegalArgumentException("User with username '" + userDto.getUsername() + "' already exists");
            }
            
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new IllegalArgumentException("User with email '" + userDto.getEmail() + "' already exists");
            }
        }
        
        User user = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);
        logger.info("User saved successfully with id: {}", savedUser.getId());
        
        return userMapper.toDto(savedUser);
    }
    
    public UserDto update(Long id, UserDto userDto) {
        logger.debug("Updating user with id: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        if (!existingUser.getUsername().equals(userDto.getUsername()) && 
            userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("User with username '" + userDto.getUsername() + "' already exists");
        }
        
        if (!existingUser.getEmail().equals(userDto.getEmail()) && 
            userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("User with email '" + userDto.getEmail() + "' already exists");
        }
        
        userMapper.updateEntityFromDto(userDto, existingUser);
        User updatedUser = userRepository.save(existingUser);
        logger.info("User updated successfully with id: {}", updatedUser.getId());
        
        return userMapper.toDto(updatedUser);
    }
    
    public void deleteById(Long id) {
        logger.debug("Deleting user with id: {}", id);
        
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        
        userRepository.deleteById(id);
        logger.info("User deleted successfully with id: {}", id);
    }
    
    public UserDto deactivateUser(Long id) {
        logger.debug("Deactivating user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setIsActive(false);
        User updatedUser = userRepository.save(user);
        logger.info("User deactivated successfully with id: {}", id);
        
        return userMapper.toDto(updatedUser);
    }
    
    public UserDto activateUser(Long id) {
        logger.debug("Activating user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setIsActive(true);
        User updatedUser = userRepository.save(user);
        logger.info("User activated successfully with id: {}", id);
        
        return userMapper.toDto(updatedUser);
    }
    
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
    
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Transactional(readOnly = true)
    public boolean existsByKeycloakId(String keycloakId) {
        return userRepository.existsByKeycloakId(keycloakId);
    }
}