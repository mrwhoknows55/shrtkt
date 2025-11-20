#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
API_KEY=${API_KEY:-"sk_test_alice"}
BASE_URL=${BASE_URL:-"http://0.0.0.0:8080"}
ITERATIONS=${ITERATIONS:-100}

echo "=========================================="
echo "Cache Performance Benchmark"
echo "=========================================="
echo ""

echo "Step 1: Testing WITHOUT cache (100 database calls)..."
echo "Note: Set ENABLE_CACHE=false in URLShortener.kt and rebuild"
echo ""

read -p "Press Enter after setting ENABLE_CACHE=false and rebuilding..."

k6 run --vus 1 --iterations $ITERATIONS \
  --env API_KEY="$API_KEY" \
  --env BASE_URL="$BASE_URL" \
  --env ENABLE_CACHE=false \
  "$SCRIPT_DIR/benchmark-cache.js" > /tmp/cache-benchmark-no-cache.txt 2>&1

echo ""
echo "Step 2: Testing WITH cache (1 database call, 99 cache hits)..."
echo "Note: Set ENABLE_CACHE=true in URLShortener.kt and rebuild"
echo ""

read -p "Press Enter after setting ENABLE_CACHE=true and rebuilding..."

k6 run --vus 1 --iterations $ITERATIONS \
  --env API_KEY="$API_KEY" \
  --env BASE_URL="$BASE_URL" \
  --env ENABLE_CACHE=true \
  "$SCRIPT_DIR/benchmark-cache.js" > /tmp/cache-benchmark-with-cache.txt 2>&1

echo ""
echo "=========================================="
echo "Results Comparison"
echo "=========================================="
echo ""
echo "--- WITHOUT Cache ---"
cat /tmp/cache-benchmark-no-cache.txt | grep -A 20 "Cache Performance Test Results" || cat /tmp/cache-benchmark-no-cache.txt | tail -20
echo ""
echo "--- WITH Cache ---"
cat /tmp/cache-benchmark-with-cache.txt | grep -A 20 "Cache Performance Test Results" || cat /tmp/cache-benchmark-with-cache.txt | tail -20

