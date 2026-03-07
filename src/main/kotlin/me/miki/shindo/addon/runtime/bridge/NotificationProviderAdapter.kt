package me.miki.shindo.addon.runtime.bridge

import me.miki.shindo.Shindo
import me.miki.shindo.addon.api.notification.AddonNotificationType
import me.miki.shindo.addon.api.notification.INotificationProvider
import me.miki.shindo.management.notification.NotificationType

class NotificationProviderAdapter : INotificationProvider {

    override fun post(title: String, message: String, type: AddonNotificationType) {
        Shindo.getInstance().notificationManager.post(title, message, type.toClient())
    }

    private fun AddonNotificationType.toClient(): NotificationType = when (this) {
        AddonNotificationType.INFO -> NotificationType.INFO
        AddonNotificationType.WARNING -> NotificationType.WARNING
        AddonNotificationType.ERROR -> NotificationType.ERROR
        AddonNotificationType.SUCCESS -> NotificationType.SUCCESS
        AddonNotificationType.MUSIC -> NotificationType.MUSIC
    }
}
