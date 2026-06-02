# 7-Eleven Vietnam — Retail Order Management System

**Technical Test Submission**  
**Candidate:** Vo Thanh Truong Long  
**Position:** Fresher Java Engineer  

---

## 📋 Overview

Web-based retail order management system built with **Spring Boot 3** + **Thymeleaf** + **PostgreSQL**.

**USER**
<img width="1912" height="995" alt="image" src="https://github.com/user-attachments/assets/85153022-b024-419f-92a6-520978c0270b" />
<img width="1908" height="1011" alt="image" src="https://github.com/user-attachments/assets/b27c5bb0-8b1b-4829-9988-2e82ff5bcbb1" />
<img width="1913" height="933" alt="image" src="https://github.com/user-attachments/assets/6236bdd4-a19b-46b9-95fa-e11522f95862" />

**PRODUCTS**
<img width="1890" height="600" alt="image" src="https://github.com/user-attachments/assets/2da388d6-9c3d-460c-bceb-3ed00620728c" />
<img width="1906" height="956" alt="image" src="https://github.com/user-attachments/assets/d7c300d1-08df-40bf-a80b-a46700af41fe" />
<img width="1891" height="953" alt="image" src="https://github.com/user-attachments/assets/d2ad4275-997b-4e75-8dcb-b00c11a7378c" />
<img width="1877" height="957" alt="image" src="https://github.com/user-attachments/assets/11b76795-078a-4639-9c89-565a0266fb08" />
<img width="1918" height="962" alt="image" src="https://github.com/user-attachments/assets/a01dba88-2972-49b4-9a08-4698b771b983" />  
<img width="1901" height="957" alt="image" src="https://github.com/user-attachments/assets/4f215650-307e-4b81-937d-7efdca67575e" />

**ORDER**
<img width="1895" height="973" alt="image" src="https://github.com/user-attachments/assets/b43c8389-e9db-4ff8-af42-0712be2f105f" />
<img width="1812" height="902" alt="image" src="https://github.com/user-attachments/assets/7b6edf23-6ab5-490e-b592-f3946837f0a6" />
<img width="1911" height="878" alt="image" src="https://github.com/user-attachments/assets/d612a199-34f4-47ad-827d-d5601b57670a" />

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17, Spring Boot 3.5.14, Spring Data JPA |
| **Frontend** | Thymeleaf, HTML5, CSS3 (Tailwind) |
| **Database** | PostgreSQL (production), H2 (test) |
| **Build** | Maven |
| **Testing** | JUnit 5, Mockito |

---

## ✨ Features

### 1️⃣ Admin — Product Management
- ✅ List all products (including inactive)
- ✅ View product details
- ✅ Create new product
- ✅ Update product
- ✅ Delete product (soft delete — sets `status = INACTIVE`)

### 2️⃣ User — Order Placement
- ✅ Browse active products
- ✅ Create order with multiple items
- ✅ Real-time stock validation
- ✅ Backend-calculated total amount

### 3️⃣ Admin — Order Management
- ✅ View all orders
- ✅ View order details

---

## 🗄️ Database Design

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Product   │         │  OrderItem   │         │    Order    │
├─────────────┤         ├──────────────┤         ├─────────────┤
│ id (PK)     │◄────────│ productId(FK)│         │ id (PK)     │
│ name        │         │ orderId (FK) │────────►│ customerName│
│ sku (UNIQUE)│         │ quantity     │         │ customerPhone│
│ price       │         │ unitPrice    │         │ totalAmount │
│ stockQuantity│        │ subtotal     │         │ status      │
│ status      │         └──────────────┘         │ createdAt   │
│ createdAt   │                                  └─────────────┘
│ updatedAt   │
└─────────────┘
```

**Note:** `OrderItem.unitPrice` stores product price at order creation time to preserve historical pricing.

---

## 🔐 Business Rules

### Product Management
- Soft delete: set `status = INACTIVE` instead of deleting from database
- Inactive products hidden from user view but remain in order history

### Order Validation
1. Product must exist
2. Product must be `ACTIVE`
3. Quantity > 0
4. Quantity ≤ available stock
5. Backend calculates total (frontend values ignored)
6. Stock decreases after successful order

---

## 🚀 Setup & Run

### Prerequisites
- Java 17+
- PostgreSQL 12+
- Maven (or use `./mvnw`)

### Step 1: Create Database
```sql
CREATE DATABASE seveneleven;
```

### Step 2: Configure Connection
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/seveneleven
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### Step 3: Run
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

Or use your IDE to run `SevenRetailOrderManagementApplication.java`

**Access:** `http://localhost:8081`

---

## 🌐 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| **User** |
| GET | `/products` | Browse products & place order |
| POST | `/orders` | Submit order |
| **Admin** |
| GET | `/admin/products` | Manage products |
| GET | `/admin/products/new` | Create product form |
| POST | `/admin/products` | Save new product |
| GET | `/admin/products/{id}/edit` | Edit product form |
| POST | `/admin/products/{id}` | Update product |
| POST | `/admin/products/{id}/delete` | Soft delete |
| GET | `/admin/orders` | View orders |
| GET | `/admin/orders/{id}` | Order details |

---

## 🧪 Testing

```bash
.\mvnw.cmd test
```

**ProductServiceTest** (6 tests)
- Get all products
- Get active products only
- Create product
- Handle duplicate SKU
- Update product
- Soft delete

**OrderServiceTest** (4 tests)
- Create order & decrease stock
- Reject insufficient stock
- Reject inactive product
- Verify total calculation

---

## CI

This project uses GitHub Actions for continuous integration.

Workflow file: `.github/workflows/ci.yml`


---

## 📁 Project Structure

```
src/main/java/com/seveneleven/
├── SevenRetailOrderManagementApplication.java
├── constants/          # Shared constants
├── controller/         # MVC controllers
├── dto/                # Request/Response objects
├── entity/             # JPA entities
├── exception/          # Custom exceptions + handler
├── mapper/             # Entity ↔ DTO conversion
├── repository/         # Data access
└── service/            # Business logic
```

---

## 👤 Contact

**Vo Thanh Truong Long**  
📞 0364964897  
✉️ longvo04100000@gmail.com

**Applied for:** Fresher Java Engineer — 7-Eleven Vietnam
