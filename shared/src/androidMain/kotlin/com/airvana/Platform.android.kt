package com.airvana

actual fun platformLabel(): String = "Android ${android.os.Build.VERSION.RELEASE}"
