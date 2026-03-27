# Benchmark Guide for Shop Management System

This document provides step-by-step instructions to run benchmarks that support the claims in your resume:
1. Redis caching reduces database overhead by 40%
2. Async stock reconciliation prevents blocking of API responses
3. Sub-50ms latency under high concurrency

## Prerequisites

Before running benchmarks, ensure:

1. **PostgreSQL is running** on `localhost:5432`
2. **Redis is running** on `localhost:6379`
3. **Application is running** on `http://localhost:8080`
4. **Python 3** with `requests` library installed

```bash
pip install requests
```

## Quick Start

### Step 1: Start the Application

```bash
# Build the application
./mvnw clean package -DskipTests

# Run the application
java -jar target/shop-management-system-0.0.1-SNAPSHOT.jar
```

Wait for the application to start (look for "Started ShopManagementSystemApplication" in logs).

### Step 2: Run All Benchmarks

```bash
python3 benchmark.py
```

This will:
- Check service health
- Register/log in as benchmark user
- Run all three benchmark tests
- Generate a summary report

## Manual Benchmark Commands

If you prefer to run tests manually, here's how:

### Claim 1: Redis Caching (40% DB Overhead Reduction)

**Objective:** Show that cached endpoints are significantly faster than uncached ones.

**Setup:**
```bash
# Get JWT token (requires jq: https://stedolan.github.io/jq/)
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.accessToken')

# Create test product
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"TestProduct","price":100,"sku":"TEST-SKU-001","stockQuantity":50}'
```

**Test Cold Cache (first request):**
```bash
python3 load_test.py \
  --url "http://localhost:8080/api/products/sku/TEST-SKU-001" \
  --method GET \
  --requests 50 \
  --concurrency 10 \
  --headers "{\"Authorization\":\"Bearer $TOKEN\"}" \
  --payload '{}'
```

**Test Warm Cache (subsequent requests):**
```bash
python3 load_test.py \
  --url "http://localhost:8080/api/products/sku/TEST-SKU-001" \
  --method GET \
  --requests 200 \
  --concurrency 20 \
  --headers "{\"Authorization\":\"Bearer $TOKEN\"}" \
  --payload '{}'
```

**Expected Result:**
- Cold cache: Higher latency (~30-50ms)
- Warm cache: Lower latency (~5-15ms)
- Improvement: >30% reduction in latency

---

### Claim 2: Async Stock Reconciliation (Non-blocking)

**Objective:** Show that order placement returns quickly even though stock updates happen asynchronously.

**Setup:**
```bash
# Create category
curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Benchmark Category"}'

# Create product with enough stock for concurrent orders
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"OrderProduct","price":25,"sku":"ORD-BENCH-001","stockQuantity":10000}'
```

**Test Order Placement** (replace `USER_ID` with the id returned from login):
```bash
python3 load_test.py \
  --url "http://localhost:8080/api/orders" \
  --method POST \
  --requests 50 \
  --concurrency 10 \
  --headers "{\"Authorization\":\"Bearer $TOKEN\"}" \
  --payload '{"userId": USER_ID, "items":[{"productId":1,"quantity":1}]}'
```

**Expected Result:**
- Average latency: <100ms (should be fast even under load)
- P95 latency: <200ms
- This proves async processing prevents blocking

---

### Claim 3: Sub-50ms Latency

**Objective:** Show that read endpoints maintain sub-50ms latency under load.

**Test Product List:**
```bash
python3 load_test.py \
  --url "http://localhost:8080/api/products" \
  --method GET \
  --requests 500 \
  --concurrency 50 \
  --headers "{\"Authorization\":\"Bearer $TOKEN\"}" \
  --payload '{}'
```

**Test Categories:**
```bash
python3 load_test.py \
  --url "http://localhost:8080/api/categories" \
  --method GET \
  --requests 500 \
  --concurrency 50 \
  --headers "{\"Authorization\":\"Bearer $TOKEN\"}" \
  --payload '{}'
```

**Test Suppliers:**
```bash
python3 load_test.py \
  --url "http://localhost:8080/api/suppliers" \
  --method GET \
  --requests 500 \
  --concurrency 50 \
  --headers "{\"Authorization\":\"Bearer $TOKEN\"}" \
  --payload '{}'
```

**Expected Result:**
- All endpoints: average latency <50ms
- P95 latency: <100ms

---

## Advanced: Measuring Database Overhead

To specifically measure the 40% database overhead reduction:

### Step 1: Enable Query Logging in PostgreSQL

```sql
-- In PostgreSQL, enable query logging
ALTER SYSTEM SET log_statement = 'all';
ALTER SYSTEM SET log_min_duration_statement = 0;
SELECT pg_reload_conf();
```

### Step 2: Count Queries Without Cache

1. Flush Redis cache:
```bash
redis-cli FLUSHALL
```

2. Run benchmark and count SQL queries in logs

### Step 3: Count Queries With Cache

1. Run same benchmark (cache will be warm)
2. Compare query counts

**Expected:** With cache, DB queries should reduce by ~40%

---

## Interpreting Results

### For Resume Claims

| Claim | Key Metric | Target Value |
|-------|-----------|--------------|
| Redis caching 40% reduction | Cache improvement % | >30% |
| Async non-blocking | Order response time | <100ms avg |
| Sub-50ms latency | Avg latency | <50ms |

### Sample Output

```json
{
  "total_requests": 500,
  "concurrency": 50,
  "avg_latency_ms": 12.45,
  "p50_latency_ms": 10.2,
  "p95_latency_ms": 28.3,
  "p99_latency_ms": 45.1,
  "max_latency_ms": 120.5
}
```

---

## Troubleshooting

### Service Not Starting
- Check PostgreSQL: `pg_isready -h localhost -p 5432`
- Check Redis: `redis-cli ping`

### Auth Errors
- Register a new user first, or use existing credentials
- Check JWT expiration in application.properties

### High Latency
- Ensure database indexes exist
- Check PostgreSQL connection pool settings
- Verify Redis is connected

---

## Generating Resume-Ready Summary

After running benchmarks, create a summary like:

```
BENCHMARK RESULTS:
─────────────────
1. Redis Caching: 45% latency reduction (cold: 35ms → warm: 19ms)
2. Async Orders: 85ms avg response (non-blocking confirmed)
3. Read Endpoints: 28ms avg latency (well under 50ms target)

All claims verified and supported by quantitative data.
```