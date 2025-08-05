package me.miki.shindo.management.notification;

import me.miki.shindo.management.nanovg.font.LegacyIcon;

public enum NotificationType {
    INFO(LegacyIcon.INFO),
    WARNING(LegacyIcon.ALERT_TRIANGLE),
    ERROR(LegacyIcon.X_CIRCLE),
    SUCCESS(LegacyIcon.CHECK),
    MUSIC(LegacyIcon.MUSIC);

    private final String icon;

    NotificationType(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }
}
