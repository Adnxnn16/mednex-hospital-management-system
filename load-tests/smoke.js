import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 10,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<300'],
    http_req_failed:   ['rate<0.01'],
  },
};

const BASE    = __ENV.BASE_URL || 'http://localhost:8080';
const TOKEN   = __ENV.JWT_TOKEN;
const HEADERS = {
  Authorization:  `Bearer ${TOKEN}`,
  'Content-Type': 'application/json',
  'X-Tenant-ID':  __ENV.TENANT_ID || 'hospital_a',
};

export default function () {
  const r1 = http.get(
    `${BASE}/api/patients`, { headers: HEADERS });
  check(r1, { 'patients 200': r => r.status === 200 });

  const r2 = http.get(
    `${BASE}/api/analytics/bed-occupancy`,
    { headers: HEADERS });
  check(r2, { 'analytics 200': r => r.status === 200 });

  sleep(0.5);
}
