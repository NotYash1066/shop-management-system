#!/usr/bin/env bash
# seed.sh — Seeds a running Shop Management System instance with test data.
#
# Usage:
#   ./seed.sh [BASE_URL]
#
# Arguments:
#   BASE_URL   Optional. Defaults to http://localhost:8080
#
# What it does:
#   1. Registers a shop + admin user (bench / pass123)
#   2. Logs in and captures the JWT token
#   3. Creates sample categories, suppliers, employees, and products
#
# Requirements:
#   - curl
#   - jq  (https://stedolan.github.io/jq/)
#   - A running instance of the application

set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
API="$BASE_URL/api"

echo "Seeding $API ..."

# ── 1. Register shop + admin user ────────────────────────────────────────────
echo "Registering shop..."
curl -sf -X POST "$API/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"bench","password":"pass123","email":"bench@test.com","shopName":"Benchmark Shop"}' \
  || echo "(already registered, continuing)"

# ── 2. Login ─────────────────────────────────────────────────────────────────
echo "Logging in..."
TOKEN=$(curl -sf -X POST "$API/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"bench","password":"pass123"}' | jq -r '.accessToken')

AUTH="-H \"Authorization: Bearer $TOKEN\""
H='Content-Type: application/json'

echo "Token acquired."

# ── 3. Categories ─────────────────────────────────────────────────────────────
echo "Creating categories..."
CAT1=$(curl -sf -X POST "$API/categories" \
  -H "Authorization: Bearer $TOKEN" -H "$H" \
  -d '{"name":"Electronics"}' | jq -r '.id')

CAT2=$(curl -sf -X POST "$API/categories" \
  -H "Authorization: Bearer $TOKEN" -H "$H" \
  -d '{"name":"Accessories"}' | jq -r '.id')

# ── 4. Suppliers ──────────────────────────────────────────────────────────────
echo "Creating suppliers..."
SUP1=$(curl -sf -X POST "$API/suppliers" \
  -H "Authorization: Bearer $TOKEN" -H "$H" \
  -d '{"name":"Tech Supplies Inc.","contact":"tech@supplies.com"}' | jq -r '.id')

# ── 5. Employees ──────────────────────────────────────────────────────────────
echo "Creating employees..."
curl -sf -X POST "$API/employees" \
  -H "Authorization: Bearer $TOKEN" -H "$H" \
  -d '{"name":"Alice Johnson","role":"Manager","email":"alice@benchshop.com"}' > /dev/null

curl -sf -X POST "$API/employees" \
  -H "Authorization: Bearer $TOKEN" -H "$H" \
  -d '{"name":"Bob Smith","role":"Sales Associate","email":"bob@benchshop.com"}' > /dev/null

# ── 6. Products ───────────────────────────────────────────────────────────────
echo "Creating products..."
for i in $(seq 1 10); do
  curl -sf -X POST "$API/products" \
    -H "Authorization: Bearer $TOKEN" -H "$H" \
    -d "{\"name\":\"Product $i\",\"price\":$((i * 10)).99,\"sku\":\"SKU-$i\",\"stockQuantity\":1000,\"lowStockThreshold\":10,\"category\":{\"id\":$CAT1},\"supplier\":{\"id\":$SUP1}}" > /dev/null
done

for i in $(seq 11 15); do
  curl -sf -X POST "$API/products" \
    -H "Authorization: Bearer $TOKEN" -H "$H" \
    -d "{\"name\":\"Accessory $i\",\"price\":$((i * 5)).49,\"sku\":\"SKU-$i\",\"stockQuantity\":500,\"lowStockThreshold\":5,\"category\":{\"id\":$CAT2},\"supplier\":{\"id\":$SUP1}}" > /dev/null
done

echo ""
echo "Seed complete. 2 categories, 1 supplier, 2 employees, 15 products created."
echo "Use token for further requests: $TOKEN"
