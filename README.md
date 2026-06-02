# 7-Eleven Vietnam — Retail Order Management
Bài test kỹ thuật cho vị trí **Java Engineer — 7-Eleven Vietnam**.

Web app quản lý sản phẩm và đơn hàng, xây dựng bằng Spring Boot 3 + Thymeleaf + PostgreSQL.

---

## Mục lục
- [Tech Stack](#tech-stack)
- [Tính năng](#tính-năng)
- [Cấu trúc database](#cấu-trúc-database)
- [Business rules quan trọng](#business-rules-quan-trọng)
- [Cài đặt & chạy local](#cài-đặt--chạy-local)
- [Các URL chính](#các-url-chính)
- [Chạy test](#chạy-test)
- [Cấu trúc project](#cấu-trúc-project)
- [Hướng phát triển thêm](#hướng-phát-triển-thêm)

---

## Tech Stack

**Backend**
- Java 17
- Spring Boot 3.5.14 (Spring MVC, Spring Data JPA)
- PostgreSQL (production) / H2 (test)
- Lombok
- Maven

**Frontend**
- Thymeleaf (server-side rendering)
- Tailwind CSS
- Font Awesome 6

**Testing**
- JUnit 5 + Mockito (unit test tầng Service)
- H2 in-memory (không cần PostgreSQL khi test)

---

## Tính năng

**Admin — Quản lý sản phẩm**
- Xem danh sách tất cả sản phẩm (kể cả INACTIVE)
- Xem chi tiết, tạo mới, cập nhật sản phẩm
- Xóa mềm: set `status = INACTIVE` thay vì xóa khỏi DB

**User — Đặt hàng**
- Xem danh sách sản phẩm đang ACTIVE
- Tạo đơn hàng với nhiều sản phẩm

**Admin — Quản lý đơn hàng**
- Xem danh sách tất cả đơn hàng
- Xem chi tiết từng đơn (sản phẩm, số lượng, tiền)

---

## Cấu trúc database

```
Product          OrderItem         Order
--------         ---------         -----
id (PK)    ◄─── productId (FK)     id (PK)
name             orderId (FK) ───► customerName
sku (UNIQUE)     quantity           customerPhone
price            unitPrice          totalAmount
stockQuantity    subtotal           status
status                              createdAt
createdAt
updatedAt
```

> **Tại sao lưu `unitPrice` trong OrderItem?**  
> Để snapshot giá tại thời điểm đặt hàng. Nếu sau này admin đổi giá sản phẩm, lịch sử đơn hàng cũ vẫn đúng.

---

## Cài đặt & chạy local

### Yêu cầu
- Java 17+
- PostgreSQL 12+
- Maven (hoặc dùng `./mvnw` đã có sẵn)

### Bước 1 — Tạo database
```sql
CREATE DATABASE seveneleven;
```

### Bước 2 — Sửa config
Mở `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/seveneleven
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### Bước 3 — Chạy app
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / Mac
./mvnw spring-boot:run
```
Hoặc dùng IntelliJ: Run `SevenRetailOrderManagementApplication.java`.

App chạy ở: `http://localhost:8081`

---

## Các URL chính

| URL | Mô tả |
|-----|-------|
| `GET /products` | User — xem sản phẩm, đặt hàng |
| `GET /admin/products` | Admin — quản lý sản phẩm |
| `GET /admin/orders` | Admin — quản lý đơn hàng |

**Chi tiết endpoints**

| Method | URL | Chức năng |
|--------|-----|-----------|
| GET | `/admin/products` | Danh sách sản phẩm |
| GET | `/admin/products/new` | Form tạo mới |
| POST | `/admin/products` | Tạo sản phẩm |
| GET | `/admin/products/{id}/edit` | Form sửa |
| POST | `/admin/products/{id}` | Cập nhật |
| POST | `/admin/products/{id}/delete` | Xóa mềm |
| GET | `/admin/orders` | Danh sách đơn hàng |
| GET | `/admin/orders/{id}` | Chi tiết đơn hàng |
| GET | `/products` | Sản phẩm đang bán |
| POST | `/orders` | Tạo đơn hàng |

---

## Chạy test

```bash
# Không cần PostgreSQL — dùng H2 in-memory
.\mvnw.cmd test
```

**ProductServiceTest**
| Test | Kịch bản |
|------|----------|
| `testGetAllProducts` | Trả về tất cả sản phẩm |
| `testGetActiveProducts` | Chỉ trả về sản phẩm ACTIVE |
| `testCreateProduct_success` | Tạo thành công |
| `testCreateProduct_duplicateSku` | Ném BusinessException khi SKU trùng |
| `testUpdateProduct_success` | Cập nhật thành công |
| `testDeleteProduct` | Set status = INACTIVE |

**OrderServiceTest**
| Test | Kịch bản |
|------|----------|
| `testCreateOrder_success` | Tạo đơn, trừ tồn kho |
| `testCreateOrder_insufficientStock` | Ném BusinessException khi hết hàng |
| `testCreateOrder_inactiveProduct` | Ném BusinessException khi sản phẩm INACTIVE |
| `testCreateOrder_totalAmountCalculation` | Backend tính đúng tổng tiền |

---

## Cấu trúc project

```
src/main/java/com/seveneleven/
├── SevenRetailOrderManagementApplication.java
├── constants/
│   ├── MessageConstants.java    ← chuỗi thông báo dùng chung
│   └── ViewConstants.java       ← đường dẫn view dùng chung
├── controller/
│   ├── BaseController.java      ← helper dùng chung cho controller
│   ├── AdminProductController.java
│   ├── AdminOrderController.java
│   └── UserController.java
├── dto/
│   ├── ProductRequest.java
│   ├── ProductResponse.java
│   ├── OrderRequest.java
│   └── OrderItemRequest.java
├── entity/
│   ├── Product.java
│   ├── Order.java
│   └── OrderItem.java
├── exception/
│   ├── GlobalExceptionHandler.java   ← @ControllerAdvice xử lý lỗi chung
│   ├── BusinessException.java
│   └── ResourceNotFoundException.java
├── mapper/
│   └── ProductMapper.java       ← convert Entity ↔ DTO
├── repository/
│   ├── ProductRepository.java
│   └── OrderRepository.java
└── service/
    ├── ProductService.java
    └── OrderService.java
```

---

## Hướng phát triển thêm

| Tính năng | Công nghệ |
|-----------|-----------|
| Cache sản phẩm | Redis |
| Async order events | Kafka / RabbitMQ |
| Đăng nhập / phân quyền | Spring Security + JWT |
| Phân trang & tìm kiếm | Spring Data Pageable |
| Containerize full stack | Docker Compose |
| CI/CD tự động | GitHub Actions |
| Monitoring | Prometheus + Grafana |

---

## Tác giả

**Vo Thanh Truong Long** — Ứng tuyển vị trí Java Engineer tại 7-Eleven Vietnam
