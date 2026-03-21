# MedNex Load Tests

## Prerequisites
Install k6: https://k6.io/docs/getting-started/installation/

## Get a JWT token first

```bash
export JWT_TOKEN=$(curl -s -X POST \
  "http://localhost:8081/realms/mednex/protocol/openid-connect/token" \
  -d "client_id=mednex-frontend" \
  -d "username=admin@hospital_a.com" \
  -d "password=admin" \
  -d "grant_type=password" | jq -r .access_token)
```

## Run smoke test (10 users, 30 seconds)

```bash
k6 run smoke.js
```

## Run full load test (100 users — PRD target)

```bash
k6 run load.js
```

## PRD pass criteria
- p(95) response time < 300ms
- Error rate < 1%
