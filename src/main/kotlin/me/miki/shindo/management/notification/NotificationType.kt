package me.miki.shindo.management.notification

import me.miki.shindo.management.nanovg.font.LegacyIcon

enum class NotificationType(val icon: String) {
    INFO(LegacyIcon.INFO),
    WARNING(LegacyIcon.ALERT_TRIANGLE),
    ERROR(LegacyIcon.X_CIRCLE),
    SUCCESS(LegacyIcon.CHECK),
    MUSIC(LegacyIcon.MUSIC)
}
