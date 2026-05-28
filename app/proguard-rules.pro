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

##── Room (used internally by WorkManager / Glance) ───────────────────────────
# Room generates a *_Impl class at compile time and finds it by naming convention
# at runtime. R8 was stripping WorkDatabase_Impl, causing the startup crash:
#   "Failed to create an instance of androidx.work.impl.WorkDatabase"
-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Database class * { *; }
-dontwarn androidx.room.**

##── WorkManager (pulled in transitively by Glance for widget scheduling) ─────
# WorkManager locates Worker implementations by class name via reflection.
-keep class androidx.work.** { *; }
-keep interface androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-dontwarn androidx.work.**

##── Jetpack Glance (comprehensive) ───────────────────────────────────────────
# Glance uses reflection extensively for composable resolution, state management,
# SizeMode dispatch, and ActionCallback lookup via actionRunCallback<T>().
# Keeping only subclasses is not enough — all Glance internals must be kept.
-keep class androidx.glance.** { *; }
-keep interface androidx.glance.** { *; }
# ActionCallback subclasses — resolved by reified type → class name at runtime
-keep class * extends androidx.glance.action.ActionCallback { *; }
# All app widget classes: NoThanksWidget, NoThanksWidgetReceiver, *Action
-keep class com.github.codebydusk.nothanks.widget.** { *; }

##── Jetpack DataStore ────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences$Key { *; }

##── Jetpack Compose / Material3 ──────────────────────────────────────────────
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

##── Kotlin Coroutines (complete) ────────────────────────────────────────────
# Coroutines dispatch machinery is found by class name at runtime.
# The previous rule only kept volatile fields — far too weak for Glance widgets.
-keep class kotlin.coroutines.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

##── Debug: preserve line numbers in release crash stack traces ───────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile