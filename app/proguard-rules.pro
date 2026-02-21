# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep data classes used for API/Room
-keep class com.frootsnoops.brickognize.data.remote.dto.** { *; }
-keep class com.frootsnoops.brickognize.data.local.entity.** { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Retrofit API interfaces to preserve generic method signatures
-keep interface com.frootsnoops.brickognize.data.remote.api.** { *; }

# Broader Retrofit/Gson/OkHttp preservation for suspend + reflection
-keep class retrofit2.** { *; }
-keep class retrofit2.converter.gson.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class kotlin.coroutines.Continuation { *; }
-dontwarn kotlin.coroutines.**

# Gson / reflection preservation
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod, InnerClasses
-keepattributes Exceptions
-dontwarn sun.misc.**
-keep class com.google.gson.examples.android.model.** { <fields>; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep generic signature of Gson classes (needed for reflection)
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep all model classes used with Gson
-keep class com.frootsnoops.brickognize.data.model.** { *; }
-keep class com.frootsnoops.brickognize.domain.model.** { *; }

# Keep all fields in classes that might be serialized
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Prevent obfuscation of generic types
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep enum classes
-keepclassmembers enum * { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
