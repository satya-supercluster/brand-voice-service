# Brand Voice Consistency Service - Knowledge Transfer Document

## Table of Contents
1. [Service Overview](#service-overview)
2. [Architecture](#architecture)
3. [Setup & Installation](#setup--installation)
4. [API Documentation](#api-documentation)
5. [Database Schema](#database-schema)
6. [Deployment](#deployment)
7. [Monitoring & Troubleshooting](#monitoring--troubleshooting)
8. [Common Issues & Solutions](#common-issues--solutions)
9. [Maintenance Tasks](#maintenance-tasks)
10. [Future Enhancements](#future-enhancements)

---

## Service Overview

### Purpose
The Brand Voice Consistency Service validates AI-generated content against customer-specific brand voice profiles to ensure consistent tone, formality, and style across all content types.

### Key Features
- Real-time content validation (< 500ms)
- Automated brand voice profile creation
- Detailed consistency scoring (0-100)
- Actionable improvement suggestions
- Event-driven architecture for analytics

### Business Value
- Reduces manual content review time by 2-3 hours/week per customer
- Improves brand consistency by 30%
- Enables faster content publishing
- Differentiates Typeface from competitors

---

## Architecture

### System Components

#### 1. API Gateway (TypeScript/Express)
**Location:** `./api-gateway`  
**Port:** 3000  
**Purpose:** Public-facing API, rate limiting, request routing

**Key Files:**
- `src/index.ts` - Main server setup
- `src/middleware/` - Authentication, rate limiting
- `Dockerfile` - Container configuration

**Dependencies:**
- Express.js 4.18
- Helmet (security)
- Express-rate-limit

#### 2. Brand Voice Service (Java/Spring Boot)
**Location:** `./brand-voice-service`  
**Port:** 8080  
**Purpose:** Core business logic, profile management, validation

**Key Components:**
- `BrandVoiceController` - REST endpoints
- `BrandVoiceService` - Business logic
- `NLPAnalyzerService` - Python service integration
- `EventPublisherService` - Pub/Sub events
- `BrandProfileRepository` - Data access

**Dependencies:**
- Spring Boot 3.2
- PostgreSQL driver
- Redis (Lettuce)
- GCP Pub/Sub client
- WebClient for HTTP calls

#### 3. NLP Analyzer (Python/FastAPI)
**Location:** `./nlp-analyzer`  
**Port:** 8001  
**Purpose:** Advanced text analysis for voice characteristics

**Key Features:**
- Tone detection
- Formality calculation
- Vocabulary complexity analysis
- Sentence structure analysis

**Dependencies:**
- FastAPI
- Uvicorn
- Pydantic

#### 4. PostgreSQL Database
**Purpose:** Persistent storage for brand profiles
**Schema:** `brand_profiles` table with JSONB for voice attributes

#### 5. Redis Cache
**Purpose:** High-speed caching of frequently accessed profiles
**TTL:** 30 minutes

#### 6. GCP Pub/Sub
**Topics:**
- `brand-profile-events` - Profile lifecycle events
- `content-validation-events` - Validation analytics

---

## Setup & Installation

### Prerequisites
- Java 21 JDK
- Maven 3.9+
- Node.js 20+
- Python 3.11+
- Docker & Docker Compose
- GCP account with Cloud Run, Cloud SQL, Pub/Sub enabled

### Local Development Setup

#### 1. Clone Repository
```bash
git clone https://github.com/typeface/brand-voice-service.git
cd brand-voice-service
```

#### 2. Start Infrastructure with Docker Compose
```bash
docker-compose up -d postgres redis
```

#### 3. Configure Environment Variables
Create `.env` file:
```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=brandvoice
DB_USERNAME=postgres
DB_PASSWORD=password
REDIS_HOST=localhost
REDIS_PORT=6379
NLP_ANALYZER_URL=http://localhost:8001
GCP_PROJECT_ID=typeface-dev
```

#### 4. Run Database Migrations
```bash
cd brand-voice-service
mvn flyway:migrate
```

#### 5. Start NLP Analyzer
```bash
cd nlp-analyzer
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --port 8001 --reload
```

#### 6. Start Brand Voice Service
```bash
cd brand-voice-service
mvn spring-boot:run
```

#### 7. Start API Gateway
```bash
cd api-gateway
npm install
npm run dev
```

#### 8. Verify Setup
```bash
# Check health endpoints
curl http://localhost:3000/health
curl http://localhost:8080/api/v1/brand-voice/health
curl http://localhost:8001/health
```

### Using Docker Compose (Recommended)
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

---

## API Documentation

### Base URL
- Local: `http://localhost:3000/api/v1/brand-voice`
- Staging: `https://staging-api.typeface.ai/api/v1/brand-voice`
- Production: `https://api.typeface.ai/api/v1/brand-voice`

### Authentication
Currently using customer ID validation. API key authentication planned for Phase 2.

### Endpoints

#### 1. Create Brand Profile
```http
POST /profiles
Content-Type: application/json

{
  "customerId": "cust_123",
  "brandName": "TechCorp Inc",
  "sampleContent": "We are a professional enterprise company..."
}
```

**Response (201 Created):**
```json
{
  "profileId": "uuid-here",
  "customerId": "cust_123",
  "brandName": "TechCorp Inc",
  "voiceAttributes": {
    "tone": "professional",
    "formality": 0.85,
    "vocabulary_complexity": 0.68,
    "sentence_length": "medium"
  },
  "confidenceScore": 0.92,
  "status": "active",
  "createdAt": "2025-01-28T10:30:00Z"
}
```

**Error Cases:**
- 400: Invalid request (missing fields, content too short)
- 409: Profile already exists for customer
- 500: Internal server error

#### 2. Get Brand Profile
```http
GET /profiles/{customerId}
```

**Response (200 OK):**
```json
{
  "profileId": "uuid-here",
  "customerId": "cust_123",
  "brandName": "TechCorp Inc",
  "voiceAttributes": {...},
  "confidenceScore": 0.92,
  "status": "active",
  "createdAt": "2025-01-28T10:30:00Z"
}
```

**Error Cases:**
- 404: Profile not found
- 500: Internal server error

#### 3. Validate Content
```http
POST /validate
Content-Type: application/json

{
  "customerId": "cust_123",
  "content": "Check out our amazing new features!",
  "contentType": "email"
}
```

**Response (200 OK):**
```json
{
  "customerId": "cust_123",
  "consistencyScore": 72.5,
  "verdict": "minor_issues",
  "issues": [
    {
      "type": "tone",
      "severity": "medium",
      "description": "The tone doesn't match your brand voice",
      "suggestion": "Try using a more professional tone"
    }
  ],
  "detailedScores": {
    "tone": 65.0,
    "formality": 70.0,
    "vocabulary": 80.0,
    "sentence_structure": 75.0
  },
  "processingTimeMs": 342
}
```

**Verdict Values:**
- `on_brand` - Score >= 80
- `minor_issues` - Score 60-79
- `off_brand` - Score < 60

**Error Cases:**
- 400: Invalid request
- 404: Profile not found for customer
- 500: Internal server error

#### 4. Delete Brand Profile
```http
DELETE /profiles/{customerId}
```

**Response (204 No Content)**

**Error Cases:**
- 404: Profile not found
- 500: Internal server error

### Rate Limiting
- 100 requests per minute per IP address
- Returns 429 Too Many Requests if exceeded

---

## Database Schema

### Table: brand_profiles

```sql
CREATE TABLE brand_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id VARCHAR(255) UNIQUE NOT NULL,
    brand_name VARCHAR(255) NOT NULL,
    voice_attributes JSONB NOT NULL,
    sample_content TEXT,
    confidence_score DECIMAL(3,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT true
);

CREATE INDEX idx_customer_id ON brand_profiles(customer_id);
CREATE INDEX idx_active ON brand_profiles(active);
CREATE INDEX idx_created_at ON brand_profiles(created_at DESC);
```

### Voice Attributes Structure (JSONB)
```json
{
  "tone": "professional",
  "formality": 0.85,
  "vocabulary_complexity": 0.68,
  "sentence_length": "medium",
  "avg_sentence_length": 18.5,
  "key_phrases": ["innovative", "strategic", "excellence"]
}
```

### Database Maintenance
- **Backups:** Automated daily backups via Cloud SQL
- **Retention:** 30 days
- **Index Maintenance:** Automatic via PostgreSQL autovacuum
- **Connection Pooling:** HikariCP (max 10 connections)

---

## Deployment

### GCP Cloud Run Deployment

#### Prerequisites
1. GCP project with billing enabled
2. Cloud Run API enabled
3. Cloud SQL PostgreSQL instance
4. Memorystore for Redis instance
5. Pub/Sub topics created

#### Deployment Steps

1. **Configure gcloud CLI**
```bash
gcloud config set project typeface-prod
gcloud auth configure-docker
```

2. **Set up Cloud Build trigger**
```bash
gcloud builds triggers create github \
  --repo-name=brand-voice-service \
  --repo-owner=typeface \
  --branch-pattern=^main$ \
  --build-config=cloudbuild.yaml
```

3. **Manual Deployment**
```bash
# Build and push
gcloud builds submit --config cloudbuild.yaml

# Deploy to Cloud Run
gcloud run deploy brand-voice-service \
  --image gcr.io/typeface-prod/brand-voice-service:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars "DB_HOST=<cloudsql-proxy>,REDIS_HOST=<redis-ip>"
```

#### Environment Variables (Production)
```bash
SPRING_PROFILES_ACTIVE=prod
DB_HOST=<Cloud SQL connection>
DB_NAME=brandvoice_prod
DB_USERNAME=<from Secret Manager>
DB_PASSWORD=<from Secret Manager>
REDIS_HOST=<Memorystore IP>
NLP_ANALYZER_URL=https://nlp-analyzer-xyz.run.app
GCP_PROJECT_ID=typeface-prod
```

### CI/CD Pipeline

**Trigger:** Push to `main` branch

**Steps:**
1. Run unit tests
2. Run integration tests
3. Build Docker images
4. Push to Container Registry
5. Deploy to staging
6. Run smoke tests
7. Manual approval required
8. Deploy to production

**Rollback:**
```bash
# List previous revisions
gcloud run revisions list --service brand-voice-service

# Rollback to specific revision
gcloud run services update-traffic brand-voice-service \
  --to-revisions=<revision-name>=100
```

---

## Monitoring & Troubleshooting

### Key Metrics to Monitor

#### 1. Application Metrics
- **API Latency** (p50, p95, p99)
    - Target: p95 < 500ms
    - Alert: p95 > 1000ms

- **Request Rate**
    - Track: requests/second
    - Alert: Sudden drops or spikes

- **Error Rate**
    - Target: < 0.5%
    - Alert: > 1%

- **Consistency Score Distribution**
    - Monitor: Average scores over time
    - Alert: Significant shifts in distribution

#### 2. Infrastructure Metrics
- **CPU Usage**
    - Target: < 70%
    - Alert: > 85%

- **Memory Usage**
    - Target: < 80%
    - Alert: > 90%

- **Instance Count**
    - Monitor: Auto-scaling behavior
    - Alert: Max instances reached

#### 3. Database Metrics
- **Connection Pool Usage**
    - Target: < 80%
    - Alert: > 90%

- **Query Latency**
    - Target: < 50ms
    - Alert: > 200ms

- **Cache Hit Rate**
    - Target: > 80%
    - Alert: < 60%

### Monitoring Dashboards

#### GCP Cloud Monitoring
Access: https://console.cloud.google.com/monitoring

**Custom Dashboard:** "Brand Voice Service Overview"
- Request rate and latency
- Error rate by endpoint
- Instance metrics
- Database performance

#### Logs
```bash
# View real-time logs
gcloud run services logs tail brand-voice-service

# Search for errors
gcloud logging read "resource.type=cloud_run_revision AND severity>=ERROR"
```

### Alert Policies

**Critical Alerts** (PagerDuty):
- Error rate > 1%
- API latency p95 > 1000ms
- Service down
- Database connection failures

**Warning Alerts** (Slack):
- Error rate > 0.5%
- API latency p95 > 500ms
- Memory usage > 80%
- Cache hit rate < 60%

---

## Common Issues & Solutions

### Issue 1: High Latency
**Symptoms:** API responses taking > 1 second

**Possible Causes:**
1. NLP Analyzer service slow/unavailable
2. Database query performance
3. Redis cache misses

**Solutions:**
```bash
# Check NLP Analyzer health
curl https://nlp-analyzer-xyz.run.app/health

# Check Redis connection
redis-cli -h <redis-host> ping

# Review slow queries
SELECT query, calls, mean_exec_time 
FROM pg_stat_statements 
ORDER BY mean_exec_time DESC 
LIMIT 10;

# Clear and warm cache
redis-cli FLUSHDB
# Make requests to popular profiles to warm cache
```

### Issue 2: Profile Creation Failures
**Symptoms:** 500 errors when creating profiles

**Possible Causes:**
1. Sample content too short
2. NLP analyzer unavailable
3. Database connection issues

**Solutions:**
```bash
# Validate sample content length
# Minimum: 100 characters

# Check NLP analyzer logs
gcloud run services logs tail nlp-analyzer

# Test database connection
psql -h <db-host> -U <username> -d brandvoice -c "SELECT 1;"

# Check for database locks
SELECT * FROM pg_locks WHERE NOT granted;
```

### Issue 3: Inconsistent Validation Results
**Symptoms:** Same content gets different scores

**Possible Causes:**
1. Cache inconsistency
2. Profile updated but cache not invalidated
3. NLP analyzer returning inconsistent results

**Solutions:**
```bash
# Clear specific cache entry
redis-cli DEL "brandProfiles::customer-id"

# Verify profile in database
SELECT * FROM brand_profiles WHERE customer_id = 'customer-id';

# Force profile refresh
DELETE /api/v1/brand-voice/profiles/{customerId}
POST /api/v1/brand-voice/profiles
```

### Issue 4: Memory Leaks
**Symptoms:** Memory usage increasing over time

**Solutions:**
```bash
# Restart service (zero-downtime)
gcloud run services update brand-voice-service \
  --revision-suffix=fix-$(date +%s)

# Check for connection pool leaks
# Review HikariCP metrics in logs

# Analyze heap dump (if needed)
jmap -dump:live,format=b,file=heap.bin <pid>
```

### Issue 5: Pub/Sub Event Publishing Failures
**Symptoms:** Events not appearing in downstream systems

**Solutions:**
```bash
# Check Pub/Sub topic permissions
gcloud pubsub topics get-iam-policy brand-profile-events

# Verify service account has Publisher role
gcloud projects add-iam-policy-binding typeface-prod \
  --member=serviceAccount:service-account@... \
  --role=roles/pubsub.publisher

# Test publishing manually
gcloud pubsub topics publish brand-profile-events \
  --message='{"test": "message"}'

# Check dead letter queue
gcloud pubsub subscriptions pull brand-profile-events-dlq --limit=10
```

---

## Maintenance Tasks

### Daily
- [ ] Review error logs in Cloud Monitoring
- [ ] Check alert notifications
- [ ] Monitor API latency dashboard

### Weekly
- [ ] Review capacity and auto-scaling behavior
- [ ] Check database slow query log
- [ ] Verify cache hit rates
- [ ] Review Pub/Sub delivery metrics

### Monthly
- [ ] Database vacuum and analyze
- [ ] Review and archive old logs
- [ ] Update dependencies (security patches)
- [ ] Performance testing
- [ ] Cost analysis

### Quarterly
- [ ] Disaster recovery test
- [ ] Load testing with production-like traffic
- [ ] Security audit
- [ ] Documentation review

### Database Maintenance Script
```sql
-- Monthly maintenance
VACUUM ANALYZE brand_profiles;

-- Check table bloat
SELECT schemaname, tablename,
  pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Update statistics
ANALYZE brand_profiles;
```

---

## Future Enhancements

### Planned Features (Prioritized)

#### Phase 2 - Q2 2025
1. **GPT-4 Integration**
    - More sophisticated voice analysis
    - Better suggestion generation
    - Estimated effort: 3 weeks

2. **Multi-language Support**
    - Support for 10+ languages
    - Language-specific voice characteristics
    - Estimated effort: 4 weeks

3. **Auto-correction**
    - Generate corrected content suggestions
    - Integration with content generation pipeline
    - Estimated effort: 2 weeks

4. **API Key Authentication**
    - Replace customer ID validation
    - Rate limiting per API key
    - Estimated effort: 1 week

#### Phase 3 - Q3 2025
1. **Industry Templates**
    - Pre-built profiles for common industries
    - Template marketplace
    - Estimated effort: 3 weeks

2. **Voice Evolution Tracking**
    - Track how brand voice changes over time
    - Historical analysis
    - Estimated effort: 2 weeks

3. **Webhook Notifications**
    - Push notifications for validation events
    - Custom webhook endpoints
    - Estimated effort: 2 weeks

### Technical Debt
1. Improve test coverage to 90%
2. Add more comprehensive error messages
3. Implement request tracing (OpenTelemetry)
4. Optimize database queries with additional indexes
5. Add GraphQL API alongside REST

---

## Contact & Support

### Team Contacts
- **Service Owner:** [Your Name] - your.email@typeface.ai
- **Tech Lead:** [Tech Lead Name] - techlead@typeface.ai
- **On-call:** Slack #brand-voice-service-oncall

### Escalation Path
1. Check this documentation
2. Search internal Confluence
3. Post in #brand-voice-service Slack channel
4. Create Jira ticket (project: BVS)
5. Escalate to tech lead

### Useful Links
- GitHub Repository: https://github.com/typeface/brand-voice-service
- API Documentation: https://docs.typeface.ai/brand-voice
- Monitoring Dashboard: https://console.cloud.google.com/monitoring
- Incident Runbook: https://confluence.typeface.ai/brand-voice-runbook
- Architecture Diagrams: https://drive.google.com/brand-voice-architecture

---

## Appendix

### Glossary
- **Brand Voice:** The distinct personality and style of a brand's communication
- **Consistency Score:** A metric (0-100) indicating how well content matches a brand profile
- **Voice Attributes:** Characteristics like tone, formality, vocabulary complexity
- **MQL/LQL:** Monitoring Query Language / Logging Query Language (GCP)

### Related Services
- **Content Generation Service:** Generates AI content that this service validates
- **Template Service:** Provides templates that use brand voice profiles
- **Analytics Service:** Consumes events from this service for reporting

### Additional Resources
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- GCP Cloud Run Best Practices: https://cloud.google.com/run/docs/best-practices
- FastAPI Documentation: https://fastapi.tiangolo.com/

---

**Document Version:** 1.0  
**Last Updated:** November 30, 2025  