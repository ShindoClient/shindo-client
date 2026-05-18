package me.miki.shindo.management.notification

import me.miki.shindo.Shindo
import me.miki.shindo.logger.FileLogWriter
import me.miki.shindo.management.language.TranslateText
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
        FileLogWriter.notification(title.getText() + " | " + message.getText() + " | " + type.name)
    }

    fun post(
        title: String,
        message: String,
        type: NotificationType,
    ) {
        notifications.add(Notification(title, message, type))
        FileLogWriter.notification(title + " | " + message + " | " + type.name)
    }

    fun post(
        title: TranslateText,
        message: String,
        type: NotificationType,
    ) {
        notifications.add(Notification(title, message, type))
        FileLogWriter.notification(title.getText() + " | " + message + " | " + type.name)
    }

    fun post(
        title: String,
        message: TranslateText,
        type: NotificationType,
    ) {
        notifications.add(Notification(title, message, type))
        FileLogWriter.notification(title + " | " + message.getText() + " | " + type.name)
    }
}
