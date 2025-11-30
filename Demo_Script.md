# Live Demo Script - Brand Voice Service

## 🎯 Demo Objective
Show how the Brand Voice Service learns a customer's brand voice and validates content in real-time.

## ⏱️ Duration: 5-7 minutes

---

## Setup Checklist (Before Demo)

- [ ] All services running: `docker-compose ps` (all should show "healthy")
- [ ] Terminal ready with commands prepared
- [ ] Browser tabs open:
    - Terminal window for commands
    - Text editor with code samples
    - (Optional) Database client showing empty table
- [ ] Clear any test data: `docker-compose exec postgres psql -U postgres -d brandvoice -c "DELETE FROM brand_profiles;"`
- [ ] Have backup screenshots ready in case of failure

---

## Demo Flow

### Part 1: Introduction (30 seconds)

**What to say:**
"Let me show you how this works with a live demo. I'll walk through creating a brand profile for a professional enterprise company, then validating different types of content against that profile."

**Action:**
Open terminal, show services are running:
```bash
curl http://localhost:3000/health
```

**What to say:**
"All services are healthy and ready. Let's get started."

---

### Part 2: Create Brand Profile (90 seconds)

**What to say:**
"First, let's create a brand profile for a fictional company called 'TechCorp Enterprise'. I'll provide some sample content that represents their professional brand voice."

**Action:**
Show the request first (in text editor):
```json
{
  "customerId": "techcorp-demo",
  "brandName": "TechCorp Enterprise",
  "sampleContent": "We are a professional enterprise technology company focused on delivering innovative solutions. Our strategic approach ensures optimal results for our clients. We believe in excellence, efficiency, and partnership. Our commitment to quality drives everything we do."
}
```

**What to say:**
"Notice the professional tone - words like 'enterprise', 'strategic', 'excellence'. This is typical B2B corporate language."

**Action:**
Execute the request:
```bash
curl -X POST http://localhost:3000/api/v1/brand-voice/profiles \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "techcorp-demo",
    "brandName": "TechCorp Enterprise",
    "sampleContent": "We are a professional enterprise technology company focused on delivering innovative solutions. Our strategic approach ensures optimal results for our clients. We believe in excellence, efficiency, and partnership. Our commitment to quality drives everything we do."
  }'
```

**What to say while waiting:**
"The service is now analyzing this content through our NLP engine..."

**Action:**
Show the response (highlight key parts):
```json
{
  "profileId": "...",
  "customerId": "techcorp-demo",
  "voiceAttributes": {
    "tone": "professional",
    "formality": 0.85,
    "vocabulary_complexity": 0.68,
    "sentence_length": "medium"
  },
  "confidenceScore": 0.92,
  "status": "active"
}
```

**What to say:**
"Great! The system identified this as a professional tone with high formality at 0.85 out of 1.0, and medium complexity vocabulary. The confidence score of 0.92 means we have enough data to reliably validate future content. This entire analysis took about 1 second."

---

### Part 3: Validate On-Brand Content (90 seconds)

**What to say:**
"Now let's validate some content that matches their brand voice - something a marketing team might write for an email campaign."

**Action:**
Show the content first:
```json
{
  "customerId": "techcorp-demo",
  "content": "Our professional team delivers innovative enterprise solutions with strategic focus and operational excellence. We partner with organizations to optimize their technology infrastructure.",
  "contentType": "email"
}
```

**What to say:**
"This has similar characteristics - professional language, formal tone, similar vocabulary."

**Action:**
Execute validation:
```bash
curl -X POST http://localhost:3000/api/v1/brand-voice/validate \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "techcorp-demo",
    "content": "Our professional team delivers innovative enterprise solutions with strategic focus and operational excellence. We partner with organizations to optimize their technology infrastructure.",
    "contentType": "email"
  }'
```

**What to say while waiting:**
"This should complete in under 500 milliseconds..."

**Action:**
Show response:
```json
{
  "customerId": "techcorp-demo",
  "consistencyScore": 88.5,
  "verdict": "on_brand",
  "issues": [],
  "detailedScores": {
    "tone": 90.0,
    "formality": 87.0,
    "vocabulary": 89.0,
    "sentence_structure": 88.0
  },
  "processingTimeMs": 342
}
```

**What to say:**
"Excellent! Consistency score of 88.5 out of 100 - that's 'on_brand' with no issues. You can see the detailed breakdown: tone scored 90, formality 87, vocabulary 89. Everything aligns well with their profile. And notice the processing time - 342 milliseconds, well under our 500ms target."

---

### Part 4: Validate Off-Brand Content (90 seconds)

**What to say:**
"Now here's where it gets interesting. Let's validate content that doesn't match their brand - maybe someone wrote this for social media without thinking about brand consistency."

**Action:**
Show the off-brand content:
```json
{
  "customerId": "techcorp-demo",
  "content": "Hey folks! Check out our super cool new features! They're totally awesome and you're gonna love them! 🎉",
  "contentType": "social"
}
```

**What to say:**
"Notice the difference - 'Hey folks', 'super cool', 'awesome', emojis. This is very casual and informal."

**Action:**
Execute validation:
```bash
curl -X POST http://localhost:3000/api/v1/brand-voice/validate \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "techcorp-demo",
    "content": "Hey folks! Check out our super cool new features! They'\''re totally awesome and you'\''re gonna love them!",
    "contentType": "social"
  }'
```

**Action:**
Show response:
```json
{
  "customerId": "techcorp-demo",
  "consistencyScore": 62.3,
  "verdict": "minor_issues",
  "issues": [
    {
      "type": "tone",
      "severity": "medium",
      "description": "The tone doesn't match your brand voice",
      "suggestion": "Try using a more professional tone"
    },
    {
      "type": "formality",
      "severity": "medium",
      "description": "The formality doesn't match your brand voice",
      "suggestion": "Adjust the formality level to match your brand"
    }
  ],
  "detailedScores": {
    "tone": 60.0,
    "formality": 55.0,
    "vocabulary": 70.0,
    "sentence_structure": 64.0
  },
  "processingTimeMs": 289
}
```

**What to say:**
"Perfect! The system caught this. Consistency score of only 62.3 - that's 'minor_issues'. You can see specific problems: tone scored only 60, formality 55. The system provides actionable feedback: 'Try using a more professional tone' and 'Adjust the formality level'. This is exactly the kind of feedback content creators need before publishing."

---

### Part 5: Show Mixed Content (60 seconds - Optional)

**What to say:**
"Let me show you one more example - content that mixes both styles."

**Action:**
```bash
curl -X POST http://localhost:3000/api/v1/brand-voice/validate \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "techcorp-demo",
    "content": "Our innovative solutions are super amazing! We provide professional services that are totally awesome and deliver strategic results.",
    "contentType": "ad"
  }'
```

**What to say:**
"Score around 70-75, classified as 'minor_issues'. The professional elements pull the score up, but the casual language pulls it down. This shows how sensitive the system is to mixed messaging."

---

### Part 6: Performance Highlight (30 seconds)

**What to say:**
"Notice how fast these responses are - all under 500 milliseconds. This is because:"

**Action:**
Point to terminal/architecture diagram:
1. "We cache brand profiles in Redis"
2. "The NLP analysis is optimized for speed"
3. "Everything runs in parallel where possible"

**What to say:**
"This means we can validate content in real-time as users type, providing immediate feedback without disrupting their workflow."

---

### Part 7: Quick Look at Database (30 seconds - Optional)

**What to say:**
"Let me quickly show you the stored profile in the database."

**Action:**
```bash
docker-compose exec postgres psql -U postgres -d brandvoice -c \
  "SELECT customer_id, brand_name, voice_attributes->>'tone' as tone, 
   (voice_attributes->>'formality')::float as formality 
   FROM brand_profiles WHERE customer_id = 'techcorp-demo';"
```

**What to say:**
"Here you can see the profile stored with all the voice characteristics we extracted. This PostgreSQL JSONB column lets us store flexible attributes that can evolve over time."

---

### Part 8: Monitoring (30 seconds - Optional)

**What to say:**
"In production, we have comprehensive monitoring."

**Action:**
Open monitoring dashboard or show logs:
```bash
docker-compose logs brand-voice-service | tail -20
```

**What to say:**
"Every request is logged with latency metrics, consistency scores, and any errors. We track API response times, cache hit rates, and database performance. Alerts fire if latency exceeds 1 second or error rates go above 1%."

---

### Part 9: Cleanup (Optional)

**What to say:**
"Finally, if a customer wants to delete their profile..."

**Action:**
```bash
curl -X DELETE http://localhost:3000/api/v1/brand-voice/profiles/techcorp-demo
```

**What to say:**
"Clean deletion with all data removed. We also publish an event so downstream systems know to clean up their data too."

---

## Demo Wrap-up

**What to say:**
"So to recap what you just saw:
1. We created a brand profile from sample content in about 1 second
2. The system accurately identified professional voice characteristics
3. It validated on-brand content with a high score and no issues
4. It caught off-brand content and provided specific, actionable feedback
5. All of this happened in under 500 milliseconds

This service integrates seamlessly into our content generation pipeline, providing real-time brand voice validation that saves customers hours of manual review each week."

---

## Troubleshooting During Demo

### If a Service is Down
**Say:** "Looks like we have a network issue. No problem - I have screenshots prepared showing exactly what would happen."
*Show pre-prepared screenshots*

### If Response is Slow
**Say:** "This is taking a bit longer than usual - probably because it's the first request and the cache is cold. In production with warm caches, this would be much faster."

### If You Make a Typo
**Say:** "Let me fix that syntax..." *fix and re-run*
"There we go."

### If Asked Unexpected Questions Mid-Demo
**Say:** "That's a great question - let me make a note and I'll address it at the end so we can complete the demo flow first."

---

## Demo Success Checklist

After the demo, you should have shown:
- ✅ Profile creation with NLP analysis
- ✅ On-brand validation (high score, no issues)
- ✅ Off-brand validation (low score, specific issues)
- ✅ Fast response times (< 500ms)
- ✅ Actionable feedback for users
- ✅ Production-ready features (monitoring, error handling)

---

## Backup: Pre-recorded Demo

If you're nervous about live demo, consider:
1. Recording the demo beforehand
2. Narrating over the recording during presentation
3. Showing live terminal as "proof it's real"

**Script for this approach:**
"I've pre-recorded this demo for time efficiency, but you can see here the services are running live if you'd like me to run any commands manually."

---

**Remember:** The demo is about showing **value**, not perfection. If something breaks, how you handle it matters more than the break itself!