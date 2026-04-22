# Lindungi JNI Interface
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.example.androidautobuildapk.MainActivity { *; }

# Optimasi tambahan agar kode tidak rusak saat dikecilkan
-dontwarn jni.**
