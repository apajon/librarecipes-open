# Android Build Troubleshooting Guide

## Common Build Issues and Solutions

### 1. "Could not load module <Error module>" / KAPT failures

**Symptoms:**
```
> Task :app:kaptGenerateStubsDebugKotlin FAILED
e: Could not load module <Error module>
```
OR
```
> Task :app:kaptGenerateStubsReleaseKotlin FAILED  
e: Could not load module <Error module>
```

**Root Cause:** Missing dependencies needed for Kotlin Annotation Processing (KAPT), particularly:
- Android Gradle Plugin
- Hilt Compiler (needed for dependency injection annotation processing)
- Other annotation processors

**Quick Diagnosis:**
```bash
# Check what dependencies are cached
./build_wrapper.sh cache-info

# This will show if key KAPT dependencies are missing
```

**Solutions:**

#### Option A: Check Network Connectivity First
```bash
# Run the connectivity checker
./check_connectivity.sh

# If internet access is available, refresh dependencies:
./build_wrapper.sh

# If no internet access detected, see Option B or C
```

#### Option B: Use Offline Mode (if sufficient dependencies are cached)
```bash
# Check cache status first
./build_wrapper.sh cache-info

# If Android Gradle Plugin and Hilt Compiler show ✅, try:
./build_wrapper.sh force-offline

# If key dependencies show ❌, you need Option C
```

#### Option C: Pre-download Dependencies
If you have access to a machine with internet:

1. On connected machine:
   ```bash
   # Download all dependencies including KAPT processors
   ./gradlew build --refresh-dependencies
   
   # Create cache archive
   tar -czf gradle-cache.tar.gz ~/.gradle/caches/
   ```

2. Transfer `gradle-cache.tar.gz` to offline machine

3. On offline machine:
   ```bash
   # Extract cache
   tar -xzf gradle-cache.tar.gz -C ~/
   
   # Verify cache
   ./build_wrapper.sh cache-info
   
   # Build offline
   ./build_wrapper.sh force-offline
   ```

#### Option D: Corporate Proxy Setup
If behind a corporate firewall:

1. Add to `gradle.properties`:
   ```properties
   systemProp.http.proxyHost=your.proxy.host
   systemProp.http.proxyPort=8080
   systemProp.https.proxyHost=your.proxy.host
   systemProp.https.proxyPort=8080
   ```

2. If authentication required:
   ```properties
   systemProp.http.proxyUser=username
   systemProp.http.proxyPassword=password
   systemProp.https.proxyUser=username
   systemProp.https.proxyPassword=password
   ```

### 2. Repository Configuration Issues

**Symptoms:**
- Timeouts downloading from repositories
- "Could not resolve" errors

**Solution:** The build.gradle has been updated with fallback repositories:
- Primary: Google Maven, Maven Central
- Fallbacks: Gradle Plugin Portal, Maven Local

### 3. Gradle Daemon Issues

**Symptoms:**
- Inconsistent build failures
- Memory issues

**Solutions:**
```bash
# Stop and restart daemon
./gradlew --stop
./gradlew build

# Check daemon status
./gradlew --status
```

### 4. Clean Build
```bash
# Full clean rebuild
./gradlew clean build --refresh-dependencies
```

## Environment-Specific Solutions

### CI/CD Environments
- Use dependency caching in CI
- Pre-download dependencies in build setup
- Use `--offline` mode if dependencies are cached

### Docker/Containerized Builds
- Include dependency download in Docker build
- Use multi-stage builds with dependency layer
- Mount Gradle cache volume

### Restricted Networks
- Set up local Maven mirror
- Use Nexus or Artifactory
- Configure proxy settings

## Build Configuration Improvements

The following improvements have been made to handle connectivity issues:

1. **Multiple Repository Fallbacks** (in `build.gradle`)
   - Google Maven (primary)
   - Maven Central (primary) 
   - Gradle Plugin Portal (fallback)
   - Maven Local (last resort)

2. **Network Timeout Settings** (in `gradle.properties`)
   - Connection timeouts: 30 seconds
   - Socket timeouts: 30 seconds

3. **Configuration Cache** (enabled)
   - Faster subsequent builds
   - Better offline capability

## Testing Your Build

1. **Check connectivity:**
   ```bash
   ./check_connectivity.sh
   ```

2. **Try offline build:**
   ```bash
   ./gradlew build --offline
   ```

3. **Force dependency refresh:**
   ```bash
   ./gradlew build --refresh-dependencies
   ```

4. **Clean build:**
   ```bash
   ./gradlew clean build
   ```

## Getting Help

If issues persist:

1. Check the build logs with `--info` or `--debug`
2. Verify network connectivity to required repositories
3. Consider using a local Maven repository mirror
4. Contact your system administrator about proxy/firewall settings

## Repository URLs to Whitelist

If you need to whitelist repositories in your firewall:

- `https://dl.google.com` (Google Maven)
- `https://maven.google.com` (Alternative Google Maven)
- `https://repo1.maven.org` (Maven Central)
- `https://plugins.gradle.org` (Gradle Plugin Portal)
- `https://services.gradle.org` (Gradle distributions)