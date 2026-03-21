import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');

export let options = {
  stages: [
    { duration: '30s', target: 100 },  // ramp up
    { duration: '60s', target: 100 },  // hold at 100 VUs
    { duration: '30s', target: 0   },  // ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<300'],   // PRD: p95 < 300ms
    http_req_failed:   ['rate<0.01'],   // < 1% errors
    errors:            ['rate<0.01'],
  },
};

const BASE    = __ENV.BASE_URL || 'http://localhost:8080';
const TOKEN   = __ENV.JWT_TOKEN;
const HEADERS = {
  Authorization: `Bearer ${TOKEN}`,
  'Content-Type': 'application/json',
  'X-Tenant-ID': __ENV.TENANT_ID || 'hospital_a',
};

export default function () {
  // Patient retrieval
  let r1 = http.get(`${BASE}/api/patients`, { headers: HEADERS });
  let success1 = check(r1, { 'patients 200': r => r.status === 200 });
  if (!success1) errorRate.add(1);

  // Bed occupancy analytics
  let r2 = http.get(
    `${BASE}/api/analytics/bed-occupancy`,
    { headers: HEADERS });
  let success2 = check(r2, { 'analytics 200': r => r.status === 200 });
  if (!success2) errorRate.add(1);

  sleep(0.3);
}
