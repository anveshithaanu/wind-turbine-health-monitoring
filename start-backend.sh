#!/bin/bash

echo "Starting Wind Turbine Monitor Backend..."
echo ""

if [ ! -d "frontend/node_modules" ]; then
    echo "Building frontend first..."
    cd frontend
    npm install
    npm run build
    cd ..
fi

echo "Starting with Docker Compose..."
docker-compose up --build


