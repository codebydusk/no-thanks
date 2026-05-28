##
## No, thanks! — ProGuard / R8 rules
## These rules are essential for the release build to work correctly.
##

##── Kotlin ───────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }

##── Retrofit ─────────────────────────────────────────────────────────────────
# Retrofit reflectively reads method annotations and return types at runtime.
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}
-keep interface com.github.codebydusk.nothanks.data.ExcuseApi { *; }
-dontwarn retrofit2.**

##── Gson ─────────────────────────────────────────────────────────────────────
# Keep the data class used for JSON deserialization — Gson reads field names
# via reflection at runtime. If R8 renames them the JSON won't deserialize.
-keep class com.github.codebydusk.nothanks.data.ExcuseResponse { *; }
-keep class com.google.gson.** { *; }
-dontwarn sun.misc.**

##── OkHttp (Retrofit dependency) ─────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

##── Jetpack Glance ───────────────────────────────────────────────────────────
# Glance resolves ActionCallbacks, GlanceAppWidget, and GlanceAppWidgetReceiver
# by class name at runtime. If R8 renames or removes them the widget crashes.
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class * extends androidx.glance.action.ActionCallback {
    public <init>();
    public <methods>;
}
# Specifically keep all widget action classes in this package
-keep class com.github.codebydusk.nothanks.widget.** { *; }

##── Jetpack DataStore ────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences$Key { *; }

##── Jetpack Compose / Material3 ──────────────────────────────────────────────
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

##── Kotlin Coroutines ────────────────────────────────────────────────────────
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

##── Debug: preserve line numbers in release crash stack traces ───────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile