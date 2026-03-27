# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the default ProGuard configuration included with the Android Gradle plugin.

# Keep Hilt generated code
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Room entities
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Keep Media3 player
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
