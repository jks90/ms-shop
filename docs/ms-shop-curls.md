# MS-SHOP - cURL collection

## Variables base

```bash
BASE_URL="http://localhost:6004/ms-store"
TOKEN="<JWT_USER_OR_ADMIN>"
ADMIN_TOKEN="<JWT_ADMIN>"
IDEMPOTENCY_KEY="checkout-$(date +%s)"
```

---

## OpenAPI / Swagger

```bash
curl -i "$BASE_URL/swagger-ui/index.html"
curl -i "$BASE_URL/api-docs"
```

---

## Public catalog (`/api/v1/public`)

### Get categories

```bash
curl -sS "$BASE_URL/api/v1/public/categories"
```

### Get products

```bash
curl -sS "$BASE_URL/api/v1/public/products?page=0&size=10&activeOnly=true"
```

### Get products (with filters)

```bash
curl -sS "$BASE_URL/api/v1/public/products?categorySlug=zapatos&search=latino&page=0&size=10&activeOnly=true"
```

### Get product detail

```bash
curl -sS "$BASE_URL/api/v1/public/products/1"
```

---

## Public authentication (`/api/v1/user`)

### Signup

```bash
curl -sS -X POST "$BASE_URL/api/v1/user/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "testuser@mail.com",
    "password": "12345678"
  }'
```

### Login (username)

```bash
curl -sS -X POST "$BASE_URL/api/v1/user/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "12345678"
  }'
```

### Login (email)

```bash
curl -sS -X POST "$BASE_URL/api/v1/user/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@mail.com",
    "password": "12345678"
  }'
```

### Confirm account

```bash
curl -sS "$BASE_URL/api/v1/user/confirm?token=<BASE64_TOKEN>"
```

---

## User self (library-users)

### Get self profile

```bash
curl -sS "$BASE_URL/api/v1/user/self" \
  -H "Authorization: Bearer $TOKEN"
```

### Update self profile

```bash
curl -sS -X PUT "$BASE_URL/api/v1/user/self" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test",
    "surname": "User",
    "phone": "600000000",
    "detailEmail": "testuser.updated@mail.com"
  }'
```

---

## Customer self - ms-shop

## Addresses (`/api/v1/customers/self/addresses`)

### List addresses

```bash
curl -sS "$BASE_URL/api/v1/customers/self/addresses" \
  -H "Authorization: Bearer $TOKEN"
```

### Create address

```bash
curl -sS -X POST "$BASE_URL/api/v1/customers/self/addresses" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Casa",
    "recipientName": "Test User",
    "phone": "600000000",
    "line1": "Calle Falsa 123",
    "line2": "Piso 2",
    "city": "Madrid",
    "state": "Madrid",
    "postalCode": "28001",
    "country": "ES",
    "isDefault": true
  }'
```

### Update address

```bash
curl -sS -X PUT "$BASE_URL/api/v1/customers/self/addresses/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Casa Actualizada",
    "recipientName": "Test User",
    "phone": "600000001",
    "line1": "Calle Nueva 456",
    "city": "Madrid",
    "state": "Madrid",
    "postalCode": "28002",
    "country": "ES",
    "isDefault": false
  }'
```

### Delete address

```bash
curl -sS -X DELETE "$BASE_URL/api/v1/customers/self/addresses/1" \
  -H "Authorization: Bearer $TOKEN"
```

## Cart (`/api/v1/customers/self/cart`)

### Get cart

```bash
curl -sS "$BASE_URL/api/v1/customers/self/cart" \
  -H "Authorization: Bearer $TOKEN"
```

### Add item

```bash
curl -sS -X POST "$BASE_URL/api/v1/customers/self/cart/items" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "variantId": 1,
    "quantity": 1
  }'
```

### Update item

```bash
curl -sS -X PUT "$BASE_URL/api/v1/customers/self/cart/items/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 2
  }'
```

### Remove item

```bash
curl -sS -X DELETE "$BASE_URL/api/v1/customers/self/cart/items/1" \
  -H "Authorization: Bearer $TOKEN"
```

### Clear cart

```bash
curl -sS -X DELETE "$BASE_URL/api/v1/customers/self/cart" \
  -H "Authorization: Bearer $TOKEN"
```

## Customer profile (`/api/v1/customers/self`)

### Get customer profile

```bash
curl -sS "$BASE_URL/api/v1/customers/self" \
  -H "Authorization: Bearer $TOKEN"
```

## Checkout (`/api/v1/customers/self/checkout`)

### Create checkout

```bash
curl -sS -X POST "$BASE_URL/api/v1/customers/self/checkout" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: $IDEMPOTENCY_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "shippingAddressId": 1,
    "billingAddressId": 1,
    "successUrl": "https://example.com/success",
    "cancelUrl": "https://example.com/cancel",
    "notes": "Pedido de prueba",
    "paymentMode": "CHECKOUT_SESSION"
  }'
```

## Orders (`/api/v1/customers/self/orders`)

### List orders

```bash
curl -sS "$BASE_URL/api/v1/customers/self/orders?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

### Get order detail

```bash
curl -sS "$BASE_URL/api/v1/customers/self/orders/1" \
  -H "Authorization: Bearer $TOKEN"
```

### Get order payment status

```bash
curl -sS "$BASE_URL/api/v1/customers/self/orders/1/payment-status" \
  -H "Authorization: Bearer $TOKEN"
```

### Cancel order

```bash
curl -sS -X POST "$BASE_URL/api/v1/customers/self/orders/1/cancel" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Admin users (library-users)

### List users

```bash
curl -sS "$BASE_URL/api/v1/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Create user

```bash
curl -sS -X POST "$BASE_URL/api/v1/user" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin_created_user",
    "password": "12345678",
    "name": "Created",
    "surname": "ByAdmin",
    "phone": "699000000",
    "detailEmail": "admin_created_user@mail.com"
  }'
```

### Get user by id

```bash
curl -sS "$BASE_URL/api/v1/user/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Update user by id

```bash
curl -sS -X PUT "$BASE_URL/api/v1/user/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated",
    "surname": "ByAdmin",
    "phone": "688111111",
    "detailEmail": "updated_by_admin@mail.com"
  }'
```

### Delete user by id

```bash
curl -sS -X DELETE "$BASE_URL/api/v1/user/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## Admin catalog / inventory / orders

## Categories (`/api/v1/admin/categories`)

### List categories

```bash
curl -sS "$BASE_URL/api/v1/admin/categories" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Create category

```bash
curl -sS -X POST "$BASE_URL/api/v1/admin/categories" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Zapatos",
    "slug": "zapatos",
    "description": "Calzado de baile",
    "isActive": true
  }'
```

### Update category

```bash
curl -sS -X PUT "$BASE_URL/api/v1/admin/categories/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Zapatos Pro",
    "slug": "zapatos-pro",
    "description": "Calzado de baile premium",
    "isActive": true
  }'
```

### Delete category

```bash
curl -sS -X DELETE "$BASE_URL/api/v1/admin/categories/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## Products (`/api/v1/admin/products`)

### List products

```bash
curl -sS "$BASE_URL/api/v1/admin/products?page=0&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Create product

```bash
curl -sS -X POST "$BASE_URL/api/v1/admin/products" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Zapato Salsa",
    "slug": "zapato-salsa",
    "description": "Zapato para salsa",
    "categoryId": 1,
    "status": "ACTIVE"
  }'
```

### Update product

```bash
curl -sS -X PUT "$BASE_URL/api/v1/admin/products/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Zapato Salsa v2",
    "slug": "zapato-salsa-v2",
    "description": "Versión actualizada",
    "categoryId": 1,
    "status": "ACTIVE"
  }'
```

### Delete product

```bash
curl -sS -X DELETE "$BASE_URL/api/v1/admin/products/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Create product variant

```bash
curl -sS -X POST "$BASE_URL/api/v1/admin/products/1/variants" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "SALSA-001-38",
    "name": "Talla 38",
    "priceAmount": 7900,
    "currency": "EUR",
    "stockAvailable": 10,
    "isActive": true
  }'
```

## Stock (`/api/v1/admin/stock`)

### Update variant stock

```bash
curl -sS -X PATCH "$BASE_URL/api/v1/admin/stock/variants/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 5,
    "reason": "MANUAL_ADJUSTMENT"
  }'
```

## Orders (`/api/v1/admin/orders`)

### List orders

```bash
curl -sS "$BASE_URL/api/v1/admin/orders?page=0&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### List orders with filters

```bash
curl -sS "$BASE_URL/api/v1/admin/orders?status=PENDING&paymentStatus=PENDING&page=0&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Get order detail

```bash
curl -sS "$BASE_URL/api/v1/admin/orders/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Get order transactions

```bash
curl -sS "$BASE_URL/api/v1/admin/orders/1/transactions" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Update order status

```bash
curl -sS -X PATCH "$BASE_URL/api/v1/admin/orders/1/status" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED"
  }'
```

---

## Stripe webhook (`/api/v1/payments/stripe/webhook`)

### Receive Stripe webhook

```bash
curl -sS -X POST "$BASE_URL/api/v1/payments/stripe/webhook" \
  -H "Stripe-Signature: t=12345,v1=fake_signature" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "evt_test_webhook",
    "type": "checkout.session.completed",
    "data": {
      "object": {
        "id": "cs_test_123"
      }
    }
  }'
```
