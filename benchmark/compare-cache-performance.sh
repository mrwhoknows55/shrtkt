#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
API_KEY=${API_KEY:-"sk_test_alice"}
BASE_URL=${BASE_URL:-"http://0.0.0.0:8080"}
ITERATIONS=100

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║        Cache Performance Comparison Benchmark                ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""
echo "This script will test redirect performance with and without cache."
echo "You need to rebuild the application between tests."
echo ""
echo "Configuration:"
echo "  - API Key: $API_KEY"
echo "  - Base URL: $BASE_URL"
echo "  - Iterations: $ITERATIONS"
echo ""

echo "═══════════════════════════════════════════════════════════════"
echo "TEST 1: WITHOUT Cache (100 database calls)"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "1. Set ENABLE_CACHE = false in src/main/kotlin/xyz/avdt/routes/URLShortener.kt"
echo "2. Rebuild: ./gradlew build"
echo "3. Start the server"
echo ""
read -p "Press Enter when ready to run test WITHOUT cache..."

k6 run --vus 1 --iterations $ITERATIONS \
  --env API_KEY="$API_KEY" \
  --env BASE_URL="$BASE_URL" \
  --env ENABLE_CACHE=false \
  "$SCRIPT_DIR/benchmark-cache.js" 2>&1 | tee /tmp/cache-test-no-cache.txt

echo ""
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "TEST 2: WITH Cache (1 database call, 99 cache hits)"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "1. Set ENABLE_CACHE = true in src/main/kotlin/xyz/avdt/routes/URLShortener.kt"
echo "2. Rebuild: ./gradlew build"
echo "3. Restart the server"
echo ""
read -p "Press Enter when ready to run test WITH cache..."

k6 run --vus 1 --iterations $ITERATIONS \
  --env API_KEY="$API_KEY" \
  --env BASE_URL="$BASE_URL" \
  --env ENABLE_CACHE=true \
  "$SCRIPT_DIR/benchmark-cache.js" 2>&1 | tee /tmp/cache-test-with-cache.txt

echo ""
echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║                    COMPARISON RESULTS                        ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""
echo "--- WITHOUT Cache (100 DB calls) ---"
echo ""
grep -A 15 "Cache Performance Test Results" /tmp/cache-test-no-cache.txt || tail -15 /tmp/cache-test-no-cache.txt
echo ""
echo "--- WITH Cache (1 DB call, 99 cache hits) ---"
echo ""
grep -A 15 "Cache Performance Test Results" /tmp/cache-test-with-cache.txt || tail -15 /tmp/cache-test-with-cache.txt

