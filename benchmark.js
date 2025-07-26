import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(50)', 'p(90)', 'p(95)', 'p(99)'],
  thresholds: {
    http_req_duration: ['p(99)<30000']
  },
};

export function setup() {
  console.log('Starting URL shortener load test...');
}

export default function () {
  const baseUrl = 'http://0.0.0.0:8080';

  const targetLoc = `https://avdt.xyz/page?query=${Date.now()}-${__ITER}`;

  const shortenResponse = http.post(`${baseUrl}/shorten`, targetLoc, {
    headers: {
      'Content-Type': 'text/plain',
    },
    tags: { name: 'shorten_url' },
  });

  let shortCode;
  try {
    const responseJson = JSON.parse(shortenResponse.body);
    shortCode = responseJson.shortCode;
  } catch (e) {
    console.error(`Failed to parse shorten response: ${shortenResponse.body}`);
    return;
  }

  check(shortCode, {
    'shortCode is not null': (code) => code !== null && code !== undefined,
    'shortCode is not empty': (code) => code && code >= 0,
  });

  if (!shortCode) {
    console.error('No shortCode received from shorten endpoint');
    return;
  }

  const redirectResponse = http.get(`${baseUrl}/redirect?code=${shortCode}`, {
    redirects: 0,
    tags: { name: 'redirect_url' },
  });

  sleep(1);
}

export function teardown(data) {
  console.log('Load test completed.');
}