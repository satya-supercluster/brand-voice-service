# Quick Start Guide - 5 Minutes to Running Service

## Prerequisites Check
```bash
# Check if you have Docker installed
docker --version

# Check if you have Docker Compose installed
docker-compose --version
```

If you don't have Docker, download from: https://www.docker.com/products/docker-desktop

## Step 1: Get the Code (30 seconds)

```bash
# Create project directory
mkdir brand-voice-service
cd brand-voice-service

# Create all the files from the artifacts provided
# (Copy each artifact into its appropriate location)
```

## Step 2: Start Everything with Docker (2 minutes)

```bash
# Start all services
docker-compose up -d

# Wait for services to be healthy (check every 10 seconds)
docker-compose ps
```

You should see:
```
NAME                       STATUS
brandvoice-postgres        Up (healthy)
brandvoice-redis           Up (healthy)
brandvoice-nlp             Up (healthy)
brandvoice-service         Up (healthy)
brandvoice-gateway         Up (healthy)
```

## Step 3: Test the Service (2 minutes)

### Test 1: Health Check
```bash
curl http://localhost:3000/health
```

Expected output:
```json
{"status":"healthy","service":"api-gateway","timestamp":"2025-01-28T..."}
```

### Test 2: Create a Brand Profile
```bash
curl -X POST http://localhost:3000/api/v1/brand-voice/profiles \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "demo-customer",
    "brandName": "Demo Company",
    "sampleContent": "We are a professional enterprise technology company focused on delivering innovative solutions. Our strategic approach ensures optimal results for our clients. We believe in excellence, efficiency, and partnership."
  }'
```

Expected output:
```json
{
  "profileId": "some-uuid",
  "customerId": "demo-customer",
  "brandName": "Demo Company",
  "voiceAttributes": {
    "tone": "professional",
    "formality": 0.85,
    ...
  },
  "status": "active"
}
```

### Test 3: Validate On-Brand Content
```bash
curl -X POST http://localhost:3000/api/v1/brand-voice/validate \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "demo-customer",
    "content": "Our professional team delivers innovative enterprise solutions with strategic focus."
  }'
```

Expected output:
```json
{
  "consistencyScore": 88.5,
  "verdict": "on_brand",
  "issues": [],
  ...
}
```

### Test 4: Validate Off-Brand Content
```bash
curl -X POST http://localhost:3000/api/v1/brand-voice/validate \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "demo-customer",
    "content": "Hey folks! Check out our super cool awesome stuff!"
  }'
```

Expected output:
```json
{
  "consistencyScore": 65.2,
  "verdict": "minor_issues",
  "issues": [
    {
      "type": "tone",
      "severity": "medium",
      "description": "The tone doesn't match your brand voice",
      "suggestion": "Try using a more professional tone"
    }
  ],
  ...
}
```

## 🎉 Success!

You now have:
✅ All services running  
✅ Database and cache working  
✅ API responding to requests  
✅ Real-time content validation

## Next Steps

1. **Try the Postman Collection**
    - Import `Brand Voice Service.postman_collection.json`
    - Test all endpoints visually

2. **View the Logs**
   ```bash
   # All services
   docker-compose logs -f
   
   # Just one service
   docker-compose logs -f brand-voice-service
   ```

3. **Access the Database**
   ```bash
   docker-compose exec postgres psql -U postgres -d brandvoice
   
   # List all profiles
   SELECT customer_id, brand_name, created_at FROM brand_profiles;
   ```

4. **Check Redis Cache**
   ```bash
   docker-compose exec redis redis-cli
   
   # List all keys
   KEYS *
   
   # Check a cached profile
   GET brandProfiles::demo-customer
   ```

## Troubleshooting

### Services won't start?
```bash
# Check if ports are in use
lsof -i :3000  # API Gateway
lsof -i :8080  # Brand Voice Service
lsof -i :8001  # NLP Analyzer
lsof -i :5432  # PostgreSQL
lsof -i :6379  # Redis

# Kill processes on those ports if needed
kill -9 <PID>
```

### Services started but not healthy?
```bash
# Check individual service logs
docker-compose logs brand-voice-service
docker-compose logs nlp-analyzer

# Restart a specific service
docker-compose restart brand-voice-service
```

### Database connection errors?
```bash
# Check if PostgreSQL is ready
docker-compose exec postgres pg_isready

# Recreate the database
docker-compose down
docker volume rm brandvoice_postgres_data
docker-compose up -d
```

## Stop Everything

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (fresh start)
docker-compose down -v
```

## Development Mode (Without Docker)

If you want to run services locally without Docker:

### 1. Start Infrastructure
```bash
docker-compose up -d postgres redis
```

### 2. Start NLP Analyzer
```bash
cd nlp-analyzer
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --reload --port 8001
```

### 3. Start Brand Voice Service
```bash
# In a new terminal
mvn spring-boot:run
```

### 4. Start API Gateway
```bash
# In a new terminal
cd api-gateway
npm install
npm run dev
```

## Using the Makefile (Optional)

If you have `make` installed:

```bash
# See all available commands
make help

# Start with Docker
make docker-up

# Check health
make health-check

# View logs
make docker-logs

# Stop everything
make docker-down
```

## URLs Reference

- **API Gateway**: http://localhost:3000
- **Brand Voice Service**: http://localhost:8080
- **NLP Analyzer**: http://localhost:8001
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379

## API Endpoints Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| POST | `/api/v1/brand-voice/profiles` | Create profile |
| GET | `/api/v1/brand-voice/profiles/{id}` | Get profile |
| POST | `/api/v1/brand-voice/validate` | Validate content |
| DELETE | `/api/v1/brand-voice/profiles/{id}` | Delete profile |

## Performance Targets

✅ Health check response: < 50ms  
✅ Profile creation: < 2s  
✅ Content validation: < 500ms  
✅ Cache hit rate: > 80%

---

**You're all set!** The service is running and ready for testing. 🚀