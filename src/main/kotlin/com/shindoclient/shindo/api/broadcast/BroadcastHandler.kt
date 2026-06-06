package com.shindoclient.shindo.api.broadcast

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventRenderNotification
import java.util.concurrent.LinkedBlockingQueue

class BroadcastHandler(
    private val broadcasts: LinkedBlockingQueue<BroadcastNotification>,
) {
    private var current: BroadcastNotification? = null

    @EventTarget
    fun onRenderNotification(event: EventRenderNotification) {
        if (current != null && current?.isShown() == false) {
            current = null
        }
        if (current == null && broadcasts.isNotEmpty()) {
            current = broadcasts.poll()
            current?.show()
        }
        current?.draw()
    }
}
