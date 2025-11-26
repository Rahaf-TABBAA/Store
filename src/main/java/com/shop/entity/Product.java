package com.shop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    @Column(name = "name", nullable = false)
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description")
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;
    
    @Column(name = "sku", unique = true)
    private String sku;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    // Constructors
    public Product() {}
    
    public Product(String name, String description, BigDecimal price, Integer stockQuantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    
    // Helper methods
    public boolean isInStock() {
        return stockQuantity > 0;
    }
    
    public void decreaseStock(Integer quantity) {
        if (stockQuantity >= quantity) {
            stockQuantity -= quantity;
        } else {
            throw new IllegalArgumentException("Insufficient stock");
        }
    }
    
    public void increaseStock(Integer quantity) {
        stockQuantity += quantity;
    }
}