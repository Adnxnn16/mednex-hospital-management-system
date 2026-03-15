import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 }, // ramp up
    { duration: '1m', target: 50 },  // stay
    { duration: '30s', target: 0 },  // ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must be below 500ms
  },
};

export default function () {
  const res = http.get('http://localhost:8081/api/patients', {
    headers: { 'X-Tenant': 'tenant-2' }
  });
  check(res, { 'status is 200': (r) => r.status === 200 });
  sleep(0.5);
}
