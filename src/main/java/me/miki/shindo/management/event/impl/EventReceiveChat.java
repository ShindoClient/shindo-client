package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;
import net.minecraft.util.IChatComponent;

public class EventReceiveChat extends Event {

    private IChatComponent message;

    public EventReceiveChat(IChatComponent message) {
        this.message = message;
    }

    public IChatComponent getMessage() {
        return message;
    }

    public void setMessage(IChatComponent newMessage) {
        this.message = newMessage;
    }
}