package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;

public class EventText extends Event {

    private final String text;
    private String outputText;

    public EventText(String text) {
        this.text = text;
        this.outputText = text;
    }

    public String getText() {
        return this.text;
    }

    public String getOutputText() {
        return this.outputText;
    }

    public String replace(String src, String target) {
        this.outputText = text.replace(src, target);
        return this.outputText;
    }
}
