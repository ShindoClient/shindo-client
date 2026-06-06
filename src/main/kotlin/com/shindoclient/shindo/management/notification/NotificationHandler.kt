package com.shindoclient.shindo.management.notification

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventRenderNotification
import java.util.concurrent.LinkedBlockingQueue

class NotificationHandler(
    private val notifications: LinkedBlockingQueue<Notification>,
) {
    private var currentNotification: Notification? = null

    @EventTarget
    fun onRenderNotification(event: EventRenderNotification) {
        if (currentNotification != null && currentNotification?.isShown() == false) {
            currentNotification = null
        }

        if (currentNotification == null && notifications.isNotEmpty()) {
            currentNotification = notifications.poll()
            currentNotification?.show()
        }

        currentNotification?.draw()
    }
}
