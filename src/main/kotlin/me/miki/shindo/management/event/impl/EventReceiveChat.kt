package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.util.IChatComponent

class EventReceiveChat(
    private var message: IChatComponent,
) : Event() {
    fun getMessage(): IChatComponent = message

    fun setMessage(message: IChatComponent) {
        this.message = message
    }
}
