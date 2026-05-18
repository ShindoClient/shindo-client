package me.miki.shindo.api.broadcast

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRenderNotification
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
