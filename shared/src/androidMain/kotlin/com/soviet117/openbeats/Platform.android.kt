package com.soviet117.openbeats

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun currentHour(): Int =
    java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)