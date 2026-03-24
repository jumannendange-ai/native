# ExoPlayer
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

# OneSignal
-keep class com.onesignal.** { *; }
-dontwarn com.onesignal.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }

# Keep model classes
-keep class com.jaynes.maxtv.Channel { *; }

# JSON
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.json.* *;
}
