package com.shop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.dto.CategoryDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class CategoryIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void getAllCategories_ShouldReturnCategoriesList() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_WithValidData_ShouldCreateCategory() throws Exception {
        CategoryDto categoryDto = new CategoryDto("Test Category", "Test Description");
        
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Category"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        CategoryDto categoryDto = new CategoryDto("", "Test Description"); // Empty name
        
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void createCategory_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        CategoryDto categoryDto = new CategoryDto("Test Category", "Test Description");
        
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createCategory_WithCustomerRole_ShouldReturnForbidden() throws Exception {
        CategoryDto categoryDto = new CategoryDto("Test Category", "Test Description");
        
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isForbidden());
    }
}