#!/bin/bash

echo "Checking Docker installation..."

# Check if docker command exists
if ! command -v docker &> /dev/null; then
    echo "❌ Docker command not found. Please install Docker Desktop."
    exit 1
fi

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
    echo "❌ Docker daemon is not running. Please start Docker Desktop."
    exit 1
fi

echo "✅ Docker is installed and running!"
docker --version
docker compose version

echo ""
echo "Ready to run the Spring Boot application with Docker Compose!"