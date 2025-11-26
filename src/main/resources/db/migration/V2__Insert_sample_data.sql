-- Insert sample categories
INSERT INTO categories (name, description) VALUES
('Electronics', 'Electronic devices and accessories'),
('Clothing', 'Apparel and fashion items'),
('Books', 'Books, ebooks, and educational materials'),
('Home & Garden', 'Home improvement and gardening supplies'),
('Sports & Fitness', 'Sports equipment and fitness gear');

-- Insert sample users
INSERT INTO users (username, email, first_name, last_name, phone_number, address, city, postal_code, country, role, keycloak_id) VALUES
('admin', 'admin@shop.com', 'Admin', 'User', '+1234567890', '123 Admin St', 'Admin City', '12345', 'USA', 'ADMIN', 'admin-keycloak-id'),
('john.doe', 'john.doe@example.com', 'John', 'Doe', '+1234567891', '456 Customer St', 'Customer City', '23456', 'USA', 'CUSTOMER', 'john-keycloak-id'),
('jane.smith', 'jane.smith@example.com', 'Jane', 'Smith', '+1234567892', '789 Buyer Ave', 'Buyer City', '34567', 'USA', 'CUSTOMER', 'jane-keycloak-id');

-- Insert sample products
INSERT INTO products (name, description, price, stock_quantity, sku, category_id, is_active) VALUES
-- Electronics
('Laptop Pro 15"', 'High-performance laptop with 16GB RAM and 512GB SSD', 1299.99, 10, 'LAPTOP-PRO-15', 1, TRUE),
('Smartphone X', 'Latest smartphone with advanced camera and 5G connectivity', 799.99, 25, 'PHONE-X-128', 1, TRUE),
('Wireless Headphones', 'Noise-cancelling wireless headphones with 30-hour battery', 199.99, 50, 'HEADPHONE-WL', 1, TRUE),
('4K Monitor', '27-inch 4K UHD monitor with HDR support', 349.99, 15, 'MONITOR-4K-27', 1, TRUE),

-- Clothing
('Cotton T-Shirt', 'Comfortable cotton t-shirt available in multiple colors', 29.99, 100, 'TSHIRT-COTTON', 2, TRUE),
('Denim Jeans', 'Classic blue denim jeans with modern fit', 79.99, 75, 'JEANS-DENIM', 2, TRUE),
('Winter Jacket', 'Warm winter jacket with water-resistant coating', 149.99, 30, 'JACKET-WINTER', 2, TRUE),
('Running Shoes', 'Lightweight running shoes with advanced cushioning', 119.99, 40, 'SHOES-RUNNING', 2, TRUE),

-- Books
('Programming Guide', 'Complete guide to modern programming languages', 49.99, 20, 'BOOK-PROG-GUIDE', 3, TRUE),
('Cooking Masterclass', 'Professional cooking techniques and recipes', 34.99, 25, 'BOOK-COOKING', 3, TRUE),
('History of Technology', 'Comprehensive history of technological advancement', 39.99, 15, 'BOOK-TECH-HIST', 3, TRUE),

-- Home & Garden
('Garden Tool Set', 'Complete set of essential gardening tools', 89.99, 20, 'GARDEN-TOOLS', 4, TRUE),
('LED Desk Lamp', 'Adjustable LED desk lamp with USB charging port', 45.99, 35, 'LAMP-LED-DESK', 4, TRUE),
('Coffee Maker', 'Programmable coffee maker with thermal carafe', 129.99, 12, 'COFFEE-MAKER', 4, TRUE),

-- Sports & Fitness
('Yoga Mat', 'Non-slip yoga mat with carrying strap', 39.99, 60, 'YOGA-MAT-PRO', 5, TRUE),
('Dumbbell Set', 'Adjustable dumbbell set with multiple weight plates', 199.99, 10, 'DUMBBELL-SET', 5, TRUE),
('Tennis Racket', 'Professional tennis racket with carbon fiber frame', 159.99, 8, 'TENNIS-RACKET', 5, TRUE);

-- Insert sample orders
INSERT INTO orders (order_number, user_id, status, shipping_address, billing_address) VALUES
('ORD-2024-001', 2, 'DELIVERED', '456 Customer St, Customer City, 23456, USA', '456 Customer St, Customer City, 23456, USA'),
('ORD-2024-002', 3, 'SHIPPED', '789 Buyer Ave, Buyer City, 34567, USA', '789 Buyer Ave, Buyer City, 34567, USA'),
('ORD-2024-003', 2, 'PROCESSING', '456 Customer St, Customer City, 23456, USA', '456 Customer St, Customer City, 23456, USA');

-- Insert sample order items
INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) VALUES
-- Order 1
(1, 1, 1, 1299.99, 1299.99),
(1, 3, 1, 199.99, 199.99),

-- Order 2
(2, 5, 2, 29.99, 59.98),
(2, 6, 1, 79.99, 79.99),
(2, 15, 1, 39.99, 39.99),

-- Order 3
(3, 2, 1, 799.99, 799.99),
(3, 8, 1, 119.99, 119.99);

-- Update order totals
UPDATE orders SET total_amount = (
    SELECT COALESCE(SUM(subtotal), 0)
    FROM order_items
    WHERE order_items.order_id = orders.id
);

-- Update product stock based on order items
UPDATE products SET stock_quantity = stock_quantity - (
    SELECT COALESCE(SUM(quantity), 0)
    FROM order_items
    WHERE order_items.product_id = products.id
) WHERE id IN (SELECT DISTINCT product_id FROM order_items);