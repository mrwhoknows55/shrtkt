import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Trend } from "k6/metrics";

const cacheHitCounter = new Counter("cache_hits");
const cacheMissCounter = new Counter("cache_misses");
const redirectDuration = new Trend("redirect_duration");

export const options = {
  summaryTrendStats: [
    "avg",
    "min",
    "med",
    "max",
    "p(50)",
    "p(90)",
    "p(95)",
    "p(99)",
  ],
  thresholds: {
    http_req_duration: ["p(99)<30000"],
    redirect_duration: ["p(99)<30000"],
  },
};

const API_KEY = __ENV.API_KEY || "sk_test_alice";
const BASE_URL = __ENV.BASE_URL || "http://0.0.0.0:8080";
const ENABLE_CACHE = __ENV.ENABLE_CACHE !== "false";

export function setup() {
  console.log(
    `Starting cache performance test with cache ${
      ENABLE_CACHE ? "ENABLED" : "DISABLED"
    }...`
  );

  const targetLoc = `https://avdt.xyz/benchmark-${Date.now()}`;
  
  console.log(`Creating short URL for: ${targetLoc}`);

  const formBody = `url=${encodeURIComponent(targetLoc)}`;
  console.log(`Form body: ${formBody}`);

  const shortenResponse = http.post(
    `${BASE_URL}/shorten`,
    formBody,
    {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
        "x-api-key": API_KEY,
      },
      tags: { name: "shorten_url" },
    }
  );

  if (shortenResponse.status !== 201) {
    console.error(`Shorten request failed with status ${shortenResponse.status}`);
    console.error(`Response body: ${shortenResponse.body}`);
    console.error(`Response headers:`, JSON.stringify(shortenResponse.headers, null, 2));
    throw new Error(`Failed to create short URL: ${shortenResponse.status} - ${shortenResponse.body}`);
  }

  check(shortenResponse, {
    "shorten status is 201": (r) => r.status === 201,
  });

  let shortCode;
  try {
    const responseJson = JSON.parse(shortenResponse.body);
    shortCode = responseJson.shortCode;
  } catch (e) {
    console.error(`Failed to parse shorten response. Status: ${shortenResponse.status}`);
    console.error(`Response body: ${shortenResponse.body}`);
    console.error(`Parse error: ${e.message}`);
    throw new Error("Failed to create short URL");
  }

  if (!shortCode) {
    console.error(`No shortCode in response. Body: ${shortenResponse.body}`);
    throw new Error("No shortCode received from shorten endpoint");
  }

  console.log(`Created short code: ${shortCode}`);
  return { shortCode, targetLoc };
}

export default function (data) {
  const { shortCode } = data;

  const startTime = Date.now();
  const redirectResponse = http.get(`${BASE_URL}/redirect?code=${shortCode}`, {
    redirects: 0,
    tags: { name: "redirect_url" },
  });
  const duration = Date.now() - startTime;

  redirectDuration.add(duration);

  const cacheStatus = redirectResponse.headers["Cache-Status"];

  if (cacheStatus === "hit") {
    cacheHitCounter.add(1);
  } else if (cacheStatus === "miss") {
    cacheMissCounter.add(1);
  }

  check(redirectResponse, {
    "redirect status is 302": (r) => r.status === 302,
    "has Cache-Status header": (r) => r.headers["Cache-Status"] !== undefined,
  });
}

export function handleSummary(data) {
  const cacheHits = data.metrics.cache_hits
    ? data.metrics.cache_hits.values.count
    : 0;
  const cacheMisses = data.metrics.cache_misses
    ? data.metrics.cache_misses.values.count
    : 0;
  const totalRequests = cacheHits + cacheMisses;
  const hitRatio =
    totalRequests > 0 ? ((cacheHits / totalRequests) * 100).toFixed(2) : 0;
  const missRatio =
    totalRequests > 0 ? ((cacheMisses / totalRequests) * 100).toFixed(2) : 0;

  const avgDuration = data.metrics.redirect_duration
    ? data.metrics.redirect_duration.values.avg
    : 0;
  const minDuration = data.metrics.redirect_duration
    ? data.metrics.redirect_duration.values.min
    : 0;
  const maxDuration = data.metrics.redirect_duration
    ? data.metrics.redirect_duration.values.max
    : 0;
  const p95Duration = data.metrics.redirect_duration
    ? data.metrics.redirect_duration.values["p(95)"]
    : 0;
  const p99Duration = data.metrics.redirect_duration
    ? data.metrics.redirect_duration.values["p(99)"]
    : 0;

  return {
    stdout: `
Cache Performance Test Results
Cache Status: ${ENABLE_CACHE ? "ENABLED" : "DISABLED"}
Total Requests: ${totalRequests}
Cache Hits: ${cacheHits}
Cache Misses: ${cacheMisses}
Hit Ratio: ${hitRatio}%
Miss Ratio: ${missRatio}%

Performance Metrics:
  Average Duration: ${avgDuration.toFixed(2)}ms
  Min Duration: ${minDuration.toFixed(2)}ms
  Max Duration: ${maxDuration.toFixed(2)}ms
  P95 Duration: ${p95Duration.toFixed(2)}ms
  P99 Duration: ${p99Duration.toFixed(2)}ms
    `,
  };
}
