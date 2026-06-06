package com.shindoclient.shindo.management.notification

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.management.language.TranslateText
import java.util.concurrent.LinkedBlockingQueue

class NotificationManager {
    private val notifications = LinkedBlockingQueue<Notification>()

    init {
        Shindo.getInstance().getEventManager().register(NotificationHandler(notifications))
    }

    fun post(
        title: TranslateText,
        message: TranslateText,
        type: NotificationType,
    ) {
        notifications.add(Notification(title, message, type))
    }

    fun post(
        title: String,
        message: String,
        type: NotificationType,
    ) {
        notifications.add(Notification(title, message, type))
    }

    fun post(
        title: TranslateText,
        message: String,
        type: NotificationType,
    ) {
        notifications.add(Notification(title, message, type))
    }

    fun post(
        title: String,
        message: TranslateText,
        type: NotificationType,
    ) {
        notifications.add(Notification(title, message, type))
    }
}
