# 🚀 **Shop Application API Testing Guide**

## 📁 **Postman Collection Created**

I've generated a complete Postman collection with all endpoints for your Spring Boot shop application:

- **Collection**: `Shop-Application-API.postman_collection.json`
- **Environment**: `Shop-Application.postman_environment.json`

## 🔧 **Setup Instructions**

### **1. Import into Postman**
1. Open Postman
2. Click **Import**
3. Select both files:
   - `postman/Shop-Application-API.postman_collection.json`
   - `postman/Shop-Application.postman_environment.json`
4. Set the environment to "Shop Application Environment"

### **2. Start Your Application**
```powershell
# Make sure services are running
docker-compose -f docker-compose.dev.yml up -d
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## 🎯 **Testing Workflow**

### **Step 1: Get Authentication Token**
1. **Get Admin Token**: Run `Authentication > Get Admin Token`
   - Automatically sets `{{access_token}}` variable
   - Used for admin endpoints (CRUD operations)

2. **Get Customer Token**: Run `Authentication > Get Customer Token`
   - Sets `{{customer_token}}` variable
   - Used for customer-specific endpoints

### **Step 2: Test Public Endpoints** (No auth required)
- ✅ `Health & Info > Health Check`
- ✅ `Categories > Get All Categories`
- ✅ `Products > Get All Products`
- ✅ `Products > Search Products`

### **Step 3: Test Admin Endpoints** (Requires admin token)
- 🔐 `Categories > Create Category`
- 🔐 `Products > Create Product`
- 🔐 `Users > Get All Users`
- 🔐 `Orders > Get All Orders`

### **Step 4: Test Customer Endpoints** (Requires auth)
- 👤 `Users > Get Current User Profile`
- 👤 `Orders > Get My Orders`
- 👤 `Orders > Create Order`

## 📚 **Available Endpoints**

### **🔍 Health & Monitoring**
- `GET /actuator/health` - Health check
- `GET /actuator/info` - Application info

### **🏷️ Categories**
- `GET /api/categories` - List all categories
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories` - Create category (Admin)
- `PUT /api/categories/{id}` - Update category (Admin)
- `DELETE /api/categories/{id}` - Delete category (Admin)

### **📦 Products**
- `GET /api/products` - List products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/search?keyword=` - Search products
- `GET /api/products/category/{id}` - Products by category
- `POST /api/products` - Create product (Admin)
- `PUT /api/products/{id}` - Update product (Admin)
- `PATCH /api/products/{id}/stock` - Update stock (Admin)
- `GET /api/products/low-stock` - Low stock products (Admin)
- `DELETE /api/products/{id}` - Delete product (Admin)

### **👥 Users**
- `GET /api/users` - List all users (Admin)
- `GET /api/users/me` - Current user profile
- `POST /api/users` - Create user (Admin)
- `PUT /api/users/{id}` - Update user

### **🛒 Orders**
- `GET /api/orders` - List all orders (Admin)
- `GET /api/orders/my-orders` - User's orders
- `GET /api/orders/{id}` - Get order by ID
- `POST /api/orders` - Create order
- `POST /api/orders/{id}/items` - Add items to order
- `PATCH /api/orders/{id}/status` - Update status (Admin)
- `PATCH /api/orders/{id}/cancel` - Cancel order

## 🔐 **Authentication Details**

### **Keycloak Setup Required**
Before testing, configure Keycloak:

1. **Access Keycloak**: http://localhost:8180
2. **Login**: admin / admin
3. **Create Realm**: `shop-realm`
4. **Create Client**: `shop-client`
5. **Create Users**:
   - **Admin**: username=`admin`, password=`admin123`, role=`ADMIN`
   - **Customer**: username=`customer`, password=`customer123`, role=`CUSTOMER`

### **Token Usage**
- Admin token: Full access to all endpoints
- Customer token: Limited to user-specific operations
- Tokens auto-refresh in Postman collection

## 📝 **Sample Request Bodies**

### **Create Category**
```json
{
    "name": "Electronics",
    "description": "Electronic devices and gadgets"
}
```

### **Create Product**
```json
{
    "name": "MacBook Pro",
    "description": "Apple MacBook Pro 16-inch with M2 chip",
    "price": 2499.99,
    "stockQuantity": 50,
    "categoryId": 1
}
```

### **Create Order**
```json
{
    "status": "PENDING",
    "totalAmount": 2499.99
}
```

### **Add Order Item**
```json
{
    "productId": 1,
    "quantity": 2,
    "price": 2499.99
}
```

## 🐛 **Troubleshooting**

### **Common Issues**
1. **401 Unauthorized**: Get fresh token from Authentication folder
2. **403 Forbidden**: Check user has correct role (ADMIN/CUSTOMER)
3. **404 Not Found**: Ensure application is running on port 8080
4. **500 Server Error**: Check application logs

### **Verification Commands**
```powershell
# Check if app is running
curl http://localhost:8080/actuator/health

# Check Keycloak
curl http://localhost:8180/health/ready

# View app logs
docker-compose logs shop-app
```

## 🎉 **Quick Test Sequence**

1. **Health Check**: `GET /actuator/health`
2. **Get Admin Token**: `POST /realms/shop-realm/protocol/openid-connect/token`
3. **Create Category**: `POST /api/categories`
4. **Create Product**: `POST /api/products`
5. **Get Products**: `GET /api/products`
6. **Search Products**: `GET /api/products/search?keyword=MacBook`

Your complete API is now ready for testing! 🚀