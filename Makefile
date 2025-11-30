.PHONY: help build test run clean docker-up docker-down install

help:
	@echo "Brand Voice Service - Available Commands"
	@echo ""
	@echo "  make install        - Install all dependencies"
	@echo "  make build          - Build all services"
	@echo "  make test           - Run all tests"
	@echo "  make run            - Run services locally"
	@echo "  make docker-up      - Start all services with Docker Compose"
	@echo "  make docker-down    - Stop all Docker services"
	@echo "  make clean          - Clean build artifacts"
	@echo "  make logs           - View Docker logs"
	@echo ""

install:
	@echo "Installing Java dependencies..."
	mvn clean install -DskipTests
	@echo "Installing Node dependencies..."
	cd api-gateway && npm install
	@echo "Installing Python dependencies..."
	cd nlp-analyzer && pip install -r requirements.txt
	@echo "✓ All dependencies installed"

build:
	@echo "Building Spring Boot service..."
	mvn clean package -DskipTests
	@echo "Building API Gateway..."
	cd api-gateway && npm run build
	@echo "✓ All services built"

test:
	@echo "Running Java tests..."
	mvn test
	@echo "✓ Tests completed"

run-java:
	@echo "Starting Spring Boot service..."
	mvn spring-boot:run

run-gateway:
	@echo "Starting API Gateway..."
	cd api-gateway && npm run dev

run-nlp:
	@echo "Starting NLP Analyzer..."
	cd nlp-analyzer && uvicorn main:app --reload --port 8001

docker-up:
	@echo "Starting all services with Docker Compose..."
	docker-compose up -d
	@echo "✓ All services started"
	@echo "API Gateway: http://localhost:3000"
	@echo "Brand Voice Service: http://localhost:8080"
	@echo "NLP Analyzer: http://localhost:8001"

docker-down:
	@echo "Stopping all Docker services..."
	docker-compose down
	@echo "✓ All services stopped"

docker-logs:
	docker-compose logs -f

clean:
	@echo "Cleaning build artifacts..."
	mvn clean
	cd api-gateway && rm -rf dist node_modules
	cd nlp-analyzer && rm -rf __pycache__
	@echo "✓ Clean completed"

db-migrate:
	@echo "Running database migrations..."
	mvn flyway:migrate
	@echo "✓ Migrations completed"

health-check:
	@echo "Checking service health..."
	@curl -s http://localhost:3000/health && echo "" || echo "✗ Gateway not responding"
	@curl -s http://localhost:8080/api/v1/brand-voice/health && echo "" || echo "✗ Service not responding"
	@curl -s http://localhost:8001/health && echo "" || echo "✗ NLP not responding"