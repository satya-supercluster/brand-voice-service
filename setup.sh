#!/bin/bash

# Brand Voice Service - Automated Setup Script
# This script creates the complete project structure

set -e  # Exit on error

echo "╔════════════════════════════════════════════════════════════╗"
echo "║     Brand Voice Service - Project Setup                    ║"
echo "║     Creating complete project structure...                 ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

PROJECT_NAME="brand-voice-service"

# Check if directory already exists
if [ -d "$PROJECT_NAME" ]; then
    echo "⚠️  Directory '$PROJECT_NAME' already exists!"
    read -p "Do you want to delete it and start fresh? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        rm -rf "$PROJECT_NAME"
        echo "✓ Removed existing directory"
    else
        echo "❌ Setup cancelled"
        exit 1
    fi
fi

echo "📁 Creating project structure..."

# Create root directory
mkdir -p "$PROJECT_NAME"
cd "$PROJECT_NAME"

# Create Java project structure
echo "  → Creating Java source directories..."
mkdir -p src/main/java/com/typeface/brandvoice/{controller,service,model,dto,repository,config,exception}
mkdir -p src/main/resources/db/migration
mkdir -p src/test/java/com/typeface/brandvoice/service

# Create API Gateway structure
echo "  → Creating API Gateway directories..."
mkdir -p api-gateway/src

# Create NLP Analyzer structure
echo "  → Creating NLP Analyzer directories..."
mkdir -p nlp-analyzer

# Create documentation directory
echo "  → Creating documentation directories..."
mkdir -p docs

echo "✓ Directory structure created"
echo ""

echo "📝 Creating placeholder files..."

# Java source files
touch src/main/java/com/typeface/brandvoice/BrandVoiceServiceApplication.java
touch src/main/java/com/typeface/brandvoice/controller/BrandVoiceController.java
touch src/main/java/com/typeface/brandvoice/service/BrandVoiceService.java
touch src/main/java/com/typeface/brandvoice/service/NLPAnalyzerService.java
touch src/main/java/com/typeface/brandvoice/service/EventPublisherService.java
touch src/main/java/com/typeface/brandvoice/model/BrandProfile.java
touch src/main/java/com/typeface/brandvoice/dto/CreateBrandProfileRequest.java
touch src/main/java/com/typeface/brandvoice/dto/ValidateContentRequest.java
touch src/main/java/com/typeface/brandvoice/dto/BrandProfileResponse.java
touch src/main/java/com/typeface/brandvoice/dto/ContentValidationResponse.java
touch src/main/java/com/typeface/brandvoice/repository/BrandProfileRepository.java
touch src/main/java/com/typeface/brandvoice/config/ApplicationConfig.java
touch src/main/java/com/typeface/brandvoice/exception/GlobalExceptionHandler.java

# Resources
touch src/main/resources/application.yml
touch src/main/resources/application-test.yml
touch src/main/resources/db/migration/V1__Initial_Schema.sql

# Test files
touch src/test/java/com/typeface/brandvoice/BrandVoiceServiceIntegrationTest.java
touch src/test/java/com/typeface/brandvoice/service/BrandVoiceServiceTest.java

# API Gateway
touch api-gateway/src/index.ts
touch api-gateway/package.json
touch api-gateway/tsconfig.json
touch api-gateway/Dockerfile
touch api-gateway/.gitignore

# NLP Analyzer
touch nlp-analyzer/main.py
touch nlp-analyzer/requirements.txt
touch nlp-analyzer/Dockerfile

# Root files
touch pom.xml
touch Dockerfile
touch docker-compose.yml
touch cloudbuild.yaml
touch Makefile
touch .gitignore
touch .env.example
touch .env

# Documentation
touch README.md
touch QUICKSTART.md
touch DEMO_SCRIPT.md
touch FILE_STRUCTURE.md
touch docs/PRESENTATION.md
touch docs/PRACTICE_GUIDE.md
touch docs/KNOWLEDGE_TRANSFER.md

# Testing tools
touch "Brand Voice Service.postman_collection.json"

echo "✓ Placeholder files created"
echo ""

echo "📋 Project structure summary:"
echo ""
echo "  Java Files:        $(find src/main/java -name '*.java' | wc -l | tr -d ' ')"
echo "  Test Files:        $(find src/test/java -name '*.java' | wc -l | tr -d ' ')"
echo "  Resource Files:    $(find src/main/resources -type f | wc -l | tr -d ' ')"
echo "  TypeScript Files:  $(find api-gateway -name '*.ts' | wc -l | tr -d ' ')"
echo "  Python Files:      $(find nlp-analyzer -name '*.py' | wc -l | tr -d ' ')"
echo "  Config Files:      $(find . -maxdepth 1 -type f | wc -l | tr -d ' ')"
echo "  Documentation:     $(find docs -name '*.md' | wc -l | tr -d ' ')"
echo ""

# Create README with next steps
cat > NEXT_STEPS.md << 'EOF'
# Next Steps

## ✅ Project Structure Created!

All directories and placeholder files have been created. Now you need to:

### 1. Copy Code from Artifacts

For each file, copy the content from the corresponding artifact:

#### Java Files (Spring Boot Service)
- `BrandVoiceServiceApplication.java` ← artifact: brand_voice_main
- `BrandProfile.java` ← artifact: brand_voice_model
- `BrandVoiceController.java` ← artifact: brand_voice_controller
- `BrandVoiceService.java` ← artifact: brand_voice_service
- `NLPAnalyzerService.java` ← artifact: nlp_analyzer_service
- `EventPublisherService.java` ← artifact: event_publisher_service
- `BrandProfileRepository.java` ← artifact: brand_profile_repo
- `ApplicationConfig.java` ← artifact: app_config
- `GlobalExceptionHandler.java` ← artifact: exception_handler

#### DTO Files
All DTOs are in artifact: voice_score_dto (split into 4 files)

#### Resources
- `application.yml` ← artifact: application_yml
- `application-test.yml` ← artifact: application_test_yml
- `V1__Initial_Schema.sql` ← artifact: db_migration

#### Tests
- `BrandVoiceServiceIntegrationTest.java` ← artifact: integration_tests
- `BrandVoiceServiceTest.java` ← artifact: unit_test_service

#### API Gateway (TypeScript)
- `api-gateway/src/index.ts` ← artifact: typescript_gateway
- `api-gateway/package.json` ← artifact: gateway_package_json
- `api-gateway/tsconfig.json` ← artifact: gateway_tsconfig
- `api-gateway/Dockerfile` ← artifact: gateway_dockerfile

#### NLP Analyzer (Python)
- `nlp-analyzer/main.py` ← artifact: python_nlp_main
- `nlp-analyzer/requirements.txt` ← artifact: python_requirements
- `nlp-analyzer/Dockerfile` ← artifact: python_dockerfile

#### Build Files
- `pom.xml` ← artifact: pom_xml
- `Dockerfile` ← artifact: dockerfile_java
- `docker-compose.yml` ← artifact: docker_compose
- `cloudbuild.yaml` ← artifact: gcp_cloudbuild
- `Makefile` ← artifact: makefile
- `.gitignore` ← artifact: gitignore
- `.env.example` ← artifact: env_example

#### Documentation
- `README.md` ← artifact: main_readme
- `QUICKSTART.md` ← artifact: quickstart_guide
- `DEMO_SCRIPT.md` ← artifact: demo_script
- `FILE_STRUCTURE.md` ← artifact: file_structure
- `docs/PRESENTATION.md` ← artifact: presentation_slides
- `docs/PRACTICE_GUIDE.md` ← artifact: practice_guide
- `docs/KNOWLEDGE_TRANSFER.md` ← artifact: knowledge_transfer

### 2. Set Up Environment

```bash
# Copy environment example
cp .env.example .env

# Edit .env with your local settings (optional, defaults work fine)
```

### 3. Start the Services

```bash
# Option 1: Using Docker Compose (Recommended)
docker-compose up -d

# Option 2: Using Makefile
make docker-up

# Wait for services to be healthy
docker-compose ps
```

### 4. Verify Everything Works

```bash
# Check health
curl http://localhost:3000/health

# Run the demo
# Follow DEMO_SCRIPT.md for step-by-step testing
```

### 5. Prepare for Your Interview

1. **Read** (2-3 hours):
   - README.md
   - PRESENTATION.md
   - PRACTICE_GUIDE.md

2. **Practice** (3-4 hours):
   - Run through DEMO_SCRIPT.md
   - Practice presentation out loud
   - Answer practice questions

3. **Review Code** (2-3 hours):
   - BrandVoiceService.java
   - BrandVoiceController.java
   - main.py (Python NLP)

## 📚 Quick Links

- [Main README](README.md) - Project overview
- [Quick Start](QUICKSTART.md) - Get running in 5 minutes
- [Demo Script](DEMO_SCRIPT.md) - Live demo walkthrough
- [Presentation](docs/PRESENTATION.md) - Slides with speaker notes
- [Practice Guide](docs/PRACTICE_GUIDE.md) - Interview prep
- [Knowledge Transfer](docs/KNOWLEDGE_TRANSFER.md) - Technical deep-dive
- [File Structure](FILE_STRUCTURE.md) - Where everything goes

## 🎯 Success Checklist

- [ ] All code files populated from artifacts
- [ ] Docker Compose starts successfully
- [ ] All health checks pass
- [ ] Demo script works end-to-end
- [ ] Can explain architecture confidently
- [ ] Can answer practice questions
- [ ] Presentation practiced 3+ times

## ❓ Need Help?

1. Check FILE_STRUCTURE.md for file locations
2. Review artifact mapping in that document
3. Verify each file has correct package declarations
4. Ensure all dependencies are in pom.xml and package.json

Good luck! You've got this! 🚀
EOF

echo "✅ Setup complete!"
echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                   NEXT STEPS                               ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "1. Read NEXT_STEPS.md for detailed instructions"
echo "2. Copy code from artifacts to corresponding files"
echo "3. Run: docker-compose up -d"
echo "4. Test: curl http://localhost:3000/health"
echo ""
echo "📖 Documentation created:"
echo "   - NEXT_STEPS.md (start here!)"
echo "   - FILE_STRUCTURE.md"
echo "   - README.md"
echo "   - QUICKSTART.md"
echo "   - DEMO_SCRIPT.md"
echo ""
echo "✨ Happy coding!"