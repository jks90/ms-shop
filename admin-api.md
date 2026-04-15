# Admin API — ms-shop

> **Base URL:** `http://localhost:6070`  
> Todos los endpoints requieren token de administrador: `-H "Authorization: Bearer <TOKEN>"`

---

## 📦 Categorías — `/ms-store/api/v1/admin/categories`

### Listar categorías
```bash
curl -X GET "http://localhost:6070/ms-store/api/v1/admin/categories" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

### Crear categoría
```bash
curl -X POST "http://localhost:6070/ms-store/api/v1/admin/categories" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Electrónica",
    "slug": "electronica",
    "description": "Categoría de electrónica",
    "isActive": true
  }'
```

### Actualizar categoría
```bash
curl -X PUT "http://localhost:6070/ms-store/api/v1/admin/categories/1" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Electrónica actualizada",
    "slug": "electronica",
    "description": "Nueva descripción",
    "isActive": true
  }'
```

### Eliminar categoría
```bash
curl -X DELETE "http://localhost:6070/ms-store/api/v1/admin/categories/1" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 🛍️ Productos — `/ms-store/api/v1/admin/products`

### Listar productos (paginado)
```bash
curl -X GET "http://localhost:6070/ms-store/api/v1/admin/products?page=0&size=20" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

### Crear producto
```bash
curl -X POST "http://localhost:6070/ms-store/api/v1/admin/products" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Auriculares BT",
    "slug": "auriculares-bt",
    "description": "Auriculares Bluetooth con cancelación de ruido",
    "categoryId": 1,
    "status": "ACTIVE"
  }'
```

### Actualizar producto
```bash
curl -X PUT "http://localhost:6070/ms-store/api/v1/admin/products/1" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Auriculares BT Pro",
    "slug": "auriculares-bt-pro",
    "description": "Versión mejorada",
    "categoryId": 1,
    "status": "ACTIVE"
  }'
```

### Crear variante de producto
```bash
curl -X POST "http://localhost:6070/ms-store/api/v1/admin/products/1/variants" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "AUR-BT-NEGRO",
    "name": "Negro",
    "priceAmount": 4999,
    "currency": "EUR",
    "stock": 50
  }'
```

### Eliminar producto
```bash
curl -X DELETE "http://localhost:6070/ms-store/api/v1/admin/products/1" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 📋 Pedidos — `/ms-store/api/v1/admin/orders`

### Listar pedidos (con filtros opcionales)
```bash
# Sin filtros
curl -X GET "http://localhost:6070/ms-store/api/v1/admin/orders?page=0&size=20" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"

# Filtrar por status
curl -X GET "http://localhost:6070/ms-store/api/v1/admin/orders?status=PLACED&page=0&size=20" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"

# Filtrar por paymentStatus
curl -X GET "http://localhost:6070/ms-store/api/v1/admin/orders?paymentStatus=PAID&page=0&size=20" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"

# Filtrar por usuario
curl -X GET "http://localhost:6070/ms-store/api/v1/admin/orders?authUserId=42&page=0&size=20" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

> **Valores válidos de `status`:** `CREATED`, `PLACED`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `PENDING_PAYMENT`  
> **Valores válidos de `paymentStatus`:** `PENDING`, `PAID`, `FAILED`, `REFUNDED`

### Detalle de un pedido
```bash
curl -X GET "http://localhost:6070/ms-store/api/v1/admin/orders/1" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

### Actualizar estado del pedido
```bash
curl -X PUT "http://localhost:6070/ms-store/api/v1/admin/orders/1/status" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED",
    "paymentStatus": "PAID",
    "notes": "Pedido confirmado manualmente"
  }'
```

### Transacciones de pago de un pedido
```bash
curl -X GET "http://localhost:6070/ms-store/api/v1/admin/orders/1/payments" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

---

## 📦 Stock — `/ms-store/api/v1/admin/variants/{variantId}/stock`

### Actualizar stock de una variante
```bash
# Incrementar stock
curl -X PUT "http://localhost:6070/ms-store/api/v1/admin/variants/1/stock" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 20,
    "operation": "ADD",
    "notes": "Reposición de stock"
  }'

# Decrementar stock
curl -X PUT "http://localhost:6070/ms-store/api/v1/admin/variants/1/stock" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 5,
    "operation": "SUBTRACT",
    "notes": "Ajuste inventario"
  }'
```

> **Valores válidos de `operation`:** `ADD`, `SUBTRACT`, `SET`
