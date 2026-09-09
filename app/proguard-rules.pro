# Add project specific ProGuard rules here.

# Keep model classes used by Room, Gson and Retrofit
-keep class com.islamic.bangla.data.model.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# Hilt / Dagger
-dontwarn com.google.errorprone.annotations.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Gson
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep class com.google.gson.** { *; }

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Coroutines
-dontwarn kotlinx.coroutines.**

# Retrofit service interfaces (called through dynamic proxies)
-keep interface com.islamic.bangla.data.remote.api.** { *; }

# Gson DTOs (reflective field access via Retrofit converter)
-keep class com.islamic.bangla.data.remote.dto.** { *; }
