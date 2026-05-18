package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.util.IChatComponent

class EventReceiveChat(
    private var _message: IChatComponent,
) : Event() {
    fun getMessage(): IChatComponent = _message

    fun setMessage(message: Any) {
        _message = message as IChatComponent
    }
}
