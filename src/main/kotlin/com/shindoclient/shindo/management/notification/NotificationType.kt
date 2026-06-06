package com.shindoclient.shindo.management.notification

import com.shindoclient.shindo.management.nanovg.font.Lucide

enum class NotificationType(
    val icon: String,
) {
    INFO(Lucide.INFO),
    WARNING(Lucide.ALERT_TRIANGLE),
    ERROR(Lucide.X_CIRCLE),
    SUCCESS(Lucide.CHECK),
    MUSIC(Lucide.MUSIC),
    WEBSOCKET(Lucide.CHEVRONS_LEFT_RIGHT_ELLIPSIS),
}
