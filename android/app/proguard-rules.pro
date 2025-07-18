# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# LibraRecipes Android ProGuard Configuration

# Keep WebView JavaScript interfaces
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep WebView classes
-keep class android.webkit.WebView { *; }
-keep class * extends android.webkit.WebViewClient { *; }
-keep class * extends android.webkit.WebChromeClient { *; }

# Keep Python-related classes for Chaquopy
-keep class com.chaquo.python.** { *; }
-keep class com.librarecipes.** { *; }

# Keep Python modules and assets
-keep class **.python.** { *; }
-keepattributes *Annotation*

# Keep MainActivity and related classes
-keep class com.librarecipes.MainActivity { *; }

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Kotlin metadata
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# AndroidX and Material Design
-keep class androidx.** { *; }
-keep class com.google.android.material.** { *; }

# Prevent optimization of classes with native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep classes referenced in AndroidManifest.xml
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Keep Timber logging framework
-keep class timber.log.** { *; }
