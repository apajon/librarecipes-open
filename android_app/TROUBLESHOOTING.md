# Android Gradle Build Troubleshooting Guide

## Common Build Issues and Solutions

### Issue: Cannot read the array length because "&lt;local6&gt;" is null

If you encounter a compilation error like:
```
Cannot read the array length because "<local6>" is null
```

This error typically occurs during annotation processing (KAPT) when Room or Hilt generates code. The project has been configured with preventive measures, but if you still encounter this error:

#### Solution 1: Clean and Rebuild
```bash
cd android_app

# Clean project completely
./gradlew clean

# Rebuild from scratch
./gradlew assembleDebug
```

#### Solution 2: Clear KAPT Generated Files
```bash
# Remove generated annotation processing files
rm -rf app/build/generated/source/kapt/
rm -rf app/build/tmp/kapt3/

# Rebuild
./gradlew clean assembleDebug
```

#### Solution 3: Reset Gradle Daemon (if Solutions 1-2 don't work)
```bash
# Stop all Gradle processes
./gradlew --stop

# Clear cache and rebuild
./gradlew clean assembleDebug
```

**Note**: The project is configured with enhanced KAPT stability settings to prevent this error from occurring. These include disabled incremental processing and improved memory allocation.

### Issue: Failed to create Jar file bcprov-jdk18on-1.79.jar

If you encounter an error like:
```
A problem occurred configuring root project 'LibraRecipes'.
java.util.concurrent.ExecutionException: org.gradle.api.GradleException: Failed to create Jar file /home/username/.gradle/caches/jars-9/5db2883d025ca47a45bd02cc62a540e3/bcprov-jdk18on-1.79.jar.
```

This is typically caused by Gradle cache corruption. Try these solutions in order:

#### Solution 1: Clean Gradle Cache
```bash
cd android_app

# Option A: Clean project caches only
./gradlew clean

# Option B: Clean all Gradle caches (if Option A doesn't work)
./gradlew clean --build-cache
```

#### Solution 2: Clear Global Gradle Cache
```bash
# Stop Gradle daemon
./gradlew --stop

# Clear global Gradle cache directory
rm -rf ~/.gradle/caches/
rm -rf ~/.gradle/wrapper/

# Rebuild
./gradlew clean assembleDebug
```

#### Solution 3: Force Dependency Re-download
```bash
./gradlew clean assembleDebug --refresh-dependencies
```

#### Solution 4: Check Disk Space and Permissions
```bash
# Check available disk space
df -h

# Check Gradle cache directory permissions
ls -la ~/.gradle/
```

### Other Common Issues

#### Deprecation Warnings
Recent versions of AGP may show deprecation warnings. These are informational and don't affect build functionality.

#### Memory Issues
If you encounter OutOfMemoryError, increase heap size in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4048m -Dfile.encoding=UTF-8
```

#### Antivirus Interference
Some antivirus software may interfere with JAR file creation. Add your project directory and Gradle cache to antivirus exclusions:
- Project directory: `/path/to/librarecipes-open/android_app`
- Gradle cache: `~/.gradle/`

## Build Commands

### Development Build
```bash
./gradlew assembleDebug
```

### Clean Build
```bash
./gradlew clean assembleDebug
```

### Force Refresh Dependencies
```bash
./gradlew assembleDebug --refresh-dependencies
```

### Build with Stack Trace (for debugging)
```bash
./gradlew assembleDebug --stacktrace
```

## Configuration Details

### Current Gradle Configuration
- **Gradle Version**: 8.13
- **Android Gradle Plugin**: 8.12.1
- **Kotlin Version**: 1.9.22
- **Compile SDK**: 34
- **Target SDK**: 34
- **Min SDK**: 24

### Compatibility Settings
The following settings preserve compatibility with previous AGP versions:
- `android.enableR8.fullMode=false` - Uses R8 compatibility mode
- `android.nonFinalResIds=false` - Preserves constant R class values
- `buildConfig = true` - Ensures BuildConfig class generation

## Getting Help

If these solutions don't resolve your issue:
1. Check the full error message and stack trace
2. Verify Java/JDK version compatibility
3. Check for conflicting Gradle processes
4. Consider IDE-specific cache clearing (Android Studio: File > Invalidate Caches)