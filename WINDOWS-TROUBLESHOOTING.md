# Windows Whitelabel Error - Troubleshooting Guide

## ❌ Problem: Whitelabel Error Page

The whitelabel error is **NOT** a Docker version issue. It means Spring Boot can't find the Angular frontend files.

## ✅ Solution Steps

### Step 1: Rebuild Docker Images (Clean Build)

```bash
# Stop and remove containers
docker compose down

# Remove old images
docker rmi turbine-monitor-backend 2>$null
docker system prune -f

# Rebuild from scratch
docker compose up --build
```

### Step 2: Check if Frontend Files Are in the Container

While the container is running, check if the static files exist:

```bash
# Check if index.html exists in the JAR
docker exec turbine-backend ls -la /app/BOOT-INF/classes/static/ 2>$null

# Or check the JAR contents
docker exec turbine-backend jar -tf app.jar | findstr "static/index.html"
```

### Step 3: Check Docker Build Logs

Look for these messages in the build output:

```
✓ Found index.html in static/
✓ Found index.html in static/browser/
```

If you see `ERROR: index.html not found!`, the frontend build failed.

### Step 4: Verify Angular Build Works Locally

Test the Angular build on Windows:

```bash
cd frontend
npm install
npm run build -- --configuration production

# Check if files were created
dir ..\src\main\resources\static\
```

### Step 5: Check Container Logs

```bash
# Check backend logs
docker logs turbine-backend

# Look for errors like:
# - "Resource not found"
# - "Cannot find static resources"
```

### Step 6: Access the Application

Try these URLs:

1. **Root URL:** http://localhost:8080/
2. **API Test:** http://localhost:8080/api/farms
3. **Actuator:** http://localhost:8080/actuator/health

If `/api/farms` works but `/` shows whitelabel error, the backend is running but frontend files are missing.

## 🔧 Alternative: Manual Fix

If the Docker build still fails, manually copy frontend files:

### Option 1: Build Frontend Locally, Then Docker

```bash
# 1. Build frontend on Windows
cd frontend
npm install
npm run build -- --configuration production

# 2. Ensure files exist
dir ..\src\main\resources\static\index.html

# 3. Build backend with Docker (frontend already built)
docker compose up --build
```

### Option 2: Check Windows Path Issues

Windows Docker might have path resolution issues. Try:

```bash
# Use forward slashes in docker-compose.yml (already done)
# Ensure no spaces in project path
# Use WSL2 if available (better Docker support)
```

## 🐛 Common Issues on Windows

### Issue 1: Line Endings (CRLF vs LF)
**Fix:** Ensure `.gitattributes` has:
```
* text=auto
*.sh text eol=lf
```

### Issue 2: File Permissions
**Fix:** Run Docker Desktop as Administrator

### Issue 3: Antivirus Blocking
**Fix:** Add Docker directories to antivirus exclusions

### Issue 4: WSL2 Backend Issues
**Fix:** Update WSL2:
```powershell
wsl --update
```

## 📋 Quick Diagnostic Commands

```bash
# 1. Check Docker version
docker --version
docker compose version

# 2. Check if containers are running
docker ps

# 3. Check backend logs
docker logs turbine-backend --tail 50

# 4. Test API endpoint
curl http://localhost:8080/api/farms

# 5. Check if static files exist in container
docker exec turbine-backend sh -c "ls -la /app/BOOT-INF/classes/static/ 2>/dev/null || echo 'Static files not found'"
```

## ✅ Expected Behavior

When working correctly:

1. **Backend starts:** You'll see Spring Boot startup logs
2. **Database connects:** "HikariPool-1 - Starting..." messages
3. **Frontend accessible:** http://localhost:8080/ shows the Angular app (not whitelabel)
4. **API works:** http://localhost:8080/api/farms returns JSON

## 🆘 Still Not Working?

1. **Check the updated Dockerfile** - It now has verification steps
2. **Rebuild completely:** `docker compose down -v && docker compose up --build`
3. **Check Windows-specific Docker issues:** Ensure Docker Desktop is using WSL2 backend
4. **Try building frontend separately** and verify files exist before Docker build

## 📝 Notes

- **Docker version is NOT the issue** - Your Docker version is fine
- **Whitelabel error = missing static files** - Frontend build didn't copy correctly
- **Windows path issues** - Docker on Windows can have path resolution problems
- **Use WSL2** - Better Docker support on Windows

