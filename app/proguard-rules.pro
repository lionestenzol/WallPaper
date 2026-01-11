# focus-black ProGuard Rules

# Keep Google Play Billing classes
-keep class com.android.vending.billing.** { *; }
-keep class com.android.billingclient.** { *; }

# Keep model classes (data classes)
-keep class com.focusblack.wallos.model.** { *; }

# Keep billing classes
-keep class com.focusblack.wallos.billing.** { *; }

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
