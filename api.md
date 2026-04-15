# API — ms-shop (Endpoints de Cliente y Públicos)

> **Base URL:** `http://localhost:6070`  
> Los endpoints autenticados requieren: `-H "Authorization: Bearer <TOKEN>"`

---

## 🌐 Catálogo Público — `/ms-store/api/v1/public`

### Listar categorías activas
```bash
curl -X GET "http://localhost:6070/ms-store/api/v1/public/categories" \
  -H "Accept: application/json"
```

### Listar productos (con filtros opcionales)
```bash
# Sin filtros
curl -X GET "http://localhost:6070/ms-store/api/v1/public/products?page=0&size=20" \
  -H "Accept: application/json"

# Por categoría (slug)
curl -X GET "http://localhost:6070/ms-store/api/v1/public/products?categorySlug=electronica&page=0&size=20" \
  -H "Accept: application/json"

# Por texto libre
curl -X GET "http://localhost:6070/ms-store/api/v1/public/products?search=auricular&page=0&size=20" \
  -H "Accept: application/json"

# Solo en stock
curl -X GET "http://localhost:6070/ms-store/api/v1/public/products?activeOnly=true&page=0&size=20" \
  -H "Accept: application/json"
```

### Detalle de producto
```bash
curl -X GET "http://localhost:6070/ms-store/api/v1/public/products/1" \
  -H "Accept: application/json"
```

---

## 📍 Direcciones — `/ms-store/api/v1/customers/self/addresses`

### Listar mis direcciones
```bash
curl -X GET "http://localhost:6070/ms-store/api/v1/customers/self/addresses" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

### Crear dirección
```bash
curl -X POST "http://localhost:6070/ms-store/api/v1/customers/self/addresses" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "recipientName": "Juan García",
    "line1": "Calle Mayor 10",
    "line2": "2ºA",
    "postalCode": "28001",
    "city": "Madrid",
    "stateRegion": "Madrid",
    "countryCode": "ES",
    "phone": "+34600000000",
    "type": "SHIPPING",
    "isDefault": true
  }'
```

### Actualizar dirección
```bash
curl -X PUT "http://localhost:6070/ms-store/api/v1/customers/self/addresses/1" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "recipientName": "Juan García López",
    "line1": "Calle Mayor 10",
    "postalCode": "28001",
    "city": "Madrid",
    "stateRegion": "Madrid",
    "countryCode": "ES",
    "phone": "+34600000000",
    "type": "SHIPPING",
    "isDefault": false
  }'
```

### Eliminar dirección
```bash
curl -X DELETE "http://localhost:6070/ms-store/api/v1/customers/self/addresses/1" \
  -H "Authorization: Bearer <TOKEN>"
```

> **Valores válidos de `type`:** `SHIPPING`, `BILLING`

---

## 🛒 Carrito — `/ms-store/api/v1/customers/self/cart`

### Ver carrito
```bash
curl -X GET "http://localhost:6070/ms-store/api/v1/customers/self/cart" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

### Añadir producto al carrito
```bash
curl -X POST "http://localhost:6070/ms-store/api/v1/customers/self/cart/items" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "variantId": 1,
    "quantity": 2
  }'
```

### Actualizar cantidad de un item
```bash
curl -X PUT "http://localhost:6070/ms-store/api/v1/customers/self/cart/items/1" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 3
  }'
```

### Eliminar item del carrito
```bash
curl -X DELETE "http://localhost:6070/ms-store/api/v1/customers/self/cart/items/1" \
  -H "Authorization: Bearer <TOKEN>"
```

### Vaciar carrito
```bash
curl -X DELETE "http://localhost:6070/ms-store/api/v1/customers/self/cart" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 📋 Pedidos — `/ms-store/api/v1/customers/self/orders`

### Listar mis pedidos
```bash
curl -X GET "http://localhost:6070/ms-store/api/v1/customers/self/orders?page=0&size=20" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

### Detalle de un pedido
```bash
curl -X GET "http://localhost:6070/ms-store/api/v1/customers/self/orders/1" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

### Estado de pago de un pedido
```bash
curl -X GET "http://localhost:6070/ms-store/api/v1/customers/self/orders/1/payment-status" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

### Cancelar un pedido
```bash
curl -X POST "http://localhost:6070/ms-store/api/v1/customers/self/orders/1/cancel" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

---

## 💳 Checkout — `/ms-store/api/v1/customers/self/checkout`

### Crear checkout (genera sesión de pago con Stripe)
```bash
curl -X POST "http://localhost:6070/ms-store/api/v1/customers/self/checkout" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: uuid-unico-por-intento" \
  -d '{
    "shippingAddressId": 1,
    "billingAddressId": 1,
    "paymentMode": "CHECKOUT_SESSION",
    "successUrl": "https://mitienda.com/checkout/success",
    "cancelUrl": "https://mitienda.com/checkout/cancel",
    "notes": "Por favor no doblar"
  }'
```

> ⚠️ El header `Idempotency-Key` es **obligatorio** y debe ser único por intento (UUID recomendado).  
> **Valores válidos de `paymentMode`:** `CHECKOUT_SESSION`, `PAYMENT_INTENT`

---

## 👤 Perfil de cliente — `/ms-store/api/v1/customers/self`

### Obtener perfil propio
```bash
curl -X GET "http://localhost:6070/ms-store/api/v1/customers/self" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

---

## 🔔 Webhook Stripe — `/ms-store/api/v1/payments/stripe/webhook`

> ⚠️ Este endpoint es llamado por Stripe, **no por el cliente**.

```bash
# Simulación local con Stripe CLI
stripe trigger payment_intent.succeeded

# O manualmente:
curl -X POST "http://localhost:6070/ms-store/api/v1/payments/stripe/webhook" \
  -H "Stripe-Signature: t=...,v1=..." \
  -H "Content-Type: application/json" \
  -d '{ ...payload de Stripe... }'
```
