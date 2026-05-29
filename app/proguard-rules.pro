# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.teacoffee.crm.data.remote.model.** { *; }
-keep class com.teacoffee.crm.data.local.entity.** { *; }
-keep class com.teacoffee.crm.domain.model.** { *; }

# Keep Retrofit interfaces
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Hilt
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }

# Keep Apache POI
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**

# Keep Jsoup
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**
