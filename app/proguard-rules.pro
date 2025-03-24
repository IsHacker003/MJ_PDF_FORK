# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.shockwave.**


# Restore some Source file names and restore approximate line numbers in the stack traces,
# otherwise the stack traces are pretty useless
-keepattributes SourceFile,LineNumberTable

# ACRA loads Plugins using reflection
-keep class * implements org.acra.plugins.Plugin {*;}

# ACRA uses enum fields in json
-keep enum org.acra.** {*;}

# autodsl accesses constructors using reflection
-keepclassmembers class * implements org.acra.config.Configuration { <init>(...); }

# ACRA creates a proxy for this interface
-keep interface org.acra.ErrorReporter

-dontwarn android.support.**

-dontwarn com.faendir.kotlin.autodsl.DslInspect
-dontwarn com.faendir.kotlin.autodsl.DslMandatory
-dontwarn com.google.auto.service.AutoService

# Keep annotation processor classes used by AutoService, do not strip them.
-dontwarn javax.annotation.processing.**

# Keep AutoService annotations themselves
-keep class com.google.auto.service.** { *; }
-keep @com.google.auto.service.AutoService class *

# Keep classes that use AutoService annotations
-keep,allowobfuscation @interface com.google.auto.service.AutoService

# If you're using classes only referenced via reflection, you might also need:
-keepclassmembers class * {
    @com.google.auto.service.AutoService *;
}

# Ignore all warnings for missing classes in the javax.annotation.processing and related namespaces
-dontwarn javax.annotation.processing.**
-dontwarn javax.lang.model.**
-dontwarn javax.tools.**

# Keep the classes that might be indirectly referenced by other libraries
-keep class javax.annotation.processing.** { *; }
-keep class javax.lang.model.** { *; }
-keep class javax.tools.** { *; }

# Keep class names and attributes needed by Gson for reflection.
-keepattributes Signature
-keepattributes *Annotation*

# Gson specific classes
-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.gitlab.mudlej.MjPdfReader.data.SearchResult { *; }
-keep class android.graphics.Bitmap { *; }
-keep class java.io.File { *; }

# Prevent Gson from being obfuscated
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
-dontwarn top.defaults.checkerboarddrawable.CheckerboardDrawable

-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
